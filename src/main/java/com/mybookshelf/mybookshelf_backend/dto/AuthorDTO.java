package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para DTOs
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CLASE AuthorDTO - Data Transfer Object para Author
 *
 * DTO simplificado para autores que se usa principalmente:
 * - En relaciones con BookDTO
 * - Para listados de autores
 * - Para autocompletado en formularios
 * - Como respuesta en endpoints de autores
 *
 * CAMPOS INCLUIDOS:
 * - Información básica del autor
 * - Datos biográficos opcionales
 * - Campos calculados útiles para UI
 * - Estadísticas básicas sin cargar relaciones
 */
public class AuthorDTO {

    // ========================================
    // INFORMACIÓN BÁSICA
    // ========================================

    /**
     * ID - Identificador único del autor
     */
    private Long id;

    /**
     * FIRST_NAME - Nombre del autor
     */
    private String firstName;

    /**
     * LAST_NAME - Apellido del autor
     */
    private String lastName;

    /**
     * BIOGRAPHY - Biografía del autor (opcional)
     */
    private String biography;

    /**
     * BIRTH_DATE - Fecha de nacimiento (opcional)
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * NATIONALITY - Nacionalidad del autor (opcional)
     */
    private String nationality;

    // ========================================
    // CAMPOS CALCULADOS
    // ========================================

    /**
     * FULL_NAME - Nombre completo calculado
     *
     * Campo calculado: firstName + " " + lastName
     * Útil para mostrar en UI sin concatenar en frontend
     */
    private String fullName;

    /**
     * DISPLAY_NAME - Nombre en formato "Apellido, Nombre"
     *
     * Campo calculado para ordenamiento alfabético
     */
    private String displayName;

    /**
     * BOOK_COUNT - Número de libros escritos
     *
     * Campo calculado sin cargar la relación completa
     * Útil para estadísticas rápidas
     */
    private Long bookCount;

    /**
     * AGE - Edad calculada (si hay birthDate)
     *
     * Campo calculado basado en birthDate
     */
    private Integer age;

    // ========================================
    // METADATA
    // ========================================

    /**
     * CREATED_AT - Cuándo se añadió el autor al sistema
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
    public AuthorDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente
     */
    public AuthorDTO(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        calculateDerivedFields();
    }

    /**
     * Constructor completo - Con toda la información
     */
    public AuthorDTO(Long id, String firstName, String lastName, String nationality, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.birthDate = birthDate;
        calculateDerivedFields();
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Calcula campos derivados automáticamente
     */
    public void calculateDerivedFields() {
        // Calcular nombre completo
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
            this.displayName = lastName + ", " + firstName;
        }

        // Calcular edad si hay fecha de nacimiento
        if (birthDate != null) {
            this.age = LocalDate.now().getYear() - birthDate.getYear();
            // Ajustar si el cumpleaños no ha pasado este año
            if (LocalDate.now().getDayOfYear() < birthDate.getDayOfYear()) {
                this.age--;
            }
        }
    }

    /**
     * Obtiene biografía corta para UI
     */
    public String getShortBiography() {
        if (biography == null || biography.length() <= 150) {
            return biography;
        }
        return biography.substring(0, 147) + "...";
    }

    /**
     * Verifica si el autor tiene información biográfica completa
     */
    public Boolean hasCompleteBiography() {
        return biography != null && !biography.trim().isEmpty() &&
                birthDate != null &&
                nationality != null && !nationality.trim().isEmpty();
    }

    /**
     * Obtiene descripción del autor para mostrar en listas
     */
    public String getAuthorDescription() {
        StringBuilder description = new StringBuilder(getFullName());

        if (nationality != null && !nationality.trim().isEmpty()) {
            description.append(" (").append(nationality).append(")");
        }

        if (age != null) {
            description.append(" - ").append(age).append(" years old");
        }

        return description.toString();
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        calculateDerivedFields(); // Recalcular cuando cambie el nombre
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        calculateDerivedFields(); // Recalcular cuando cambie el apellido
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        calculateDerivedFields(); // Recalcular edad
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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
        return "AuthorDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", bookCount=" + bookCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorDTO)) return false;
        AuthorDTO authorDTO = (AuthorDTO) o;
        return id != null && id.equals(authorDTO.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}