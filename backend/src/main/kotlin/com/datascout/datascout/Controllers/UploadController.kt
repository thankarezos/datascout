package com.datascout.datascout.Controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

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
class UploadController(val restTemplate: RestTemplate) {

    // @GetMapping("/helloWorld2")
    // fun sayHello(): String {
    //     return "Hello World!2"
    // }

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("uri", required = false) uri: String?): ResponseEntity<JsonResponse>{
        // if (file.isEmpty && uri.isNullOrBlank()){
        //     return ResponseEntity.ok(JsonResponse(success = "false"))
        // }

        val pythonApiUrl = "http://0.0.0.0:3333/infer"
        
        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // Prepare the body map
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        if (!file.isEmpty) {
            body.add("file", file.resource)
        }
        uri?.let {
            body.add("uri", it)
        }

        // Create HttpEntity with headers and body
        val requestEntity = HttpEntity<MultiValueMap<String, Any>>(body, headers)

        // Execute the request
        val responseFromPython = restTemplate.postForObject(pythonApiUrl, requestEntity, JsonResponse::class.java)

        var labels = responseFromPython?.labels

        //find unique labels and their counts
        if (labels != null) {
            val uniqueLabels = labels.distinct()
            val labelsCopy = labels
            val counts = uniqueLabels.map { label -> labelsCopy.count { it == label } }
            labels = uniqueLabels.zip(counts).map { (label, count) -> "$label ($count)" }
        }


        return ResponseEntity.ok(responseFromPython)
    }

    //return json success response
    // @GetMapping("/test")
    // fun test(): JsonResponse{
    //     return JsonResponse(success = "true")
    // }
}
