package com.schoolproject.app.controller;

import com.schoolproject.app.dto.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public MessageResponse home() {
        return new MessageResponse("AcademicAI API is running");
    }

    @GetMapping("/health")
    public MessageResponse health() {
        return new MessageResponse("OK");
    }
}
