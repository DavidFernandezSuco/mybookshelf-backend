package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para DTOs
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE BookDTO - Data Transfer Object para Book
 *
 * ¿Qué es un DTO?
 * - Data Transfer Object: objeto para transferir datos entre capas
 * - Versión "limpia" de la entidad para exponer en la API
 * - Controla exactamente qué datos se envían al cliente
 * - Evita problemas de serialización y lazy loading
 *
 * ¿Por qué usar DTOs en lugar de entidades directas?
 * - SEGURIDAD: No expones campos internos sensibles
 * - PERFORMANCE: Solo los datos necesarios
 * - FLEXIBILIDAD: Puedes agregar campos calculados
 * - VERSIONADO: Cambios en entidad no rompen API
 * - LAZY LOADING: Evita errores de Hibernate
 *
 * CAMPOS INCLUIDOS:
 * - Información básica del libro
 * - Datos de progreso calculados
 * - Relaciones simplificadas (AuthorDTO, GenreDTO)
 * - Campos útiles para la UI
 */
public class BookDTO {

    // ========================================
    // INFORMACIÓN BÁSICA
    // ========================================

    /**
     * ID - Identificador único del libro
     */
    private Long id;

    /**
     * TITLE - Título del libro
     */
    private String title;

    /**
     * ISBN - Código internacional del libro
     */
    private String isbn;

    /**
     * TOTAL_PAGES - Número total de páginas
     */
    private Integer totalPages;

    /**
     * CURRENT_PAGE - Página actual de lectura
     */
    private Integer currentPage;

    /**
     * STATUS - Estado actual del libro
     */
    private BookStatus status;

    // ========================================
    // INFORMACIÓN ADICIONAL
    // ========================================

    /**
     * PUBLISHED_DATE - Fecha de publicación
     *
     * @JsonFormat - Controla cómo se serializa la fecha en JSON
     * pattern = "yyyy-MM-dd" → formato: "2024-08-20"
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    /**
     * PUBLISHER - Editorial del libro
     */
    private String publisher;

    /**
     * DESCRIPTION - Sinopsis o descripción
     */
    private String description;

    // ========================================
    // INFORMACIÓN PERSONAL
    // ========================================

    /**
     * PERSONAL_RATING - Calificación personal (0.0 - 5.0)
     */
    private BigDecimal personalRating;

    /**
     * PERSONAL_NOTES - Notas personales sobre el libro
     */
    private String personalNotes;

    /**
     * START_DATE - Cuándo empecé a leer
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * FINISH_DATE - Cuándo terminé de leer
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishDate;

    // ========================================
    // CAMPOS CALCULADOS (NO EN LA ENTIDAD)
    // ========================================

    /**
     * PROGRESS_PERCENTAGE - Porcentaje de progreso calculado
     *
     * Campo calculado dinámicamente:
     * - (currentPage / totalPages) * 100
     * - Útil para barras de progreso en UI
     * - No se guarda en BD, se calcula al crear el DTO
     */
    private Double progressPercentage;

    /**
     * PAGES_REMAINING - Páginas restantes para terminar
     *
     * Campo calculado: totalPages - currentPage
     */
    private Integer pagesRemaining;

    /**
     * IS_FINISHED - Boolean para saber si está terminado
     *
     * Campo calculado: status == FINISHED
     */
    private Boolean isFinished;

    /**
     * IS_CURRENTLY_READING - Boolean para saber si está leyendo
     *
     * Campo calculado: status == READING
     */
    private Boolean isCurrentlyReading;

    // ========================================
    // RELACIONES SIMPLIFICADAS
    // ========================================

    /**
     * AUTHORS - Autores del libro (como DTOs)
     *
     * Set<AuthorDTO> en lugar de Set<Author>:
     * - Evita lazy loading issues
     * - Solo incluye datos necesarios de autores
     * - Permite serialización JSON limpia
     */
    private Set<AuthorDTO> authors = new HashSet<>();

    /**
     * GENRES - Géneros del libro (como DTOs)
     *
     * Similar a authors, usa GenreDTO para evitar problemas
     */
    private Set<GenreDTO> genres = new HashSet<>();

    // ========================================
    // METADATA ÚTIL
    // ========================================

    /**
     * TOTAL_READING_SESSIONS - Número de sesiones de lectura
     *
     * Campo calculado: authors.size()
     * Útil para estadísticas sin cargar todas las sesiones
     */
    private Long totalReadingSessions;

    /**
     * CREATED_AT - Cuándo se añadió el libro al sistema
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
     * Constructor vacío - OBLIGATORIO para JSON deserialization
     */
    public BookDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente
     */
    public BookDTO(Long id, String title, BookStatus status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Calcula y actualiza campos derivados
     *
     * Método útil para el mapper - calcula todos los campos
     * que dependen de otros campos
     */
    public void calculateDerivedFields() {
        // Calcular progreso
        if (totalPages != null && totalPages > 0 && currentPage != null) {
            this.progressPercentage = (currentPage.doubleValue() / totalPages) * 100;
            this.pagesRemaining = totalPages - currentPage;
        } else {
            this.progressPercentage = 0.0;
            this.pagesRemaining = totalPages;
        }

        // Calcular estados booleanos
        this.isFinished = (status == BookStatus.FINISHED);
        this.isCurrentlyReading = (status == BookStatus.READING);
    }

    /**
     * Obtiene una descripción corta para UI
     */
    public String getShortDescription() {
        if (description == null || description.length() <= 100) {
            return description;
        }
        return description.substring(0, 97) + "...";
    }

    /**
     * Obtiene nombres de autores como String
     */
    public String getAuthorsAsString() {
        if (authors == null || authors.isEmpty()) {
            return "Unknown Author";
        }
        return authors.stream()
                .map(AuthorDTO::getFullName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Unknown Author");
    }

    /**
     * Obtiene géneros como String
     */
    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "No Genre";
        }
        return genres.stream()
                .map(GenreDTO::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Genre");
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDate finishDate) {
        this.finishDate = finishDate;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Integer getPagesRemaining() {
        return pagesRemaining;
    }

    public void setPagesRemaining(Integer pagesRemaining) {
        this.pagesRemaining = pagesRemaining;
    }

    public Boolean getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(Boolean isFinished) {
        this.isFinished = isFinished;
    }

    public Boolean getIsCurrentlyReading() {
        return isCurrentlyReading;
    }

    public void setIsCurrentlyReading(Boolean isCurrentlyReading) {
        this.isCurrentlyReading = isCurrentlyReading;
    }

    public Set<AuthorDTO> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<AuthorDTO> authors) {
        this.authors = authors;
    }

    public Set<GenreDTO> getGenres() {
        return genres;
    }

    public void setGenres(Set<GenreDTO> genres) {
        this.genres = genres;
    }

    public Long getTotalReadingSessions() {
        return totalReadingSessions;
    }

    public void setTotalReadingSessions(Long totalReadingSessions) {
        this.totalReadingSessions = totalReadingSessions;
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
        return "BookDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", progress=" + progressPercentage + "%" +
                ", authors=" + getAuthorsAsString() +
                '}';
    }
}