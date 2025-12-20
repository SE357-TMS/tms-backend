package com.example.tms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/admin/ping")
    public ResponseEntity<String> adminPing() {
        return ResponseEntity.ok("admin ok");
    }

    @GetMapping("/staff/ping")
    public ResponseEntity<String> staffPing() {
        return ResponseEntity.ok("staff ok");
    }

    @GetMapping("/customer/ping")
    public ResponseEntity<String> customerPing() {
        return ResponseEntity.ok("customer ok");
    }
}

