package com.weddingfit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/")
    public String home() {
        return "WeddingFit API Server is running!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "Test endpoint works!";
    }
}