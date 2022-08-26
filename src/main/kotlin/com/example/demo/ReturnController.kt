package com.example.demo

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/")
class ReturnController() {
    @GetMapping("/test")
    fun test() = "OK"
}