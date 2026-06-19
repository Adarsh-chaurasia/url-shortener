package com.urlShortner.urlShortnerProject.DTO;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenRequest {

    @NotNull(message = "Url cannot be null")
    @URL(message = "Must be a valid URL (include http:// or https://)")
    private String originalUrl;


    @Size(min = 3, max = 20, message = "Custom code must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Custom code may only contain letters, numbers, hyphens, and underscores")
    private String customCode;

    private String expiresAt;


}
