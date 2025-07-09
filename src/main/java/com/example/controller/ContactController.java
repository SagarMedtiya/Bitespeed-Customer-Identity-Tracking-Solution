package com.example.controller;

import com.example.dto.ContactRequest;
import com.example.dto.ContactResponse;
import com.example.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContactController {
    @Autowired
    private ContactService contactService;

    @GetMapping("/")
    public String home() {
        return "Spring Boot app is running!";
    }
    @PostMapping("/api/identity")
    public ResponseEntity<ContactResponse> identityContact(@RequestBody ContactRequest request){
        ContactResponse response = contactService.processContact(request);
        return ResponseEntity.ok(response);
    }
}


