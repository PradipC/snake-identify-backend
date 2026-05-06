package com.snakeid.snake_identifier.controller;

import com.snakeid.snake_identifier.model.SnakeResult;
import com.snakeid.snake_identifier.service.ClaudeVisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/snake")
@CrossOrigin(origins = "*")
public class SnakeController {

    private final ClaudeVisionService claudeVisionService;

    public SnakeController(ClaudeVisionService claudeVisionService) {
        this.claudeVisionService = claudeVisionService;
    }

    @PostMapping("/identify")
    public ResponseEntity<?> identifySnake(@RequestParam("image") MultipartFile image) {
        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload an image");
            }
            SnakeResult result = claudeVisionService.identifySnake(image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SnakeID API is running!");
    }
}