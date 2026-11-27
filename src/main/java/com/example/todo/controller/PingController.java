package com.example.todo.controller;
import org.springframework.web.bind.annotation.*;
@RestController
public class PingController {
    @GetMapping("/api/ping") public String ping() { return "OK"; }
}
