package com.mybookshelf.mybookshelf_backend.client;

import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBooksResponseDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * CLASE GoogleBooksClient - Cliente para Google Books API
 *
 * SIGUIENDO PATRÓN DE TUS SERVICES EXISTENTES:
 * - @Service component
 * - Inyección de dependencias simple
 * - Métodos básicos y claros
 * - Manejo de errores nivel junior
 * - Sin complejidades innecesarias
 *
 * RESPONSABILIDADES:
 * - Hacer llamadas HTTP a Google Books API
 * - Convertir respuestas JSON a DTOs
 * - Manejar errores básicos
 * - Proporcionar métodos simples de búsqueda
 */
@Service
public class GoogleBooksClient {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    @Autowired
    private RestTemplate googleBooksRestTemplate;

    @Value("${google.books.api.key}")
    private String apiKey;

    @Value("${google.books.api.url}")
    private String apiUrl;

    @Value("${google.books.max.results:10}")
    private int maxResults;

    // ========================================
    // MÉTODOS PÚBLICOS DE BÚSQUEDA
    // ========================================

    /**
     * BUSCAR LIBROS POR TÉRMINO GENERAL
     *
     * Busca libros que contengan el término en título, autor o descripción
     *
     * @param query Término de búsqueda (ej: "clean code", "java programming")
     * @return Lista de libros encontrados
     */
    public List<GoogleBookDTO> searchBooks(String query) {
        try {
            // Validar entrada
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be empty");
            }

            // Construir URL de búsqueda
            String searchUrl = buildSearchUrl(query, maxResults);

            System.out.println("🔍 Buscando en Google Books: " + query);

            // Hacer llamada a la API
            GoogleBooksResponseDTO response = googleBooksRestTemplate
                    .getForObject(searchUrl, GoogleBooksResponseDTO.class);

            // Verificar respuesta
            if (response == null || !response.hasResults()) {
                System.out.println("📝 No se encontraron resultados para: " + query);
                return List.of(); // Lista vacía en lugar de null
            }

            System.out.println("📚 Encontrados " + response.getResultCount() + " libros para: " + query);
            return response.getItems();

        } catch (RestClientException e) {
            System.err.println("❌ Error calling Google Books API: " + e.getMessage());
            return List.of(); // Devolver lista vacía en caso de error
        }
    }

    /**
     * BUSCAR LIBRO POR ISBN
     *
     * Busca un libro específico por su ISBN
     *
     * @param isbn ISBN del libro (con o sin guiones)
     * @return Libro encontrado o null si no existe
     */
    public GoogleBookDTO searchByIsbn(String isbn) {
        // Validar ISBN ANTES del try-catch
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be empty");
        }

        try {
            // Limpiar ISBN (quitar guiones y espacios)
            String cleanIsbn = isbn.replaceAll("[\\s-]", "");

            System.out.println("🔍 Buscando por ISBN: " + cleanIsbn);

            // Buscar por ISBN específico
            String query = "isbn:" + cleanIsbn;
            List<GoogleBookDTO> results = searchBooks(query);

            // Devolver primer resultado (debería ser único)
            if (results.isEmpty()) {
                System.out.println("📝 No se encontró libro con ISBN: " + cleanIsbn);
                return null;
            } else {
                System.out.println("📖 Libro encontrado por ISBN: " + results.get(0).getTitle());
                return results.get(0);
            }

        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error searching by ISBN: " + e.getMessage());
            return null;
        }
    }

    /**
     * BUSCAR LIBROS POR TÍTULO
     *
     * Busca libros que contengan el título específico
     *
     * @param title Título a buscar
     * @return Lista de libros con ese título
     */
    public List<GoogleBookDTO> searchByTitle(String title) {
        // Validar título ANTES del try-catch
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        try {
            System.out.println("🔍 Buscando por título: " + title);

            // Buscar específicamente en títulos
            String query = "intitle:" + title;
            return searchBooks(query);

        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error searching by title: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * BUSCAR LIBROS POR AUTOR
     *
     * Busca libros de un autor específico
     *
     * @param author Nombre del autor
     * @return Lista de libros del autor
     */
    public List<GoogleBookDTO> searchByAuthor(String author) {
        // Validar autor ANTES del try-catch
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be empty");
        }

        try {
            System.out.println("🔍 Buscando por autor: " + author);

            // Buscar específicamente en autores
            String query = "inauthor:" + author;
            return searchBooks(query);

        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error searching by author: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * OBTENER DETALLES DE UN LIBRO POR ID
     *
     * Obtiene información completa de un libro usando su ID de Google Books
     *
     * @param googleBookId ID del libro en Google Books
     * @return Libro con información completa o null si no existe
     */
    public GoogleBookDTO getBookById(String googleBookId) {
        // Validar ID ANTES del try-catch
        if (googleBookId == null || googleBookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be empty");
        }

        try {
            System.out.println("🔍 Obteniendo libro por ID: " + googleBookId);

            // Construir URL para libro específico
            String bookUrl = apiUrl + "/" + googleBookId + "?key=" + apiKey;

            // Hacer llamada directa al libro
            GoogleBookDTO book = googleBooksRestTemplate.getForObject(bookUrl, GoogleBookDTO.class);

            if (book != null) {
                System.out.println("📖 Libro obtenido: " + book.getTitle());
            } else {
                System.out.println("📝 No se encontró libro con ID: " + googleBookId);
            }

            return book;

        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (RestClientException e) {
            System.err.println("❌ Error getting book by ID: " + e.getMessage());
            return null;
        }
    }

    // ========================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================

    /**
     * CONSTRUIR URL DE BÚSQUEDA
     *
     * Método privado para construir la URL con parámetros
     */
    private String buildSearchUrl(String query, int maxResults) {
        // ✅ AÑADIR VALIDACIÓN AQUÍ:
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        StringBuilder url = new StringBuilder(apiUrl);
        url.append("?q=").append(query.replace(" ", "+"));
        url.append("&key=").append(apiKey);
        url.append("&maxResults=").append(maxResults);

        return url.toString();
    }

    // ========================================
    // MÉTODO PARA TESTING/DEBUG
    // ========================================

    /**
     * VERIFICAR CONFIGURACIÓN
     *
     * Método para verificar que la configuración está correcta
     */
    public boolean isConfigured() {
        boolean configured = apiKey != null && !apiKey.trim().isEmpty() &&
                apiUrl != null && !apiUrl.trim().isEmpty() &&
                googleBooksRestTemplate != null;

        if (configured) {
            System.out.println("✅ GoogleBooksClient configurado correctamente");
        } else {
            System.err.println("❌ GoogleBooksClient NO configurado correctamente");
        }

        return configured;
    }

    // ========================================
    // MÉTODO DE CONVENIENCIA PARA DEBUGGING
    // ========================================

    /**
     * MOSTRAR INFORMACIÓN DE CONFIGURACIÓN
     *
     * Útil para debugging y verificar setup
     */
    public void showConfiguration() {
        System.out.println("📊 Configuración GoogleBooksClient:");
        System.out.println("   🌐 API URL: " + apiUrl);
        System.out.println("   🔑 API Key configurada: " + (apiKey != null && !apiKey.isEmpty() ? "Sí" : "No"));
        System.out.println("   📈 Max resultados: " + maxResults);
        System.out.println("   🔧 RestTemplate: " + (googleBooksRestTemplate != null ? "Configurado" : "No configurado"));
    }
}