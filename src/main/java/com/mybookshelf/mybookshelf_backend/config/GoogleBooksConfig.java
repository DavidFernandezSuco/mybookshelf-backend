package com.mybookshelf.mybookshelf_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * CONFIGURACIÓN PARA GOOGLE BOOKS API
 *
 * Esta clase configura el RestTemplate que usaremos para hacer
 * llamadas HTTP a la API de Google Books.
 */
@Configuration
public class GoogleBooksConfig {

    // Inyectar valores desde application.properties
    @Value("${google.books.timeout:5000}")
    private int timeout;

    /**
     * Bean RestTemplate configurado específicamente para Google Books API
     *
     * - Timeout de conexión: configurable (default 5 segundos)
     * - Timeout de lectura: configurable (default 5 segundos)
     * - Configuración optimizada para APIs externas
     */
    @Bean
    public RestTemplate googleBooksRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(timeout))
                .readTimeout(Duration.ofMillis(timeout))
                .build();
    }
}