package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.client.GoogleBooksClient;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTS ADICIONALES para GoogleBooksClient
 *
 * Siguiendo EXACTAMENTE tu patrón de GoogleBooksClientTest.java
 * Añadiendo tests de error handling y validación
 */
@SpringBootTest
public class GoogleBooksClientAdditionalTest {

    @Autowired
    private GoogleBooksClient googleBooksClient;

    /**
     * TEST: Parameter validation - debe lanzar excepción para null
     */
    @Test
    void shouldThrowExceptionForNullQuery() {
        // Expect exception for null query - this is correct behavior
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchBooks(null);
        }, "Should throw exception for null query");

        System.out.println("✅ Null parameter validation works correctly");
    }

    /**
     * TEST: Parameter validation - debe lanzar excepción para query vacía
     */
    @Test
    void shouldThrowExceptionForEmptyQuery() {
        // Expect exception for empty query - this is correct behavior
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchBooks("");
        }, "Should throw exception for empty query");

        System.out.println("✅ Empty parameter validation works correctly");
    }

    /**
     * TEST: Búsqueda con query problemática (no null, pero rara)
     */
    @Test
    void shouldHandleProblematicQuery() {
        // Test with strange but valid query
        String problematicQuery = "!@#$%^&*()";

        // This should NOT throw exception, should return empty list or handle gracefully
        assertDoesNotThrow(() -> {
            List<GoogleBookDTO> result = googleBooksClient.searchBooks(problematicQuery);
            assertNotNull(result, "Should return list even with problematic query");
        });

        System.out.println("✅ Problematic query handling works gracefully");
    }
}
