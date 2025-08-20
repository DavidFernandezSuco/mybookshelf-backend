package com.mybookshelf.mybookshelf_backend.model;

// IMPORTS: Librerías necesarias para la funcionalidad JPA y validaciones
import jakarta.persistence.*;           // Anotaciones JPA para mapeo de base de datos
import jakarta.validation.constraints.*; // Validaciones (NotBlank, Size, Min, etc.)
import org.hibernate.annotations.CreationTimestamp; // Timestamp automático de creación
import org.hibernate.annotations.UpdateTimestamp;   // Timestamp automático de actualización

import java.math.BigDecimal;  // Para números decimales precisos (rating)
import java.time.LocalDate;   // Para fechas (sin hora)
import java.time.LocalDateTime; // Para fechas con hora exacta
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE BOOK - Entidad Principal del Sistema
 *
 * Esta clase representa un libro en nuestra biblioteca personal.
 *
 * ¿Qué es una ENTIDAD JPA?
 * - Una clase Java que se mapea directamente con una tabla de base de datos
 * - Cada instancia de Book = una fila en la tabla "books"
 * - Cada atributo = una columna en la tabla
 *
 * ANOTACIONES PRINCIPALES:
 * @Entity - Le dice a JPA que esta clase es una entidad de base de datos
 * @Table - Especifica el nombre de la tabla (si no se pone, usa el nombre de la clase)
 */
@Entity
@Table(name = "books") // La tabla se llamará "books" en la base de datos
public class Book {

    // ========================================
    // CLAVE PRIMARIA (Primary Key)
    // ========================================

