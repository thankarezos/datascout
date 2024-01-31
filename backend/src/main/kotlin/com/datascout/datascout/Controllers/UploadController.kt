package com.datascout.datascout.Controllers

import com.datascout.datascout.models.Image
import com.datascout.datascout.models.Label
import com.datascout.datascout.repository.ImageRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

@RestController
@RequestMapping("/api")
class UploadController(val restTemplate: RestTemplate, val imageRepo: ImageRepository) {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("uri", required = false) uri: String?): ResponseEntity<JsonResponse>{

        val responseFromPython = infer(file)

        val labels = responseFromPython?.labels


        val labelSet = if (labels != null) {
            val uniqueLabels = labels.distinct()
            val labelsCopy = labels
            val counts = uniqueLabels.map { label -> labelsCopy.count { it == label } }
            uniqueLabels.zip(counts).map { Label(it.first, it.second) }.toSet()
        } else {
            setOf()
        }

        //save image to database
        val image = Image(
            userId = 1,
            path = "path",
            labels = labelSet
        )

        imageRepo.save(image)

        val anntFile = annt(file, responseFromPython)



        //save anntFile to storage

        val directory = Paths.get("src/main/resources/static/images")
        if (!Files.exists(directory)) {
            Files.createDirectories(directory)
        }

        val fileExtension = file.originalFilename?.substringAfterLast('.', "") ?: ""

        val filePath = directory.resolve("${image.id}.$fileExtension")

        // Save the file
        try {
            anntFile?.inputStream()?.use { inputStream ->
                Files.copy(inputStream, filePath)
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file, error: $e")
        }



        return ResponseEntity.ok(responseFromPython)
    }

    @GetMapping("/images")
    @ResponseBody
    fun getAllImages(): Iterable<Image> {
        // Method to handle GET requests to "/users"
        return imageRepo.findAll()
    }

    @PostMapping("/images")
    fun addImage(): Image {

        var labels = setOf(
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
        val pythonApiUrl = "http://0.0.0.0:3333/infer"

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
        val pythonApiUrl = "http://localhost:3333/annt"

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


