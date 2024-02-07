package com.datascout.datascout.controllers

import com.datascout.datascout.JwtUtil
import com.datascout.datascout.dto.ImageDto
import com.datascout.datascout.dto.Response
import com.datascout.datascout.models.Image
import com.datascout.datascout.models.Label
import com.datascout.datascout.repositories.ImageRepository
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

data class JsonResponse(
    val scores: List<Double>? = null,
    var labels: List<String>? = null,
    val boxes: List<List<Double>>? = null,
)



@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        // Customizing the Jackson ObjectMapper
        return jacksonObjectMapper().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL) // Ignore null fields
        }
    }
}

@RestController
@RequestMapping("/api")
class UploadController(
        val restTemplate: RestTemplate,
        val imageRepo: ImageRepository,
        @Value("\${fastapi.url}") private val fastApiUrl: String,
        @Value("\${storage.original.path}") private val originalImagesPath: String,
        @Value("\${storage.annotated.path}") private val annotatedImagesPath: String,
        private val jwtUtil: JwtUtil
) {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@RequestParam("file") file: MultipartFile,
                   @RequestParam("uri", required = false) uri: String?,
                   request: HttpServletRequest
    ): ResponseEntity<Response<JsonResponse>> {
        val jwt = request.cookies?.firstOrNull { it.name == "jwt" }?.value
        if (jwt == null) {
            return ResponseEntity.status(401).body(Response(error = "Unauthorized"))
        }
        val userId = jwtUtil.validateAndExtractUserId(jwt)
                ?: return ResponseEntity.status(401).body(Response(error = "Unauthorized"))
        val responseFromPython = infer(file)

        val labels = responseFromPython?.labels

        val labelSet = if (labels != null) {
            val uniqueLabels = labels.distinct()
            val counts = uniqueLabels.map { label -> labels.count { it == label } }
            uniqueLabels.zip(counts).map { Label(it.first, it.second) }.toSet()
        } else {
            setOf()
        }

        val image = Image(
            userId = userId,
            labels = labelSet
        )

        val fileExtension = "png"



        val savedImage = imageRepo.save(image)

        image.path = "${savedImage.id}.$fileExtension"

        imageRepo.save(image)
        //save original file to storage
        val originalDirectory = Paths.get("storage/images/original")
        if (!Files.exists(originalDirectory)) {
            Files.createDirectories(originalDirectory)
        }

        val originalFilePath = originalDirectory.resolve("${image.id}.$fileExtension")

        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, originalFilePath)
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file, error: $e")
        }


        val anntFile = annt(file, responseFromPython)



        //save anntFile to storage

        val directory = Paths.get("storage/images/annotated")
        if (!Files.exists(directory)) {
            Files.createDirectories(directory)
        }



        val filePath = directory.resolve("${image.id}.$fileExtension")
        // Save the file
        try {
            anntFile?.inputStream()?.use { inputStream ->
                Files.copy(inputStream, filePath)
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file, error: $e")
        }


        return ResponseEntity.ok(Response())
    }

    @GetMapping("/images")
    @ResponseBody
    fun getAllImages(): Iterable<ImageDto> {
        // Method to handle GET requests to "/users"
        val images = imageRepo.findAll()
        //add the path to the images
        val imagesDto = images.map { image ->
            val originalPath = originalImagesPath + image.path
            val annotatedPath = annotatedImagesPath + image.path
            ImageDto(
                id = image.id,
                userId = image.userId,
                originalPath = originalPath,
                path = annotatedPath,
                labels = image.labels?.map { label -> com.datascout.datascout.dto.LabelDto(label.label, label.count) }?.toSet()
            )
        }
        return imagesDto
    }

    @GetMapping("/images/{userId}")
    @ResponseBody
    fun getImage(@PathVariable userId: Long): Iterable<ImageDto?> {
        // Method to handle GET requests to "/users"
        val images = imageRepo.findAllByUserId(userId)
        //add the path to the images
        val imagesDto = images.map {
            val originalPath = originalImagesPath + it.path
            val annotatedPath = annotatedImagesPath + it.path
            ImageDto(
                    id = it.id,
                    userId = it.userId,
                    originalPath = originalPath,
                    path = annotatedPath,
                    labels = it.labels?.map { label -> com.datascout.datascout.dto.LabelDto(label.label, label.count) }?.toSet()
            )
        }
        return imagesDto
    }

    @PostMapping("/images")
    fun addImage(): Image {

        val labels = setOf(
            Label("label1", 1),
            Label("label2", 2),
            Label("label3", 3),
        )

        val image = Image(
            userId = 1,
            path = "path",
            labels = labels
        )
        return imageRepo.save(image)
    }

    private fun infer(file: MultipartFile): JsonResponse?
    {
        val pythonApiUrl = fastApiUrl + "infer"

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // Prepare the body map
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        if (!file.isEmpty) {
            body.add("file", file.resource)
        }


        // Create HttpEntity with headers and body
        val requestEntity = HttpEntity<MultiValueMap<String, Any>>(body, headers)

        // Execute the request
        return restTemplate.postForObject(pythonApiUrl, requestEntity, JsonResponse::class.java)
    }

    private fun annt(file: MultipartFile, response: JsonResponse?): ByteArray?
    {
        val pythonApiUrl = fastApiUrl + "annt"

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA


        val mapper = jacksonObjectMapper()
        val serializedResponse = mapper.writeValueAsString(response)
        // Prepare the body map
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        if (!file.isEmpty) {
            body.add("file", file.resource)
        }
        body.add("scores", serializedResponse)

        val requestEntity = HttpEntity<MultiValueMap<String, Any>>(body, headers)

        return restTemplate.postForObject(pythonApiUrl, requestEntity, ByteArray::class.java)
    }
}


