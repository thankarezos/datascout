package com.datascout.datascout.Controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    @GetMapping("/helloWorld")
    fun sayHello(): String {
        return "Hello World!2"
    }
}
