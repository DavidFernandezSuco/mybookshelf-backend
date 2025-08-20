package com.mybookshelf.mybookshelf_backend.model;

// IMPORTS: Librerías necesarias para JPA y validaciones
import jakarta.persistence.*;           // Anotaciones JPA
import jakarta.validation.constraints.*; // Validaciones
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE GENRE - Entidad para representar géneros/categorías de libros
 *
 * ¿Qué representa?
 * - Categorías o géneros literarios (Ficción, Terror, Romance, Ciencia Ficción, etc.)
 * - Un libro puede pertenecer a múltiples géneros
 * - Un género puede tener múltiples libros
 * - Relación Many-to-Many con Book
 *
 * EJEMPLOS DE GÉNEROS:
 * - "Ficción", "No Ficción", "Terror", "Romance"
 * - "Ciencia Ficción", "Fantasía", "Biografía"
 * - "Autoayuda", "Historia", "Tecnología"
 *
 * RELACIÓN CON BOOK:
 * - Genre ←→ Book (Many-to-Many)
 * - Se crea tabla intermedia "book_genres" automáticamente
 * - Un libro como "Dune" puede ser "Ciencia Ficción" + "Aventura"
 * - Un género como "Terror" puede tener miles de libros
 */
@Entity
@Table(name = "genres") // Tabla "genres" en la base de datos
public class Genre {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    /**
     * ID - Identificador único del género
     *
     * Mismo patrón que Book y Author: auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // INFORMACIÓN DEL GÉNERO
    // ========================================

    /**
     * NAME - Nombre del género (OBLIGATORIO Y ÚNICO)
     *
     * @NotBlank - Campo obligatorio, no puede estar vacío
     * @Size(max = 50) - Máximo 50 caracteres (géneros suelen ser cortos)
     * @Column(unique = true) - No pueden existir géneros duplicados
     *
     * Ejemplos: "Terror", "Ciencia Ficción", "Romance"
     *
     * ¿Por qué único?
     * - No queremos tener "Terror" y "terror" como géneros separados
     * - Facilita búsquedas y filtros
     * - Evita duplicados accidentales
     */
    @NotBlank(message = "Genre name is required")
    @Size(max = 50, message = "Genre name must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    /**
     * DESCRIPTION - Descripción del género (OPCIONAL)
     *
     * Para explicar qué tipo de libros incluye este género
     *
     * Ejemplos:
     * - "Terror": "Libros que buscan generar miedo, suspense y tensión"
     * - "Ciencia Ficción": "Narrativa basada en especulación científica y tecnológica"
     */
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(length = 200)
    private String description;

    // ========================================
    // TIMESTAMPS AUTOMÁTICOS
    // ========================================

    /**
     * CREATED_AT - Cuándo se creó este género en el sistema
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED_AT - Última modificación del género
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================
    // RELACIONES JPA
    // ========================================

    /**
     * BOOKS - Libros que pertenecen a este género
     *
     * RELACIÓN MANY-TO-MANY CON BOOK:
     *
     * @ManyToMany - Un género puede tener muchos libros, un libro puede tener muchos géneros
     * mappedBy = "genres" - La relación está mapeada en Book.genres
     *
     * ¿Por qué mappedBy?
     * - En Book tenemos @JoinTable que define la tabla intermedia "book_genres"
     * - Genre solo referencia esa definición, no la duplica
     * - Book es el "dueño" de la relación (owner side)
     * - Genre es el "lado inverso" (inverse side)
     *
     * fetch = FetchType.LAZY - No cargar libros automáticamente
     * ¿Por qué LAZY? Un género popular puede tener miles de libros
     */
    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JPA
     */
    public Genre() {}

    /**
     * Constructor con nombre - Para crear géneros básicos
     *
     * El nombre es lo único obligatorio para un género
     */
    public Genre(String name) {
        this.name = name;
    }

    /**
     * Constructor completo - Para casos más específicos
     */
    public Genre(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Cuenta cuántos libros pertenecen a este género
     *
     * @return int número de libros en este género
     *
     * Útil para estadísticas: "Terror: 25 libros"
     */
    public int getBookCount() {
        return books != null ? books.size() : 0;
    }

    /**
     * Verifica si el género tiene algún libro
     *
     * @return true si tiene libros, false si está vacío
     *
     * Útil para limpiar géneros sin uso
     */
    public boolean hasBooks() {
        return books != null && !books.isEmpty();
    }

    /**
     * Obtiene el nombre en formato capitalizado
     *
     * @return String con primera letra mayúscula
     *
     * Útil para mostrar consistencia en UI
     */
    public String getDisplayName() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    // ========================================
    // MÉTODOS PARA GESTIONAR RELACIONES
    // ========================================

    /**
     * Añade un libro a este género
     *
     * IMPORTANTE: Este método gestiona la relación bidireccional
     * - Añade el libro al Set de libros del género
     * - Añade el género al Set de géneros del libro
     *
     * ¿Por qué es importante?
     * - JPA necesita que ambos lados de la relación estén sincronizados
     * - Si solo actualizamos un lado, puede haber inconsistencias
     */
    public void addBook(Book book) {
        if (book != null) {
            books.add(book);
            book.getGenres().add(this);
        }
    }

    /**
     * Remueve un libro de este género
     *
     * También gestiona la relación bidireccional
     */
    public void removeBook(Book book) {
        if (book != null) {
            books.remove(book);
            book.getGenres().remove(this);
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
     * toString() - Representación en String del género
     */
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bookCount=" + getBookCount() +
                '}';
    }

    /**
     * equals() - Dos géneros son iguales si tienen el mismo ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre)) return false;
        Genre genre = (Genre) o;
        return id != null && id.equals(genre.id);
    }

    /**
     * hashCode() - Para usar Genre en HashSet, HashMap, etc.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}