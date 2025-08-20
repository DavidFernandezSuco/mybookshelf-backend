package com.mybookshelf.mybookshelf_backend.model;

// IMPORTS: Librerías necesarias para JPA y validaciones
import jakarta.persistence.*;           // Anotaciones JPA
import jakarta.validation.constraints.*; // Validaciones
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * CLASE READINGSESSION - Entidad para registrar sesiones de lectura
 *
 * ¿Qué representa?
 * - Una sesión individual de lectura de un libro específico
 * - Registro de cuándo, cuánto tiempo y qué páginas se leyeron
 * - Estado emocional durante la lectura
 * - Notas específicas de esa sesión
 *
 * EJEMPLOS DE SESIONES:
 * - "Leí Dune de 20:00 a 21:30, páginas 50-75, me sentí emocionado"
 * - "15 minutos leyendo en el metro, páginas 102-105, algo distraído"
 * - "Sesión larga del domingo, 2 horas, páginas 200-240, muy concentrado"
 *
 * RELACIÓN CON BOOK:
 * - ReadingSession ←→ Book (Many-to-One)
 * - Un libro puede tener muchas sesiones de lectura
 * - Una sesión pertenece a UN libro específico
 * - Se crea columna "book_id" como foreign key
 */
@Entity
@Table(name = "reading_sessions") // Tabla "reading_sessions" en la base de datos
public class ReadingSession {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    /**
     * ID - Identificador único de la sesión de lectura
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // INFORMACIÓN TEMPORAL DE LA SESIÓN
    // ========================================

    /**
     * START_TIME - Cuándo empezó esta sesión de lectura (OBLIGATORIO)
     *
     * LocalDateTime - Fecha y hora exacta de inicio
     * @NotNull - Campo obligatorio
     *
     * Ejemplos: "2025-08-20T20:30:00" (20:30 del 20 de agosto)
     *
     * ¿Por qué es importante?
     * - Permite analizar patrones de lectura (¿cuándo leo más?)
     * - Calcular duración real de sesiones
     * - Estadísticas temporales (lecturas nocturnas, fines de semana, etc.)
     */
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * END_TIME - Cuándo terminó esta sesión de lectura (OPCIONAL)
     *
     * Puede ser null si:
     * - La sesión está en progreso
     * - Se olvidó registrar el final
     * - Se pausó indefinidamente
     *
     * Si existe, debe ser posterior a startTime
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // ========================================
    // INFORMACIÓN DE PROGRESO
    // ========================================

    /**
     * PAGES_READ - Número de páginas leídas en esta sesión
     *
     * @Min(value = 0) - No puede ser negativo
     * Valor por defecto = 0
     *
     * Ejemplos: 25, 3, 50
     *
     * ¿Cómo se calcula?
     * - Páginas al final - páginas al inicio
     * - O simplemente "leí 20 páginas"
     */
    @Min(value = 0, message = "Pages read cannot be negative")
    @Column(name = "pages_read")
    private Integer pagesRead = 0;

    // ========================================
    // INFORMACIÓN CUALITATIVA
    // ========================================

    /**
     * NOTES - Notas personales sobre esta sesión (OPCIONAL)
     *
     * Para registrar:
     * - Reflexiones sobre lo leído
     * - Partes interesantes o confusas
     * - Interrupciones o distracciones
     * - Estado del lugar de lectura
     *
     * Ejemplos:
     * - "Capítulo muy emocionante, no podía parar de leer"
     * - "Me costó concentrarme, mucho ruido en el café"
     * - "Plot twist increíble en la página 234"
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(length = 500)
    private String notes;

    /**
     * MOOD - Estado emocional durante la lectura
     *
     * Enum que representa cómo te sentiste mientras leías
     * Útil para:
     * - Analizar qué afecta tu experiencia de lectura
     * - Correlacionar estado de ánimo con productividad
     * - Recordar experiencias pasadas con libros
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_mood")
    private ReadingMood mood;

    // ========================================
    // TIMESTAMP AUTOMÁTICO
    // ========================================

    /**
     * CREATED_AT - Cuándo se registró esta sesión en el sistema
     *
     * Diferente de startTime:
     * - startTime = cuándo empezaste a leer
     * - createdAt = cuándo registraste la sesión en la app
     *
     * Útil para detectar sesiones registradas con retraso
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========================================
    // RELACIONES JPA
    // ========================================

    /**
     * BOOK - Libro al que pertenece esta sesión de lectura
     *
     * RELACIÓN MANY-TO-ONE CON BOOK:
     *
     * @ManyToOne - Muchas sesiones pueden pertenecer a un libro
     * fetch = FetchType.LAZY - No cargar el libro automáticamente
     * @JoinColumn - Crea columna "book_id" como foreign key
     *
     * ¿Por qué LAZY?
     * - Al listar sesiones, no siempre necesitamos los datos completos del libro
     * - Mejora performance cuando hay muchas sesiones
     * - Solo carga el libro cuando lo accedes explícitamente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JPA
     */
    public ReadingSession() {}

    /**
     * Constructor básico - Para empezar una sesión
     *
     * Solo requiere el libro y automáticamente pone la hora actual
     */
    public ReadingSession(Book book) {
        this.book = book;
        this.startTime = LocalDateTime.now();
    }

    /**
     * Constructor completo - Para registrar sesión completa
     */
    public ReadingSession(Book book, LocalDateTime startTime, LocalDateTime endTime,
                          Integer pagesRead, ReadingMood mood) {
        this.book = book;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pagesRead = pagesRead;
        this.mood = mood;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Calcula la duración de la sesión en minutos
     *
     * @return Long duración en minutos, null si no hay endTime
     *
     * Útil para estadísticas: "Promedio de lectura: 45 minutos por sesión"
     */
    public Long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Verifica si la sesión está en progreso (no terminada)
     *
     * @return true si startTime existe pero endTime es null
     */
    public boolean isInProgress() {
        return startTime != null && endTime == null;
    }

    /**
     * Verifica si la sesión está completada
     *
     * @return true si tiene tanto startTime como endTime
     */
    public boolean isCompleted() {
        return startTime != null && endTime != null;
    }

    /**
     * Termina la sesión actual poniendo endTime = ahora
     *
     * Útil para cerrar sesiones automáticamente
     */
    public void endSession() {
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    /**
     * Calcula páginas por minuto de esta sesión
     *
     * @return Double páginas por minuto, null si no se puede calcular
     *
     * Útil para medir velocidad de lectura
     */
    public Double getPagesPerMinute() {
        Long duration = getDurationInMinutes();
        if (duration == null || duration == 0 || pagesRead == null || pagesRead == 0) {
            return null;
        }
        return pagesRead.doubleValue() / duration;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReadingMood getMood() {
        return mood;
    }

    public void setMood(ReadingMood mood) {
        this.mood = mood;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR DE JAVA
    // ========================================

    /**
     * toString() - Representación en String de la sesión
     */
    @Override
    public String toString() {
        return "ReadingSession{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", pagesRead=" + pagesRead +
                ", mood=" + mood +
                ", duration=" + getDurationInMinutes() + " minutes" +
                '}';
    }

    /**
     * equals() - Dos sesiones son iguales si tienen el mismo ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadingSession)) return false;
        ReadingSession session = (ReadingSession) o;
        return id != null && id.equals(session.id);
    }

    /**
     * hashCode() - Para usar ReadingSession en HashSet, HashMap, etc.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}