package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para validaciones y DTOs
import jakarta.validation.constraints.*;

/**
 * CLASE GenreCreateDTO - DTO para crear nuevos géneros
 *
 * ¿Para qué sirve este DTO?
 * - Recibir datos del frontend/API cuando crean un género
 * - Validar que los datos son correctos ANTES de crear la entidad
 * - Controlar exactamente qué campos se pueden establecer al crear
 * - Evitar que se envíen campos que no deberían modificarse (como ID, timestamps)
 *
 * DIFERENCIAS CON GenreDTO:
 * - GenreDTO: Para ENVIAR datos (respuestas de API)
 * - GenreCreateDTO: Para RECIBIR datos (peticiones de creación)
 *
 * CAMPOS INCLUIDOS:
 * - Solo campos que el usuario puede/debe establecer al crear
 * - Validaciones estrictas para cada campo
 * - Sin campos calculados ni timestamps
 * - Sin relaciones complejas (se manejan por separado)
 *
 * SIGUIENDO PATRÓN AuthorCreateDTO/BookCreateDTO
 */
public class GenreCreateDTO {

    // ========================================
    // INFORMACIÓN BÁSICA OBLIGATORIA
    // ========================================

    /**
     * NAME - Nombre del género (OBLIGATORIO Y ÚNICO)
     *
     * @NotBlank - No puede estar vacío
     * @Size - Entre 1 y 50 caracteres (igual que entity Genre)
     *
     * Ejemplos: "Fiction", "Science Fiction", "Terror", "Romance"
     */
    @NotBlank(message = "Genre name is required")
    @Size(min = 1, max = 50, message = "Genre name must be between 1 and 50 characters")
    private String name;

    /**
     * DESCRIPTION - Descripción del género (OPCIONAL)
     *
     * @Size - Máximo 200 caracteres (igual que entity Genre)
     *
     * Para explicar qué tipo de libros incluye este género
     *
     * Ejemplos:
     * - "Terror": "Libros que buscan generar miedo, suspense y tensión"
     * - "Ciencia Ficción": "Narrativa basada en especulación científica y tecnológica"
     */
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JSON deserialization
     */
    public GenreCreateDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente en tests
     */
    public GenreCreateDTO(String name) {
        this.name = name;
    }

    /**
     * Constructor completo - Para casos específicos
     */
    public GenreCreateDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Verifica si tiene información suficiente para crear el género
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Verifica si tiene descripción completa
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Obtiene el nombre normalizado para logging
     */
    public String getNormalizedName() {
        if (name == null) {
            return "Unknown Genre";
        }
        return name.trim();
    }

    /**
     * Obtiene una descripción corta para logging
     */
    public String getShortDescription() {
        StringBuilder desc = new StringBuilder("Genre: '" + getNormalizedName() + "'");

        if (hasDescription()) {
            String shortDesc = description.length() > 50 ?
                    description.substring(0, 47) + "..." : description;
            desc.append(" - ").append(shortDesc);
        }

        return desc.toString();
    }

    /**
     * Verifica si es un género común/estándar
     */
    public boolean isCommonGenre() {
        if (name == null) return false;

        String[] commonGenres = {
                "fiction", "non-fiction", "fantasy", "science fiction", "mystery",
                "thriller", "romance", "horror", "biography", "history",
                "self-help", "business", "technology", "travel", "poetry"
        };

        String lowerName = name.toLowerCase().trim();
        for (String common : commonGenres) {
            if (lowerName.equals(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sugiere categoría general basada en el nombre
     */
    public String suggestCategory() {
        if (name == null) return "Other";

        String lowerName = name.toLowerCase().trim();

        if (lowerName.contains("fiction") || lowerName.contains("fantasy") ||
                lowerName.contains("mystery") || lowerName.contains("thriller") ||
                lowerName.contains("romance") || lowerName.contains("horror")) {
            return "Literary Fiction";
        }

        if (lowerName.contains("science") || lowerName.contains("technology") ||
                lowerName.contains("programming") || lowerName.contains("computer")) {
            return "Technical";
        }

        if (lowerName.contains("business") || lowerName.contains("self-help") ||
                lowerName.contains("productivity") || lowerName.contains("management")) {
            return "Professional Development";
        }

        if (lowerName.contains("history") || lowerName.contains("biography") ||
                lowerName.contains("memoir") || lowerName.contains("politics")) {
            return "Non-Fiction";
        }

        return "General";
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================

    @Override
    public String toString() {
        return "GenreCreateDTO{" +
                "name='" + name + '\'' +
                ", hasDescription=" + hasDescription() +
                ", category='" + suggestCategory() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenreCreateDTO)) return false;
        GenreCreateDTO that = (GenreCreateDTO) o;
        return name != null && name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }
}