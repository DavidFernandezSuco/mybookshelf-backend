package com.mybookshelf.mybookshelf_backend.model;

// IMPORTS: Librerías necesarias para JPA y validaciones
import jakarta.persistence.*;           // Anotaciones JPA
import jakarta.validation.constraints.*; // Validaciones
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE AUTHOR - Entidad para representar autores de libros
 *
 * ¿Qué representa?
 * - Un autor puede escribir múltiples libros
 * - Un libro puede tener múltiples autores (coautores)
 * - Relación Many-to-Many con Book
 *
 * RELACIÓN CON BOOK:
 * - Author ←→ Book (Many-to-Many)
 * - Se crea tabla intermedia "book_authors" automáticamente
 * - Un autor como "García Márquez" puede tener muchos libros
 * - Un libro como "Good Omens" puede tener múltiples autores (Pratchett & Gaiman)
 */
@Entity
@Table(name = "authors") // Tabla "authors" en la base de datos
public class Author {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    /**
     * ID - Identificador único del autor
     *
     * Mismo patrón que Book: auto-increment con GenerationType.IDENTITY
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // INFORMACIÓN PERSONAL DEL AUTOR
    // ========================================

    /**
     * FIRST_NAME - Nombre del autor
     *
     * @NotBlank - Campo obligatorio, no puede estar vacío
     * @Size(max = 100) - Máximo 100 caracteres
     *
     * Ejemplos: "Gabriel", "J.K.", "Stephen"
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * LAST_NAME - Apellido del autor
     *
     * También obligatorio para identificación completa
     *
     * Ejemplos: "García Márquez", "Rowling", "King"
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * BIOGRAPHY - Biografía del autor (OPCIONAL)
     *
     * Campo largo para descripción detallada del autor
     * No es obligatorio porque no siempre tenemos esta información
     */
    @Column(length = 1000)
    private String biography;

    /**
     * BIRTH_DATE - Fecha de nacimiento (OPCIONAL)
     *
     * LocalDate - Solo fecha, sin hora
     * Útil para mostrar edad, ordenar por época, etc.
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * NATIONALITY - Nacionalidad del autor (OPCIONAL)
     *
     * Ejemplos: "Colombian", "British", "American"
     * Útil para filtros y estadísticas por país
     */
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    @Column(length = 100)
    private String nationality;

    // ========================================
    // TIMESTAMPS AUTOMÁTICOS
    // ========================================

    /**
     * CREATED_AT - Cuándo se creó este autor en el sistema
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED_AT - Última modificación del autor
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================
    // RELACIONES JPA
    // ========================================

    /**
     * BOOKS - Libros escritos por este autor
     *
     * RELACIÓN MANY-TO-MANY CON BOOK:
     *
     * @ManyToMany - Un autor puede tener muchos libros, un libro puede tener muchos autores
     * mappedBy = "authors" - La relación está mapeada en Book.authors
     *
     * ¿Por qué mappedBy?
     * - En Book tenemos @JoinTable que define la tabla intermedia
     * - Author solo referencia esa definición, no la duplica
     * - Book es el "dueño" de la relación (owner side)
     * - Author es el "lado inverso" (inverse side)
     *
     * fetch = FetchType.LAZY - No cargar libros automáticamente
     * ¿Por qué LAZY? Performance - solo carga libros cuando los necesitamos
     */
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JPA
     */
    public Author() {}

    /**
     * Constructor con nombres - Para crear autores básicos
     */
    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor completo - Para casos más específicos
     */
    public Author(String firstName, String lastName, String nationality, LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.birthDate = birthDate;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Obtiene el nombre completo del autor
     *
     * @return String con formato "Nombre Apellido"
     *
     * Útil para mostrar en interfaces de usuario
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Obtiene el nombre en formato "Apellido, Nombre"
     *
     * @return String con formato "Apellido, Nombre"
     *
     * Útil para ordenamiento alfabético y referencias bibliográficas
     */
    public String getLastNameFirst() {
        return lastName + ", " + firstName;
    }

    /**
     * Cuenta cuántos libros ha escrito este autor
     *
     * @return int número de libros
     */
    public int getBookCount() {
        return books != null ? books.size() : 0;
    }

    /**
     * Verifica si el autor ha escrito algún libro
     *
     * @return true si tiene libros, false si no
     */
    public boolean hasBooks() {
        return books != null && !books.isEmpty();
    }

    // ========================================
    // MÉTODOS PARA GESTIONAR RELACIONES
    // ========================================

    /**
     * Añade un libro a este autor
     *
     * IMPORTANTE: Este método gestiona la relación bidireccional
     * - Añade el libro al Set de libros del autor
     * - Añade el autor al Set de autores del libro
     *
     * ¿Por qué es importante?
     * - JPA necesita que ambos lados de la relación estén sincronizados
     * - Si solo actualizamos un lado, puede haber inconsistencias
     */
    public void addBook(Book book) {
        if (book != null) {
            books.add(book);
            book.getAuthors().add(this);
        }
    }

    /**
     * Remueve un libro de este autor
     *
     * También gestiona la relación bidireccional
     */
    public void removeBook(Book book) {
        if (book != null) {
            books.remove(book);
            book.getAuthors().remove(this);
        }
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

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR DE JAVA
    // ========================================

    /**
     * toString() - Representación en String del autor
     */
    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", nationality='" + nationality + '\'' +
                ", bookCount=" + getBookCount() +
                '}';
    }

    /**
     * equals() - Dos autores son iguales si tienen el mismo ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return id != null && id.equals(author.id);
    }

    /**
     * hashCode() - Para usar Author en HashSet, HashMap, etc.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}