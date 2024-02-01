package com.datascout.datascout.Controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

data class JsonResponse(val success: String)

@RestController
@RequestMapping("/api")
class UploadController {

    private val restTemplate = RestTemplate()

    // @GetMapping("/helloWorld2")
    // fun sayHello(): String {
    //     return "Hello World!2"
    // }

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("uri", required = false) uri: String?): ResponseEntity<JsonResponse>{
        if (file.isEmpty && uri.isNullOrBlank()){
            return ResponseEntity.ok(JsonResponse(success = "false"))
        }

        val pythonApiUrl = "http://0.0.0.0:3333/infer"

        val data = if (file.isEmpty){
            mapOf("uri" to uri)
        }else{
            mapOf("file" to file.bytes)
        }

        //val responseFromPython = restTemplate.postForObject(pythonApiUrl, data, JsonResponse::class.java)
        // println(responseFromPython)

        val responseFromPython: String? = restTemplate.postForObject(pythonApiUrl, data, String::class.java)
        return ResponseEntity.ok(JsonResponse(success = responseFromPython ?: ""))
        
        //return ResponseEntity.ok(responseFromPython)
        //return ResponseEntity.ok(JsonResponse(success = "responseFromPython"))
    }

    //return json success response
    // @GetMapping("/test")
    // fun test(): JsonResponse{
    //     return JsonResponse(success = "true")
    // }
}
