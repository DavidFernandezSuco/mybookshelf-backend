package com.mybookshelf.mybookshelf_backend.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO DE RESPUESTA PARA AUTOCOMPLETADO
 *
 * FASE 5: Respuesta para sugerencias de autocompletado inteligente
 *
 * Usado en: GET /api/books/autocomplete
 *
 * Proporciona sugerencias mientras el usuario escribe, combinando:
 * - Títulos de la biblioteca local (más relevantes)
 * - Sugerencias de Google Books (para descubrir nuevos libros)
 *
 * Optimizado para UX rápida con respuestas mínimas pero útiles.
 */
public class AutocompleteResponseDTO {

    // ========================================
    // CAMPOS PRINCIPALES
    // ========================================

    /**
     * Término de búsqueda parcial introducido por el usuario
     */
    private String query;

    /**
     * Lista de sugerencias de títulos de libros
     */
    private List<String> suggestions;

    /**
     * Fuente de cada sugerencia ("local" o "external")
     * Mismo tamaño que suggestions, índice por índice
     */
    private List<String> sources;

    /**
     * Número total de sugerencias devueltas
     */
    private Integer count;

    /**
     * Tiempo de respuesta en milisegundos (opcional, para debugging)
     */
    private Long responseTimeMs;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío para Jackson
     */
    public AutocompleteResponseDTO() {
        this.suggestions = new ArrayList<>();
        this.sources = new ArrayList<>();
        this.count = 0;
    }

    /**
     * Constructor con campos principales
     */
    public AutocompleteResponseDTO(String query, List<String> suggestions, List<String> sources) {
        this.query = query;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.sources = sources != null ? sources : new ArrayList<>();
        this.count = this.suggestions.size();
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.count = this.suggestions.size();
    }

    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) {
        this.sources = sources != null ? sources : new ArrayList<>();
    }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Añadir una sugerencia con su fuente
     */
    public void addSuggestion(String suggestion, String source) {
        if (suggestion != null && !suggestion.trim().isEmpty()) {
            this.suggestions.add(suggestion);
            this.sources.add(source != null ? source : "unknown");
            this.count = this.suggestions.size();
        }
    }

    /**
     * Verificar si hay sugerencias
     */
    public boolean hasSuggestions() {
        return suggestions != null && !suggestions.isEmpty();
    }

    /**
     * Contar sugerencias locales
     */
    public long getLocalSuggestionsCount() {
        return sources != null ? sources.stream().filter("local"::equals).count() : 0;
    }

    /**
     * Contar sugerencias externas
     */
    public long getExternalSuggestionsCount() {
        return sources != null ? sources.stream().filter("external"::equals).count() : 0;
    }

    /**
     * Verificar si la consulta es válida para autocompletado
     */
    public boolean isValidQuery() {
        return query != null && query.trim().length() >= 2;
    }

    /**
     * Limpiar sugerencias (útil para reset)
     */
    public void clearSuggestions() {
        this.suggestions.clear();
        this.sources.clear();
        this.count = 0;
    }
}
