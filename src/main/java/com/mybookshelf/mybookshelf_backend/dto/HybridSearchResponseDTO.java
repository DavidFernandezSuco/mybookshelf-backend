package com.mybookshelf.mybookshelf_backend.dto;

import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;

import java.util.List;

/**
 * DTO DE RESPUESTA PARA BÚSQUEDA HÍBRIDA
 *
 * FASE 5: Respuesta unificada que combina resultados locales y externos
 *
 * Usado en: GET /api/books/search-hybrid
 *
 * Sigue el patrón profesional de tus otros DTOs:
 * - Campos privados con getters/setters
 * - Documentación clara
 * - Validaciones simples
 * - Consistente con arquitectura existente
 */
public class HybridSearchResponseDTO {

    // ========================================
    // CAMPOS PRINCIPALES
    // ========================================

    /**
     * Término de búsqueda original
     */
    private String query;

    /**
     * Resultados encontrados en tu biblioteca local
     */
    private List<BookDTO> localResults;

    /**
     * Resultados encontrados en Google Books
     */
    private List<GoogleBookDTO> externalResults;

    /**
     * Número total de resultados locales
     */
    private Integer totalLocal;

    /**
     * Número total de resultados externos
     */
    private Integer totalExternal;

    /**
     * Nota adicional (ej: "External search temporarily unavailable")
     */
    private String note;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío para Jackson
     */
    public HybridSearchResponseDTO() {}

    /**
     * Constructor con campos principales
     */
    public HybridSearchResponseDTO(String query, List<BookDTO> localResults, List<GoogleBookDTO> externalResults) {
        this.query = query;
        this.localResults = localResults;
        this.externalResults = externalResults;
        this.totalLocal = localResults != null ? localResults.size() : 0;
        this.totalExternal = externalResults != null ? externalResults.size() : 0;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<BookDTO> getLocalResults() { return localResults; }
    public void setLocalResults(List<BookDTO> localResults) {
        this.localResults = localResults;
        this.totalLocal = localResults != null ? localResults.size() : 0;
    }

    public List<GoogleBookDTO> getExternalResults() { return externalResults; }
    public void setExternalResults(List<GoogleBookDTO> externalResults) {
        this.externalResults = externalResults;
        this.totalExternal = externalResults != null ? externalResults.size() : 0;
    }

    public Integer getTotalLocal() { return totalLocal; }
    public void setTotalLocal(Integer totalLocal) { this.totalLocal = totalLocal; }

    public Integer getTotalExternal() { return totalExternal; }
    public void setTotalExternal(Integer totalExternal) { this.totalExternal = totalExternal; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Obtener el total combinado de resultados
     */
    public Integer getTotalResults() {
        return (totalLocal != null ? totalLocal : 0) + (totalExternal != null ? totalExternal : 0);
    }

    /**
     * Verificar si se encontraron resultados
     */
    public boolean hasResults() {
        return getTotalResults() > 0;
    }

    /**
     * Verificar si solo hay resultados locales
     */
    public boolean hasOnlyLocalResults() {
        return (totalLocal != null && totalLocal > 0) && (totalExternal == null || totalExternal == 0);
    }

    /**
     * Verificar si solo hay resultados externos
     */
    public boolean hasOnlyExternalResults() {
        return (totalExternal != null && totalExternal > 0) && (totalLocal == null || totalLocal == 0);
    }
}
