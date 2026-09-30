package com.portfolio.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Distributed URL Shortener & Analytics API")
                        .description("High-performance URL shortening service with Redis caching and real-time click analytics.")
                        .version("1.0.0")
                        .contact(new Contact().name("Praveenkumar T").email("praveenkumardtp@gmail.com")));
    }
}
