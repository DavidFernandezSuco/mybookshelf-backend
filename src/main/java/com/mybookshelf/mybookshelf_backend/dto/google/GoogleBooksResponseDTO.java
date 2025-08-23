package com.mybookshelf.mybookshelf_backend.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * CLASE GoogleBooksResponseDTO - Respuesta completa de Google Books API
 *
 * SIGUIENDO PATRÓN DE TUS DTOs:
 * - Estructura simple y clara
 * - Solo lo necesario para manejar la respuesta
 * - Métodos de conveniencia básicos
 *
 * MAPEA LA RESPUESTA JSON COMPLETA:
 * {
 *   "kind": "books#volumes",
 *   "totalItems": 1234,
 *   "items": [...]
 * }
 */
public class GoogleBooksResponseDTO {

    // ========================================
    // INFORMACIÓN DE LA RESPUESTA
    // ========================================

    /**
     * KIND - Tipo de respuesta
     * Siempre será "books#volumes"
     */
    private String kind;

    /**
     * TOTAL_ITEMS - Total de libros encontrados
     * En JSON viene como "totalItems"
     */
    @JsonProperty("totalItems")
    private Integer totalItems;

    /**
     * ITEMS - Lista de libros encontrados
     * Array de GoogleBookDTO
     */
    private List<GoogleBookDTO> items;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - Para JSON deserialization
     */
    public GoogleBooksResponseDTO() {}

    /**
     * Constructor para tests
     */
    public GoogleBooksResponseDTO(Integer totalItems, List<GoogleBookDTO> items) {
        this.totalItems = totalItems;
        this.items = items;
    }

    // ========================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================

    /**
     * Verificar si hay resultados
     */
    public boolean hasResults() {
        return totalItems != null && totalItems > 0 && items != null && !items.isEmpty();
    }

    /**
     * Obtener número de resultados en esta página
     */
    public int getResultCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    /**
     * Verificar si la búsqueda encontró exactamente un resultado
     */
    public boolean hasSingleResult() {
        return hasResults() && items.size() == 1;
    }

    /**
     * Obtener primer resultado (si existe)
     */
    public GoogleBookDTO getFirstResult() {
        if (hasResults()) {
            return items.get(0);
        }
        return null;
    }

    /**
     * Verificar si hay más resultados disponibles
     * (totalItems mayor que los items actuales)
     */
    public boolean hasMoreResults() {
        return totalItems != null && items != null && totalItems > items.size();
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public List<GoogleBookDTO> getItems() {
        return items;
    }

    public void setItems(List<GoogleBookDTO> items) {
        this.items = items;
    }

    // ========================================
    // MÉTODO TOSTRING PARA DEBUG
    // ========================================

    @Override
    public String toString() {
        return "GoogleBooksResponseDTO{" +
                "kind='" + kind + '\'' +
                ", totalItems=" + totalItems +
                ", resultCount=" + getResultCount() +
                '}';
    }
}