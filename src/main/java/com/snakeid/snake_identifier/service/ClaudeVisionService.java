package com.snakeid.snake_identifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snakeid.snake_identifier.model.SnakeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.List;

@Service
public class ClaudeVisionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PROMPT = """
        You are an expert herpetologist specializing in Indian snake species.
        Analyze this snake image and respond ONLY with a valid JSON object. No markdown, no explanation outside JSON.
        Return this exact structure:
        {
          "commonName": "Snake common name in English",
          "hindiName": "Hindi name if known, else empty string",
          "scientificName": "Genus species",
          "venomous": true or false or null,
          "venomType": "neurotoxic/hemotoxic/cytotoxic/non-venomous/unknown",
          "dangerLevel": "high/medium/low/none/unknown",
          "foundInRegions": ["list of Indian states/regions"],
          "firstAid": ["Step 1", "Step 2", "Step 3", "Step 4"],
          "symptoms": ["symptom1", "symptom2", "symptom3"],
          "confidence": 0.0 to 1.0,
          "notes": "Brief note for rural users",
          "isSnake": true or false
        }
        If no snake in image, set isSnake: false.
        """;

    public SnakeResult identifySnake(MultipartFile image) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        String mediaType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mediaType,
                                        "data", base64Image
                                ))
                        )
                ))
        );

        String body = objectMapper.writeValueAsString(requestBody);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error: " + response.body());
        }

        var responseJson = objectMapper.readTree(response.body());
        String content = responseJson
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();

        String cleaned = content.replace("```json", "").replace("```", "").trim();
        return objectMapper.readValue(cleaned, SnakeResult.class);
    }
}