    /**
     * ID - Identificador único del libro
     *
     * @Id - Marca este campo como clave primaria
     * @GeneratedValue - JPA genera automáticamente el valor
     * GenerationType.IDENTITY - Usa auto-increment de la base de datos
     *
     * ¿Por qué Long? Permite millones de libros sin problemas
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // INFORMACIÓN BÁSICA DEL LIBRO
    // ========================================

    /**
     * TITLE - Título del libro (OBLIGATORIO)
     *
     * @NotBlank - No puede ser null, vacío o solo espacios
     * @Size(max = 500) - Máximo 500 caracteres
     * @Column(nullable = false) - Campo obligatorio en BD
     *
     * length = 500 - La columna en BD tendrá VARCHAR(500)
     */
    @NotBlank(message = "Title is required") // Mensaje personalizado de error
    @Size(max = 500, message = "Title must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * ISBN - Código único internacional del libro (OPCIONAL)
     *
     * @Size(max = 20) - Máximo 20 caracteres (ISBN-13 tiene 13 dígitos + guiones)
     * No es @NotBlank porque es opcional
     */
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Column(length = 20)
    private String isbn;

    /**
     * TOTAL_PAGES - Número total de páginas del libro
     *
     * @Min(value = 1) - Debe ser al menos 1 página
     * @Column(name = "total_pages") - En BD se llama "total_pages" (snake_case)
     *
     * ¿Por qué Integer y no int? Integer puede ser null, int no
     */
    @Min(value = 1, message = "Total pages must be at least 1")
    @Column(name = "total_pages")
    private Integer totalPages;

    /**
     * CURRENT_PAGE - Página actual de lectura
     *
     * @Min(value = 0) - No puede ser negativa
     * Valor por defecto = 0 (no hemos empezado)
     */
    @Min(value = 0, message = "Current page cannot be negative")
    @Column(name = "current_page")
    private Integer currentPage = 0; // Valor por defecto

    /**
     * STATUS - Estado actual del libro
     *
     * @Enumerated(EnumType.STRING) - Guarda el nombre del enum como String en BD
     * ¿Por qué STRING? Es más legible en la BD que números
     *
     * Alternativa: EnumType.ORDINAL guardaría 0,1,2,3... (menos legible)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // Campo obligatorio
    private BookStatus status = BookStatus.WISHLIST; // Valor por defecto

    // ========================================
    // INFORMACIÓN ADICIONAL DEL LIBRO
    // ========================================

    /**
     * PUBLISHED_DATE - Fecha de publicación del libro
     *
     * LocalDate - Solo fecha, sin hora (año-mes-día)
     * @Column(name = "published_date") - Nombre en BD
     */
    @Column(name = "published_date")
    private LocalDate publishedDate;

    /**
     * PUBLISHER - Editorial del libro
     */
    @Size(max = 200, message = "Publisher name must not exceed 200 characters")
    @Column(length = 200)
    private String publisher;

    /**
     * DESCRIPTION - Sinopsis o descripción del libro
     *
     * length = 2000 - Permite descripciones largas
     */
    @Column(length = 2000)
    private String description;

    // ========================================
    // INFORMACIÓN PERSONAL (Mi experiencia con el libro)
    // ========================================

    /**
     * PERSONAL_RATING - Mi calificación personal del libro
     *
     * BigDecimal - Para números decimales precisos (ej: 4.5)
     * @DecimalMin/Max - Valida rango 0.0 a 5.0
     * precision = 2, scale = 1 - Formato: X.X (ej: 4.5)
     */
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Column(name = "personal_rating", precision = 2, scale = 1)
    private BigDecimal personalRating;

    /**
     * PERSONAL_NOTES - Mis notas personales sobre el libro
     */
    @Column(name = "personal_notes", length = 1000)
    private String personalNotes;

    /**
     * START_DATE - Cuándo empecé a leer este libro
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * FINISH_DATE - Cuándo terminé de leer este libro
     */
    @Column(name = "finish_date")
    private LocalDate finishDate;

    // ========================================
// RELACIONES JPA
// ========================================

    /**
     * AUTHORS - Autores que escribieron este libro
     *
     * RELACIÓN MANY-TO-MANY:
     * - Un libro puede tener múltiples autores (ej: Good Omens por Pratchett & Gaiman)
     * - Un autor puede escribir múltiples libros
     *
     * @ManyToMany - Relación Many-to-Many
     * @JoinTable - Define la tabla intermedia "book_authors"
     * joinColumns - Clave foránea hacia Book (book_id)
     * inverseJoinColumns - Clave foránea hacia Author (author_id)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    // ========================================
    // TIMESTAMPS AUTOMÁTICOS
    // ========================================

    /**
     * CREATED_AT - Cuándo se creó este registro en la BD
     *
     * @CreationTimestamp - Hibernate pone automáticamente la fecha/hora actual
     * updatable = false - Una vez creado, no se puede cambiar
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED_AT - Cuándo se modificó por última vez este registro
     *
     * @UpdateTimestamp - Se actualiza automáticamente en cada modificación
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JPA
     *
     * JPA necesita poder crear instancias vacías de la entidad
     */
    public Book() {}

    /**
     * Constructor con título - Para crear libros rápidamente
     */
    public Book(String title) {
        this.title = title;
    }

    /**
     * Constructor completo - Para casos más específicos
     */
    public Book(String title, Integer totalPages, BookStatus status) {
        this.title = title;
        this.totalPages = totalPages;
        this.status = status;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD (Business Logic)
    // ========================================

    /**
     * Calcula el porcentaje de progreso de lectura
     *
     * @return Double entre 0.0 y 100.0
     *
     * Ejemplo: Si tengo 150 páginas leídas de 300 total = 50.0%
     */
    public Double getProgressPercentage() {
        // Validaciones para evitar errores
        if (totalPages == null || totalPages == 0 || currentPage == null) {
            return 0.0;
        }
        // Cálculo: (páginas leídas / páginas totales) * 100
        return (currentPage.doubleValue() / totalPages) * 100;
    }

    /**
     * Verifica si el libro está terminado
     *
     * @return true si status = FINISHED
     */
    public boolean isFinished() {
        return status == BookStatus.FINISHED;
    }

    /**
     * Verifica si el libro está siendo leído actualmente
     *
     * @return true si status = READING
     */
    public boolean isCurrentlyReading() {
        return status == BookStatus.READING;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    /**
     * Los getters y setters permiten acceso controlado a los atributos privados
     *
     * ¿Por qué no hacer los atributos públicos?
     * - Encapsulación: Controlamos cómo se accede/modifica la data
     * - Validación: Podríamos añadir lógica en los setters
     * - Flexibilidad: Podemos cambiar la implementación interna sin afectar código externo
     */

    // ID - Solo getter, no setter (se auto-genera)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // TITLE
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // ISBN
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    // TOTAL_PAGES
    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    // CURRENT_PAGE
    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    // STATUS
    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    // PUBLISHED_DATE
    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    // PUBLISHER
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    // DESCRIPTION
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // PERSONAL_RATING
    public BigDecimal getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(BigDecimal personalRating) {
        this.personalRating = personalRating;
    }

    // PERSONAL_NOTES
    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }

    // START_DATE
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // FINISH_DATE
    public LocalDate getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDate finishDate) {
        this.finishDate = finishDate;
    }

    // TIMESTAMPS (Solo getters - se manejan automáticamente)
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

    // AUTHORS
    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    /**
     * Añade un autor al libro manteniendo relación bidireccional
     */
    public void addAuthor(Author author) {
        if (author != null) {
            authors.add(author);
            author.getBooks().add(this);
        }
    }

    /**
     * Remueve un autor del libro manteniendo relación bidireccional
     */
    public void removeAuthor(Author author) {
        if (author != null) {
            authors.remove(author);
            author.getBooks().remove(this);
        }
    }

    // ========================================
    // MÉTODOS ESTÁNDAR DE JAVA
    // ========================================

    /**
     * toString() - Representación en String del objeto
     *
     * Útil para debugging y logging
     * Muestra información clave del libro
     */
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", status=" + status +
                ", progress=" + getProgressPercentage() + "%" +
                '}';
    }

    /**
     * equals() - Compara si dos objetos Book son iguales
     *
     * Dos libros son iguales si tienen el mismo ID
     * (ID es único en la base de datos)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Mismo objeto en memoria
        if (!(o instanceof Book)) return false; // No es un Book
        Book book = (Book) o;
        return id != null && id.equals(book.id); // Compara IDs
    }

    /**
     * hashCode() - Genera código hash para el objeto
     *
     * Necesario para usar Book en HashSet, HashMap, etc.
     * Usa la clase porque el ID puede ser null en objetos nuevos
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}