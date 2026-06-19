package com.urlShortner.urlShortnerProject.Controller;


import com.urlShortner.urlShortnerProject.DTO.ShortenRequest;
import com.urlShortner.urlShortnerProject.DTO.ShortenResponse;
import com.urlShortner.urlShortnerProject.Service.UrlShortnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlShortnerController {

    @Autowired
    private final UrlShortnerService service;



    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.shorten(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);


    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = service.resolve(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<ShortenResponse> getStats(@PathVariable String shortCode){

        return ResponseEntity.ok(service.getStats(shortCode));

    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<ShortenResponse>> getAllUrls() {
        return ResponseEntity.ok(service.getAllUrls());
    }

    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        service.delete(shortCode);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "url-shortener"));
    }


}
