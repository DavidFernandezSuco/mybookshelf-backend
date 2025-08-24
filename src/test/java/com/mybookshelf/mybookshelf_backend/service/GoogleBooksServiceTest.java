package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.client.GoogleBooksClient;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookVolumeInfoDTO;
import com.mybookshelf.mybookshelf_backend.mapper.GoogleBooksMapper;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.mybookshelf.mybookshelf_backend.model.Author;
import com.mybookshelf.mybookshelf_backend.model.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST DE INTEGRACIÓN PARA GoogleBooksService
 *
 * Siguiendo patrón de tus tests existentes con @SpringBootTest
 * Tests de integración reales - demuestra que toda la cadena funciona
 */
@SpringBootTest
public class GoogleBooksServiceTest {

    @Autowired
    private GoogleBooksService googleBooksService;

    @Autowired
    private GoogleBooksClient googleBooksClient;

    @Autowired
    private GoogleBooksMapper googleBooksMapper;

    @Autowired
    private BookService bookService;

    /**
     * TEST 1: Verificar que el servicio está configurado
     */
    @Test
    public void testServiceConfiguration() {
        assertNotNull(googleBooksService, "GoogleBooksService debe estar inyectado");
        assertTrue(googleBooksService.isFullyConfigured(), "GoogleBooksService debe estar completamente configurado");

        // Mostrar estadísticas para debug
        googleBooksService.showIntegrationStats();

        System.out.println("✅ GoogleBooksService configurado correctamente");
    }

    /**
     * TEST 2: Búsqueda básica funciona
     */
    @Test
    public void testSearchBooks() {
        String query = "java programming";
        List<GoogleBookDTO> books = googleBooksService.searchBooks(query);

        assertNotNull(books, "La lista de libros no debe ser null");
        assertFalse(books.isEmpty(), "Debería encontrar libros sobre Java programming");

        GoogleBookDTO firstBook = books.get(0);
        assertNotNull(firstBook.getId(), "El libro debe tener un ID");
        assertNotNull(firstBook.getTitle(), "El libro debe tener un título");

        System.out.println("✅ Búsqueda básica funciona");
        System.out.println("📚 Encontrados: " + books.size() + " libros para '" + query + "'");
        System.out.println("📖 Primer libro: " + firstBook.getTitle());
    }

    /**
     * TEST 3: Detección de duplicados funciona
     */
    @Test
    public void testDuplicateDetection() {
        // Crear GoogleBookDTO de prueba
        GoogleBookVolumeInfoDTO volumeInfo = new GoogleBookVolumeInfoDTO();
        volumeInfo.setTitle("Test Book That Does Not Exist 12345");
        volumeInfo.setAuthors(Arrays.asList("Test Author"));

        GoogleBookDTO testBook = new GoogleBookDTO();
        testBook.setId("test-id-12345");
        testBook.setVolumeInfo(volumeInfo);

        // Verificar que NO es duplicado (libro que no existe)
        boolean isDuplicate = googleBooksService.isDuplicate(testBook);
        assertFalse(isDuplicate, "Libro con título único no debe ser detectado como duplicado");

        System.out.println("✅ Detección de duplicados funciona");
        System.out.println("📚 Libro único correctamente identificado");
    }

