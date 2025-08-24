package com.mybookshelf.mybookshelf_backend.dto;

import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;

import java.util.List;

/**
 * DTO DE RESPUESTA PARA SUGERENCIAS DE CREACIÓN DE LIBROS
 *
 * FASE 5: Respuesta inteligente para ayudar al usuario antes de crear un libro
 *
 * Usado en: GET /api/books/suggestions
 *
 * Analiza si el libro que quiere crear:
 * - Ya existe exactamente en su biblioteca
 * - Tiene libros similares que podrían ser duplicados
 * - Tiene opciones disponibles en Google Books para importar
 *
 * Proporciona recomendaciones claras para mejorar la UX de creación.
 */
public class BookSuggestionResponseDTO {

    // ========================================
    // CAMPOS PRINCIPALES
    // ========================================

    /**
     * Información de la consulta original
     */
    private QueryInfo query;

    /**
     * Libro exacto encontrado en la biblioteca (null si no existe)
     */
    private BookDTO exactMatch;

    /**
     * Libros similares encontrados en la biblioteca
     */
    private List<BookDTO> similarBooks;

    /**
     * Sugerencias desde Google Books para importar
     */
    private List<GoogleBookDTO> googleSuggestions;

    /**
     * Recomendación para el usuario: "exists", "similar", "create", "error"
     */
    private String recommendation;

    /**
     * Mensaje descriptivo para mostrar al usuario
     */
    private String message;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío para Jackson
     */
    public BookSuggestionResponseDTO() {}

    /**
     * Constructor completo
     */
    public BookSuggestionResponseDTO(String title, String author, BookDTO exactMatch,
                                     List<BookDTO> similarBooks, List<GoogleBookDTO> googleSuggestions,
                                     String recommendation, String message) {
        this.query = new QueryInfo(title, author);
        this.exactMatch = exactMatch;
        this.similarBooks = similarBooks;
        this.googleSuggestions = googleSuggestions;
        this.recommendation = recommendation;
        this.message = message;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public QueryInfo getQuery() { return query; }
    public void setQuery(QueryInfo query) { this.query = query; }

    public BookDTO getExactMatch() { return exactMatch; }
    public void setExactMatch(BookDTO exactMatch) { this.exactMatch = exactMatch; }

    public List<BookDTO> getSimilarBooks() { return similarBooks; }
    public void setSimilarBooks(List<BookDTO> similarBooks) { this.similarBooks = similarBooks; }

    public List<GoogleBookDTO> getGoogleSuggestions() { return googleSuggestions; }
    public void setGoogleSuggestions(List<GoogleBookDTO> googleSuggestions) { this.googleSuggestions = googleSuggestions; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Verificar si existe una coincidencia exacta
     */
    public boolean hasExactMatch() {
        return exactMatch != null;
    }

    /**
     * Verificar si hay libros similares
     */
    public boolean hasSimilarBooks() {
        return similarBooks != null && !similarBooks.isEmpty();
    }

    /**
     * Verificar si hay sugerencias de Google Books
     */
    public boolean hasGoogleSuggestions() {
        return googleSuggestions != null && !googleSuggestions.isEmpty();
    }

    /**
     * Verificar si es seguro crear el libro
     */
    public boolean isSafeToCreate() {
        return "create".equals(recommendation);
    }

    /**
     * Contar el total de sugerencias disponibles
     */
    public int getTotalSuggestions() {
        int count = 0;
        if (hasExactMatch()) count++;
        if (hasSimilarBooks()) count += similarBooks.size();
        if (hasGoogleSuggestions()) count += googleSuggestions.size();
        return count;
    }

    // ========================================
    // CLASE INTERNA PARA INFORMACIÓN DE CONSULTA
    // ========================================

    /**
     * Información de la consulta original del usuario
     */
    public static class QueryInfo {

        private String title;
        private String author;

        public QueryInfo() {}

        public QueryInfo(String title, String author) {
            this.title = title;
            this.author = author;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        /**
         * Verificar si se proporcionó información del autor
         */
        public boolean hasAuthor() {
            return author != null && !author.trim().isEmpty();
        }

        /**
         * Obtener consulta completa para búsqueda
         */
        public String getFullQuery() {
            if (hasAuthor()) {
                return title + " " + author;
            }
            return title;
        }
    }

    // ========================================
    // CONSTANTS PARA RECOMENDACIONES
    // ========================================

    /**
     * Constantes para los tipos de recomendación
     */
    public static final class Recommendation {
        public static final String EXISTS = "exists";
        public static final String SIMILAR = "similar";
        public static final String CREATE = "create";
        public static final String ERROR = "error";
    }

    /**
     * Mensajes estándar para recomendaciones
     */
    public static final class Messages {
        public static final String EXISTS = "This book already exists in your library.";
        public static final String SIMILAR = "Similar books found in your library. Check if it's a duplicate.";
        public static final String CREATE = "This book is not in your library. Safe to create.";
        public static final String ERROR = "Unable to analyze suggestions at this time.";
    }
}
