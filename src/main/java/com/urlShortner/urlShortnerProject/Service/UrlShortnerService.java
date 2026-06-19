package com.urlShortner.urlShortnerProject.Service;

import com.urlShortner.urlShortnerProject.DTO.ShortenRequest;
import com.urlShortner.urlShortnerProject.DTO.ShortenResponse;
import com.urlShortner.urlShortnerProject.Exception.UrlNotFoundException;
import com.urlShortner.urlShortnerProject.Exception.UrlShortenerException;
import com.urlShortner.urlShortnerProject.Model.UrlMapping;
import com.urlShortner.urlShortnerProject.Repository.UrlMappingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortnerService {

    private final UrlMappingRepository repository;


    private String baseUrl = "http://localhost:8080";

    private int shortCodeLength = 7;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public ShortenResponse shorten(ShortenRequest request){
        String code ;

        if(request.getCustomCode() != null && !request.getCustomCode().isEmpty()){
            code = request.getCustomCode();
        } else {
            code = generateUniqueCode();
        }

        LocalDateTime expiresAt = null;
        if (request.getExpiresAt() != null && !request.getExpiresAt().isBlank()) {
            try {
                expiresAt = LocalDateTime.parse(request.getExpiresAt());
                if (expiresAt.isBefore(LocalDateTime.now())) {
                    throw new UrlShortenerException("Expiry date must be in the future.");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new UrlShortenerException("Invalid expiry date format. Use ISO format: 2025-12-31T23:59:59");
            }
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(code)
                .originalUrl(request.getOriginalUrl())
                .clickCount(0L)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Created short URL: {} -> {}", code, request.getOriginalUrl());
        return toResponse(saved);

    }

    @Transactional
    public String resolve(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (!mapping.isActive()) {
            throw new UrlShortenerException("This short URL has been deactivated.");
        }
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlShortenerException("This short URL has expired.");
        }

        repository.incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }
    @Transactional(readOnly = true)
    public ShortenResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return toResponse(mapping);
    }

    @Transactional(readOnly = true)
    public List<ShortenResponse> getAllUrls() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deactivate(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        mapping.setActive(false);
        repository.save(mapping);
        log.info("Deactivated short URL: {}", shortCode);
    }

    @Transactional
    public void delete(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        repository.delete(mapping);
        log.info("Deleted short URL: {}", shortCode);
    }


    private ShortenResponse toResponse(UrlMapping mapping) {
        return ShortenResponse.builder()
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .active(mapping.isActive())
                .build();
    }
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = generateCode();
            attempts++;
            if (attempts > 10) {
                throw new UrlShortenerException("Could not generate a unique short code. Please try again.");
            }
        } while (repository.existsByShortCode(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }


}