    /**
     * TEST 4: Mapeo de GoogleBook a Book entity funciona
     */
    @Test
    public void testMappingToBookEntity() {
        // Crear GoogleBookDTO completo
        GoogleBookVolumeInfoDTO volumeInfo = new GoogleBookVolumeInfoDTO();
        volumeInfo.setTitle("Test Programming Book");
        volumeInfo.setAuthors(Arrays.asList("John Doe", "Jane Smith"));
        volumeInfo.setDescription("A comprehensive guide to programming");
        volumeInfo.setPageCount(450);
        volumeInfo.setPublisher("Test Publisher");

        GoogleBookDTO googleBook = new GoogleBookDTO();
        googleBook.setId("test-mapping-123");
        googleBook.setVolumeInfo(volumeInfo);

        // Mapear a Book entity
        Book book = googleBooksService.mapToBookEntity(googleBook, BookStatus.WISHLIST);

        // Verificar mapeo
        assertNotNull(book, "Book entity no debe ser null");
        assertEquals("Test Programming Book", book.getTitle(), "Título debe mapearse correctamente");
        assertEquals("A comprehensive guide to programming", book.getDescription(), "Descripción debe mapearse");
        assertEquals(450, book.getTotalPages(), "Páginas deben mapearse");
        assertEquals("Test Publisher", book.getPublisher(), "Publisher debe mapearse");
        assertEquals(BookStatus.WISHLIST, book.getStatus(), "Status debe establecerse");
        assertEquals(0, book.getCurrentPage(), "Current page debe ser 0");

        System.out.println("✅ Mapeo GoogleBook → Book funciona");
        System.out.println("📖 Título mapeado: " + book.getTitle());
        System.out.println("📊 Páginas: " + book.getTotalPages());
    }

    /**
     * TEST 5: Importación completa desde Google Books (ID REAL)
     */
    @Test
    public void testImportFromGoogleBooksWithRealId() {
        // ID real de "Clean Code" en Google Books
        String googleBooksId = "hjEFCAAAQBAJ"; // Clean Code by Robert C. Martin

        try {
            // Intentar importar libro real
            Book importedBook = googleBooksService.importFromGoogle(googleBooksId, BookStatus.WISHLIST);

            // Verificar que se importó correctamente
            assertNotNull(importedBook, "Libro importado no debe ser null");
            assertNotNull(importedBook.getId(), "Libro debe tener ID asignado por BD");
            assertTrue(importedBook.getTitle().toLowerCase().contains("clean"), "Debe ser Clean Code");
            assertEquals(BookStatus.WISHLIST, importedBook.getStatus(), "Status debe ser WISHLIST");
            assertNotNull(importedBook.getAuthors(), "Debe tener autores asignados");
            assertFalse(importedBook.getAuthors().isEmpty(), "Debe tener al menos un autor");

            System.out.println("✅ Importación completa exitosa");
            System.out.println("📖 Libro importado: " + importedBook.getTitle());
            System.out.println("👥 Autores: " + importedBook.getAuthors().size());
            System.out.println("🏷️ Géneros: " + importedBook.getGenres().size());

        } catch (RuntimeException e) {
            // Si ya existe, verificar mensaje de duplicado
            if (e.getMessage().contains("already exists")) {
                System.out.println("ℹ️ Libro ya existe - detección de duplicados funciona");
                System.out.println("📚 Mensaje: " + e.getMessage());
            } else {
                throw e; // Re-lanzar si es otro tipo de error
            }
        }
    }

    /**
     * TEST 6: Manejo de errores - ID inválido
     */
    @Test
    public void testErrorHandlingWithInvalidId() {
        String invalidId = "id-que-no-existe-12345";

        // Debe lanzar excepción para ID inválido
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> googleBooksService.getBookDetails(invalidId),
                "Debe lanzar excepción para ID inválido"
        );

        assertTrue(exception.getMessage().contains("not found") || exception.getMessage().contains("Failed"),
                "Mensaje debe indicar que no se encontró el libro");

        System.out.println("✅ Manejo de errores funciona");
        System.out.println("⚠️ Error esperado: " + exception.getMessage());
    }

    /**
     * TEST 7: Validación de parámetros
     */
    @Test
    public void testParameterValidation() {
        System.out.println("🔍 Testeando validación de parámetros...");

        // Query vacía debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksService.searchBooks("");
        }, "Debe lanzar excepción para query vacía");

        // Query null debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksService.searchBooks(null);
        }, "Debe lanzar excepción para query null");

        // GoogleBooksId vacío debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksService.getBookDetails("");
        }, "Debe lanzar excepción para ID vacío");

        System.out.println("✅ Validación de parámetros funciona correctamente");
    }
}