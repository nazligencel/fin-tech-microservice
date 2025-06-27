package com.fintech.fin_tech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        long MAX_AGE_SECS = 3600;

        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                //.allowedOrigins("https://fin-tech-production.up.railway.app","http://localhost")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // Kimlik bilgisi içeren isteklere (JWT token) izin ver
                .maxAge(3600); //  ne kadar süreyle cache'leneceği
    }
}