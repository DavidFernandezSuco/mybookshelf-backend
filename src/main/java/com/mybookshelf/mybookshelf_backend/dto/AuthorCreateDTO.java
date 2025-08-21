package com.mybookshelf.mybookshelf_backend.dto;

// IMPORTS: Librerías necesarias para validaciones y DTOs
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * CLASE AuthorCreateDTO - DTO para crear nuevos autores
 *
 * ¿Para qué sirve este DTO?
 * - Recibir datos del frontend/API cuando crean un autor
 * - Validar que los datos son correctos ANTES de crear la entidad
 * - Controlar exactamente qué campos se pueden establecer al crear
 * - Evitar que se envíen campos que no deberían modificarse (como ID, timestamps)
 *
 * DIFERENCIAS CON AuthorDTO:
 * - AuthorDTO: Para ENVIAR datos (respuestas de API)
 * - AuthorCreateDTO: Para RECIBIR datos (peticiones de creación)
 *
 * CAMPOS INCLUIDOS:
 * - Solo campos que el usuario puede/debe establecer al crear
 * - Validaciones estrictas para cada campo
 * - Sin campos calculados ni timestamps
 * - Sin relaciones complejas (se manejan por separado)
 */
public class AuthorCreateDTO {

    // ========================================
    // INFORMACIÓN BÁSICA OBLIGATORIA
    // ========================================

    /**
     * FIRST_NAME - Nombre del autor (OBLIGATORIO)
     *
     * @NotBlank - No puede estar vacío
     * @Size - Entre 1 y 100 caracteres
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    /**
     * LAST_NAME - Apellido del autor (OBLIGATORIO)
     *
     * @NotBlank - No puede estar vacío
     * @Size - Entre 1 y 100 caracteres
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    // ========================================
    // INFORMACIÓN BIOGRÁFICA OPCIONAL
    // ========================================

    /**
     * BIOGRAPHY - Biografía del autor (OPCIONAL)
     *
     * @Size - Máximo 1000 caracteres
     */
    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    private String biography;

    /**
     * BIRTH_DATE - Fecha de nacimiento (OPCIONAL)
     *
     * @JsonFormat - Controla formato JSON: "yyyy-MM-dd"
     * @PastOrPresent - No puede ser fecha futura
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Birth date cannot be in the future")
    private LocalDate birthDate;

    /**
     * NATIONALITY - Nacionalidad del autor (OPCIONAL)
     *
     * @Size - Máximo 100 caracteres
     */
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JSON deserialization
     */
    public AuthorCreateDTO() {}

    /**
     * Constructor básico - Para crear DTOs rápidamente en tests
     */
    public AuthorCreateDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor completo - Para casos específicos
     */
    public AuthorCreateDTO(String firstName, String lastName, String nationality, LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.birthDate = birthDate;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Verifica si tiene información suficiente para crear el autor
     */
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty();
    }

    /**
     * Verifica si tiene información biográfica completa
     */
    public boolean hasCompleteBiography() {
        return biography != null && !biography.trim().isEmpty() &&
                birthDate != null &&
                nationality != null && !nationality.trim().isEmpty();
    }

    /**
     * Obtiene el nombre completo para logging
     */
    public String getFullName() {
        if (firstName == null || lastName == null) {
            return "Unknown Author";
        }
        return firstName.trim() + " " + lastName.trim();
    }

    /**
     * Obtiene una descripción corta para logging
     */
    public String getShortDescription() {
        StringBuilder desc = new StringBuilder("Author: '" + getFullName() + "'");

        if (nationality != null && !nationality.trim().isEmpty()) {
            desc.append(" (").append(nationality.trim()).append(")");
        }

        if (birthDate != null) {
            desc.append(" born ").append(birthDate);
        }

        return desc.toString();
    }

    /**
     * Verifica si es un autor contemporáneo (nacido en los últimos 100 años)
     */
    public boolean isContemporary() {
        if (birthDate == null) {
            return false; // No sabemos
        }
        LocalDate cutoff = LocalDate.now().minusYears(100);
        return birthDate.isAfter(cutoff);
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================

    @Override
    public String toString() {
        return "AuthorCreateDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", birthDate=" + birthDate +
                ", hasCompleteBio=" + hasCompleteBiography() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorCreateDTO)) return false;
        AuthorCreateDTO that = (AuthorCreateDTO) o;
        return firstName != null && firstName.equals(that.firstName) &&
                lastName != null && lastName.equals(that.lastName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(firstName, lastName);
    }
}