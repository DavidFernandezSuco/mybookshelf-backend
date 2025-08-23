package com.mybookshelf.mybookshelf_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBooksResponseDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookVolumeInfoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST SIMPLE PARA VERIFICAR QUE LOS DTOs FUNCIONAN
 *
 * Siguiendo patrón de tus tests existentes
 */
@SpringBootTest
public class GoogleBooksDTOTest {

    @Test
    public void testGoogleBookDTO() {
        // Crear VolumeInfo
        GoogleBookVolumeInfoDTO volumeInfo = new GoogleBookVolumeInfoDTO();
        volumeInfo.setTitle("Clean Code");
        volumeInfo.setAuthors(Arrays.asList("Robert C. Martin"));
        volumeInfo.setPageCount(464);
        volumeInfo.setDescription("A guide to writing clean code");

        // Crear GoogleBook
        GoogleBookDTO book = new GoogleBookDTO();
        book.setId("test-id-123");
        book.setVolumeInfo(volumeInfo);

        // Verificar métodos básicos
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthorsAsString());
        assertFalse(book.hasIsbn()); // No configuramos ISBN

        System.out.println("✅ GoogleBookDTO funciona correctamente");
        System.out.println("📖 Libro: " + book.toString());
    }

    @Test
    public void testGoogleBooksResponseDTO() {
        // Crear libro de ejemplo
        GoogleBookVolumeInfoDTO volumeInfo = new GoogleBookVolumeInfoDTO();
        volumeInfo.setTitle("Java Programming");
        volumeInfo.setAuthors(Arrays.asList("John Doe"));

        GoogleBookDTO book = new GoogleBookDTO("book-1", volumeInfo);

        // Crear respuesta
        GoogleBooksResponseDTO response = new GoogleBooksResponseDTO();
        response.setKind("books#volumes");
        response.setTotalItems(1);
        response.setItems(Arrays.asList(book));

        // Verificar métodos de conveniencia
        assertTrue(response.hasResults());
        assertEquals(1, response.getResultCount());
        assertTrue(response.hasSingleResult());
        assertEquals("Java Programming", response.getFirstResult().getTitle());
        assertFalse(response.hasMoreResults());

        System.out.println("✅ GoogleBooksResponseDTO funciona correctamente");
        System.out.println("📊 Respuesta: " + response.toString());
    }

    @Test
    public void testEmptyResponse() {
        GoogleBooksResponseDTO response = new GoogleBooksResponseDTO();
        response.setTotalItems(0);

        assertFalse(response.hasResults());
        assertEquals(0, response.getResultCount());
        assertNull(response.getFirstResult());

        System.out.println("✅ Respuesta vacía manejada correctamente");
    }
}