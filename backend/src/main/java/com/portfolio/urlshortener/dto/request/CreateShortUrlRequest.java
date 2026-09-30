package com.portfolio.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {

    @NotBlank(message = "Original destination URL is required")
    @Pattern(
        regexp = "^(https?://).+",
        message = "URL must start with http:// or https://"
    )
    private String longUrl;

    @Size(min = 3, max = 32, message = "Custom alias must be between 3 and 32 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]*$",
        message = "Custom alias can only contain alphanumeric characters, hyphens, and underscores"
    )
    private String customAlias;

    private Integer expirationDays;
}
