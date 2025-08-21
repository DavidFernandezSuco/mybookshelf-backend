package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para validaciones y DTOs
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE BookCreateDTO - DTO para crear nuevos libros
 *
 * ¿Para qué sirve este DTO?
 * - Recibir datos del frontend/API cuando crean un libro
 * - Validar que los datos son correctos ANTES de crear la entidad
 * - Controlar exactamente qué campos se pueden establecer al crear
 * - Evitar que se envíen campos que no deberían modificarse (como ID, timestamps)
 *
 * DIFERENCIAS CON BookDTO:
 * - BookDTO: Para ENVIAR datos (respuestas de API)
 * - BookCreateDTO: Para RECIBIR datos (peticiones de creación)
 *
 * CAMPOS INCLUIDOS:
 * - Solo campos que el usuario puede/debe establecer al crear
 * - Validaciones estrictas para cada campo
 * - Referencias a autores y géneros (por IDs)
 * - Sin campos calculados ni timestamps
 */
public class BookCreateDTO {

    // ========================================
    // INFORMACIÓN BÁSICA OBLIGATORIA
    // ========================================

    /**
     * TITLE - Título del libro (OBLIGATORIO)
     *
     * @NotBlank - No puede estar vacío
     * @Size - Entre 1 y 500 caracteres
     */
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;

    /**
     * TOTAL_PAGES - Número total de páginas (OPCIONAL pero recomendado)
     *
     * @Min - Debe ser al menos 1 página si se especifica
     * @Max - Máximo razonable para evitar errores de tipeo
     */
    @Min(value = 1, message = "Total pages must be at least 1")
    @Max(value = 10000, message = "Total pages must not exceed 10,000")
    private Integer totalPages;

    // ========================================
    // INFORMACIÓN OPCIONAL DEL LIBRO
    // ========================================

    /**
     * ISBN - Código internacional del libro (OPCIONAL)
     *
     * @Size - Máximo 20 caracteres (ISBN-13 + guiones)
     * @Pattern - Formato básico de ISBN (opcional, comentado para simplicidad)
     */
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    // @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$).*",
    //          message = "ISBN format is invalid")
    private String isbn;

    /**
     * STATUS - Estado inicial del libro (OPCIONAL)
     *
     * Por defecto será WISHLIST si no se especifica
     */
    private BookStatus status = BookStatus.WISHLIST;

    /**
     * PUBLISHED_DATE - Fecha de publicación (OPCIONAL)
     *
     * @JsonFormat - Controla formato JSON: "yyyy-MM-dd"
     * @PastOrPresent - No puede ser fecha futura
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    /**
     * PUBLISHER - Editorial del libro (OPCIONAL)
     */
    @Size(max = 200, message = "Publisher name must not exceed 200 characters")
    private String publisher;

    /**
     * DESCRIPTION - Sinopsis o descripción (OPCIONAL)
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    // ========================================
    // INFORMACIÓN PERSONAL INICIAL
    // ========================================

    /**
     * PERSONAL_RATING - Calificación inicial (OPCIONAL)
     *
     * El usuario puede dar una calificación basada en expectativas
     * o si ya conoce el libro
     */
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Digits(integer = 1, fraction = 1, message = "Rating must have at most 1 decimal place")
    private BigDecimal personalRating;

    /**
     * PERSONAL_NOTES - Notas iniciales (OPCIONAL)
     *
     * El usuario puede añadir notas sobre por qué quiere leer el libro,
     * expectativas, etc.
     */
    @Size(max = 1000, message = "Personal notes must not exceed 1000 characters")
    private String personalNotes;

    // ========================================
    // RELACIONES (POR IDs)
    // ========================================

    /**
     * AUTHOR_IDS - IDs de autores que escribieron este libro
     *
     * Set<Long> porque:
     * - Un libro puede tener múltiples autores
     * - Solo necesitamos los IDs para crear las relaciones
     * - El service buscará los autores por estos IDs
     *
     * @Size - Máximo 10 autores (razonable)
     */
    @Size(max = 10, message = "A book cannot have more than 10 authors")
    private Set<Long> authorIds = new HashSet<>();

    /**
     * GENRE_IDS - IDs de géneros a los que pertenece este libro
     *
     * Similar a authorIds
     */
    @Size(max = 5, message = "A book cannot have more than 5 genres")
    private Set<Long> genreIds = new HashSet<>();

    // ========================================
    // CAMPOS DE PROGRESO INICIAL
    // ========================================

    /**
     * CURRENT_PAGE - Página actual si ya empezó a leer (OPCIONAL)
     *
     * Por defecto será 0 (no empezado)
     * Útil si el usuario está añadiendo un libro que ya empezó
     *
     * @Min - No puede ser negativa
     */
    @Min(value = 0, message = "Current page cannot be negative")
    private Integer currentPage = 0;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JSON deserialization
     */
    public BookCreateDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente en tests
     */
    public BookCreateDTO(String title) {
        this.title = title;
    }

    /**
     * Constructor completo - Para casos específicos
     */
    public BookCreateDTO(String title, Integer totalPages, BookStatus status) {
        this.title = title;
        this.totalPages = totalPages;
        this.status = status;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Verifica si tiene información suficiente para crear el libro
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Verifica si el libro se puede empezar a leer inmediatamente
     */
    public boolean isReadyToRead() {
        return totalPages != null && totalPages > 0;
    }

    /**
     * Verifica si tiene autores asignados
     */
    public boolean hasAuthors() {
        return authorIds != null && !authorIds.isEmpty();
    }

    /**
     * Verifica si tiene géneros asignados
     */
    public boolean hasGenres() {
        return genreIds != null && !genreIds.isEmpty();
    }

    /**
     * Obtiene una descripción corta para logging
     */
    public String getShortDescription() {
        StringBuilder desc = new StringBuilder("Book: '" + title + "'");

        if (totalPages != null) {
            desc.append(" (").append(totalPages).append(" pages)");
        }

        if (hasAuthors()) {
            desc.append(" with ").append(authorIds.size()).append(" author(s)");
        }

        return desc.toString();
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(BigDecimal personalRating) {
        this.personalRating = personalRating;
    }

    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }

    public Set<Long> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(Set<Long> authorIds) {
        this.authorIds = authorIds;
    }

    public Set<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(Set<Long> genreIds) {
        this.genreIds = genreIds;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================

    @Override
    public String toString() {
        return "BookCreateDTO{" +
                "title='" + title + '\'' +
                ", totalPages=" + totalPages +
                ", status=" + status +
                ", authorIds=" + authorIds.size() + " authors" +
                ", genreIds=" + genreIds.size() + " genres" +
                '}';
    }
}