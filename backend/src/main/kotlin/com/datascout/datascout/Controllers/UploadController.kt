package com.datascout.datascout.Controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

data class JsonResponse(val success: Boolean)

@RestController
class UploadController {

    @GetMapping("/helloWorld2")
    fun sayHello(): String {
        return "Hello World!2"
    }

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@RequestParam("file") file: MultipartFile): JsonResponse {
        if (file.isEmpty) {
            return JsonResponse(success = false)
        }
        return JsonResponse(success = true)
    }

    //return json success response
    @GetMapping("/test")
    fun test(): JsonResponse {
        return JsonResponse(success = true)
    }

    
}
