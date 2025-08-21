package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para DTOs
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * CLASE GenreDTO - Data Transfer Object para Genre
 *
 * DTO simple para géneros literarios que se usa principalmente:
 * - En relaciones con BookDTO
 * - Para listados de géneros disponibles
 * - Para filtros y categorización
 * - Como respuesta en endpoints de géneros
 *
 * CARACTERÍSTICAS:
 * - DTO muy simple por naturaleza de Genre
 * - Incluye estadísticas básicas (número de libros)
 * - Optimizado para UI (nombres normalizados)
 * - Útil para filtros y autocompletado
 */
public class GenreDTO {

    // ========================================
    // INFORMACIÓN BÁSICA
    // ========================================

    /**
     * ID - Identificador único del género
     */
    private Long id;

    /**
     * NAME - Nombre del género
     *
     * Ejemplos: "Fiction", "Science Fiction", "Terror", "Romance"
     */
    private String name;

    /**
     * DESCRIPTION - Descripción del género (opcional)
     *
     * Explicación de qué tipo de libros incluye este género
     */
    private String description;

    // ========================================
    // CAMPOS CALCULADOS
    // ========================================

    /**
     * BOOK_COUNT - Número de libros en este género
     *
     * Campo calculado sin cargar la relación completa
     * Útil para estadísticas y ordenamiento por popularidad
     */
    private Long bookCount;

    /**
     * DISPLAY_NAME - Nombre formateado para mostrar
     *
     * Versión capitalizada y limpia del nombre
     */
    private String displayName;

    /**
     * IS_POPULAR - Si es un género popular (más de X libros)
     *
     * Campo calculado para destacar géneros principales
     */
    private Boolean isPopular;

    // ========================================
    // METADATA
    // ========================================

    /**
     * CREATED_AT - Cuándo se añadió el género al sistema
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * UPDATED_AT - Última modificación
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - Para JSON deserialization
     */
    public GenreDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente
     */
    public GenreDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        calculateDerivedFields();
    }

    /**
     * Constructor con descripción
     */
    public GenreDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        calculateDerivedFields();
    }

    /**
     * Constructor completo - Con estadísticas
     */
    public GenreDTO(Long id, String name, String description, Long bookCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bookCount = bookCount;
        calculateDerivedFields();
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Calcula campos derivados automáticamente
     */
    public void calculateDerivedFields() {
        // Calcular display name
        if (name != null) {
            this.displayName = capitalizeGenreName(name);
        }

        // Calcular popularidad (más de 5 libros es popular)
        if (bookCount != null) {
            this.isPopular = bookCount >= 5;
        } else {
            this.isPopular = false;
        }
    }

    /**
     * Capitaliza correctamente el nombre del género
     */
    private String capitalizeGenreName(String genreName) {
        if (genreName == null || genreName.trim().isEmpty()) {
            return genreName;
        }

        // Dividir por espacios y capitalizar cada palabra
        String[] words = genreName.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");

            String word = words[i];
            if (!word.isEmpty()) {
                // No capitalizar preposiciones pequeñas (excepto la primera palabra)
                if (i > 0 && isSmallPreposition(word)) {
                    result.append(word);
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        result.append(word.substring(1));
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Verifica si es una preposición pequeña que no se capitaliza
     */
    private boolean isSmallPreposition(String word) {
        String[] smallWords = {"of", "and", "the", "in", "on", "at", "to", "for", "with"};
        for (String smallWord : smallWords) {
            if (word.equalsIgnoreCase(smallWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene descripción corta para UI
     */
    public String getShortDescription() {
        if (description == null || description.length() <= 100) {
            return description;
        }
        return description.substring(0, 97) + "...";
    }

    /**
     * Obtiene el género con estadísticas para mostrar
     */
    public String getGenreWithStats() {
        StringBuilder result = new StringBuilder(getDisplayName());

        if (bookCount != null && bookCount > 0) {
            result.append(" (").append(bookCount).append(" book");
            if (bookCount != 1) {
                result.append("s");
            }
            result.append(")");
        }

        return result.toString();
    }

    /**
     * Verifica si el género tiene libros
     */
    public Boolean hasBooks() {
        return bookCount != null && bookCount > 0;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        calculateDerivedFields(); // Recalcular cuando cambie el nombre
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
        calculateDerivedFields(); // Recalcular popularidad
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================

    @Override
    public String toString() {
        return "GenreDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bookCount=" + bookCount +
                ", isPopular=" + isPopular +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenreDTO)) return false;
        GenreDTO genreDTO = (GenreDTO) o;
        return id != null && id.equals(genreDTO.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}