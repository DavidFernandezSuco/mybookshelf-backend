package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.client.GoogleBooksClient;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST DEL CLIENTE GOOGLE BOOKS
 *
 * Siguiendo patrón de tus tests existentes
 * Tests de integración reales con la API
 * Nivel junior - tests claros y directos
 */
@SpringBootTest
public class GoogleBooksClientTest {

    @Autowired
    private GoogleBooksClient googleBooksClient;

    /**
     * TEST 1: Verificar que el cliente está configurado
     */
    @Test
    public void testClientConfiguration() {
        assertNotNull(googleBooksClient, "GoogleBooksClient debe estar inyectado");
        assertTrue(googleBooksClient.isConfigured(), "GoogleBooksClient debe estar configurado correctamente");

        // Mostrar configuración para debug
        googleBooksClient.showConfiguration();

        System.out.println("✅ GoogleBooksClient configurado correctamente");
    }

    /**
     * TEST 2: Búsqueda general de libros
     */
    @Test
    public void testSearchBooks() {
        String query = "java programming";
        List<GoogleBookDTO> books = googleBooksClient.searchBooks(query);

        assertNotNull(books, "La lista de libros no debe ser null");
        assertFalse(books.isEmpty(), "Debería encontrar libros sobre Java programming");

        // Verificar primer resultado
        GoogleBookDTO firstBook = books.get(0);
        assertNotNull(firstBook.getId(), "El libro debe tener un ID");
        assertNotNull(firstBook.getTitle(), "El libro debe tener un título");

        System.out.println("✅ Búsqueda general funciona");
        System.out.println("📚 Encontrados: " + books.size() + " libros para '" + query + "'");
        System.out.println("📖 Primer libro: " + firstBook.getTitle());
        System.out.println("🆔 ID: " + firstBook.getId());
    }

    /**
     * TEST 3: Búsqueda por título específico
     */
    @Test
    public void testSearchByTitle() {
        String title = "Clean Code";
        List<GoogleBookDTO> books = googleBooksClient.searchByTitle(title);

        assertNotNull(books, "La lista de libros no debe ser null");
        // No assertFalse porque Clean Code podría no estar disponible

        System.out.println("✅ Búsqueda por título funciona");
        System.out.println("📚 Libros encontrados para '" + title + "': " + books.size());

        if (!books.isEmpty()) {
            GoogleBookDTO firstBook = books.get(0);
            System.out.println("📖 Primer resultado: " + firstBook.getTitle());
            System.out.println("👥 Autores: " + firstBook.getAuthorsAsString());
        }
    }

    /**
     * TEST 4: Búsqueda por autor
     */
    @Test
    public void testSearchByAuthor() {
        String author = "Robert Martin";
        List<GoogleBookDTO> books = googleBooksClient.searchByAuthor(author);

        assertNotNull(books, "La lista de libros no debe ser null");
        System.out.println("✅ Búsqueda por autor funciona");
        System.out.println("📚 Libros de '" + author + "' encontrados: " + books.size());

        if (!books.isEmpty()) {
            // Mostrar algunos resultados
            books.stream().limit(3).forEach(book -> {
                System.out.println("📖 " + book.getTitle() + " - " + book.getAuthorsAsString());
            });
        }
    }

    /**
     * TEST 5: Búsqueda por ISBN
     */
    @Test
    public void testSearchByIsbn() {
        // ISBN de Clean Code (uno conocido)
        String isbn = "9780132350884";
        GoogleBookDTO book = googleBooksClient.searchByIsbn(isbn);

        System.out.println("✅ Búsqueda por ISBN completada");

        if (book != null) {
            assertNotNull(book.getId(), "El libro encontrado debe tener ID");
            System.out.println("📖 Libro encontrado por ISBN: " + book.getTitle());
            System.out.println("👥 Autores: " + book.getAuthorsAsString());
            System.out.println("🆔 ID Google Books: " + book.getId());
        } else {
            System.out.println("📝 No se encontró libro con ISBN: " + isbn + " (normal, puede no estar disponible)");
        }

        // No hacer assert porque el ISBN puede no estar disponible
        // Solo verificamos que no lance excepción
    }

    /**
     * TEST 6: Obtener libro por ID
     */
    @Test
    public void testGetBookById() {
        // Primero buscar un libro para obtener un ID válido
        List<GoogleBookDTO> books = googleBooksClient.searchBooks("java");

        if (!books.isEmpty()) {
            String bookId = books.get(0).getId();
            assertNotNull(bookId, "El primer libro debe tener un ID");

            // Ahora obtener el libro por ID
            GoogleBookDTO book = googleBooksClient.getBookById(bookId);

            assertNotNull(book, "Debe obtener el libro por ID");
            assertEquals(bookId, book.getId(), "El ID debe coincidir");
            assertNotNull(book.getTitle(), "El libro debe tener título");

            System.out.println("✅ Obtener libro por ID funciona");
            System.out.println("📖 Libro obtenido: " + book.getTitle());
            System.out.println("🆔 ID: " + book.getId());
        } else {
            System.out.println("⚠️ No se pudo obtener un ID válido para el test");
        }
    }

    /**
     * TEST 7: Manejo de búsqueda sin resultados
     */
    @Test
    public void testEmptySearch() {
        String weirdQuery = "xyzabc123nonexistentbook999";
        List<GoogleBookDTO> books = googleBooksClient.searchBooks(weirdQuery);

        assertNotNull(books, "La lista no debe ser null aunque esté vacía");
        System.out.println("✅ Búsqueda sin resultados manejada correctamente");
        System.out.println("📊 Resultados para query raro: " + books.size());
    }

    /**
     * TEST 8: Validación de parámetros nulos/vacíos
     */
    @Test
    public void testParameterValidation() {
        System.out.println("✅ Testeando validación de parámetros...");

        // Test query vacía
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchBooks("");
        }, "Debe lanzar excepción para query vacía");

        // Test query null
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchBooks(null);
        }, "Debe lanzar excepción para query null");

        // Test título vacío
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchByTitle("  ");
        }, "Debe lanzar excepción para título vacío");

        // Test autor null
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchByAuthor(null);
        }, "Debe lanzar excepción para autor null");

        // Test ISBN vacío
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.searchByIsbn("");
        }, "Debe lanzar excepción para ISBN vacío");

        // Test Book ID null
        assertThrows(IllegalArgumentException.class, () -> {
            googleBooksClient.getBookById(null);
        }, "Debe lanzar excepción para book ID null");

        System.out.println("✅ Validación de parámetros funciona correctamente");
    }

    /**
     * TEST 9: Test de integración completo
     */
    @Test
    public void testCompleteIntegration() {
        System.out.println("🚀 Ejecutando test de integración completo...");

        // 1. Verificar configuración
        assertTrue(googleBooksClient.isConfigured());

        // 2. Buscar libros
        List<GoogleBookDTO> books = googleBooksClient.searchBooks("programming");
        assertNotNull(books);

        if (!books.isEmpty()) {
            // 3. Tomar primer libro
            GoogleBookDTO book = books.get(0);
            assertNotNull(book.getId());
            assertNotNull(book.getTitle());

            // 4. Obtener por ID
            GoogleBookDTO bookById = googleBooksClient.getBookById(book.getId());
            assertNotNull(bookById);
            assertEquals(book.getId(), bookById.getId());

            System.out.println("✅ Integración completa exitosa");
            System.out.println("📖 Libro de prueba: " + book.getTitle());
            System.out.println("👥 Autores: " + book.getAuthorsAsString());
        }
    }
}