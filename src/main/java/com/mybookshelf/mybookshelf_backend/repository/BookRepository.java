package com.mybookshelf.mybookshelf_backend.repository;

// IMPORTS: Librerías necesarias para Spring Data JPA
import com.mybookshelf.mybookshelf_backend.model.Book;    // Nuestra entidad Book
import com.mybookshelf.mybookshelf_backend.model.BookStatus; // Enum BookStatus
import org.springframework.data.domain.Page;               // Para paginación
import org.springframework.data.domain.Pageable;           // Configuración de paginación
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base
import org.springframework.data.jpa.repository.Query;      // Para queries personalizadas
import org.springframework.data.repository.query.Param;    // Para parámetros en queries
import org.springframework.stereotype.Repository;          // Anotación de Spring

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * INTERFAZ BookRepository - "Bibliotecario Digital" para libros
 *
 * ¿Qué es una interfaz Repository?
 * - Define QUÉ operaciones podemos hacer (buscar, guardar, etc.)
 * - Spring Data JPA implementa automáticamente el CÓMO
 * - Solo escribimos la "firma" del método, Spring hace la magia
 *
 * ¿Por qué extender JpaRepository<Book, Long>?
 * - Book: Tipo de entidad que maneja
 * - Long: Tipo del ID de la entidad
 * - Heredamos métodos básicos: save(), findById(), findAll(), delete(), etc.
 *
 * FUNCIONALIDADES QUE VAMOS A IMPLEMENTAR:
 * 1. Búsquedas básicas por campo (status, título)
 * 2. Búsquedas con relaciones (por autor, por género)
 * 3. Estadísticas y conteos
 * 4. Búsquedas paginadas para performance
 * 5. Queries personalizadas complejas
 */
@Repository // Indica que es un componente de acceso a datos
public interface BookRepository extends JpaRepository<Book, Long> {

    // ========================================
    // MÉTODOS AUTOMÁTICOS (Query Methods)
    // ========================================

    /**
     * BUSCAR POR STATUS
     *
     * Nombre del método: findByStatus
     * Spring traduce automáticamente a: SELECT * FROM books WHERE status = ?
     *
     * @param status Estado del libro (WISHLIST, READING, FINISHED, etc.)
     * @return Lista de libros con ese status
     *
     * Ejemplos de uso:
     * - findByStatus(BookStatus.READING) → Libros que estoy leyendo
     * - findByStatus(BookStatus.FINISHED) → Libros terminados
     */
    List<Book> findByStatus(BookStatus status);

    /**
     * BUSCAR POR TÍTULO (INSENSIBLE A MAYÚSCULAS)
     *
     * Nombre del método: findByTitleContainingIgnoreCase
     * Spring traduce a: SELECT * FROM books WHERE UPPER(title) LIKE UPPER('%?%')
     *
     * - Containing: Busca si el texto está contenido en el título
     * - IgnoreCase: No importa si escribes "DUNE" o "dune"
     *
     * @param title Parte del título a buscar
     * @return Lista de libros cuyo título contiene el texto
     *
     * Ejemplo:
     * - findByTitleContainingIgnoreCase("harry") → Encuentra "Harry Potter y la Piedra Filosofal"
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * BUSCAR POR ISBN
     *
     * Como ISBN debería ser único, devolvemos Optional<Book>
     * Optional = "puede existir o no" → evita NullPointerException
     *
     * @param isbn Código ISBN del libro
     * @return Optional con el libro si existe, vacío si no existe
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * BUSCAR LIBROS TERMINADOS ESTE AÑO
     *
     * Nombre del método: findByStatusAndFinishDateBetween
     * Spring traduce a: WHERE status = ? AND finish_date BETWEEN ? AND ?
     *
     * @param status Debe ser FINISHED
     * @param startDate Inicio del año (ej: 2025-01-01)
     * @param endDate Final del año (ej: 2025-12-31)
     * @return Lista de libros terminados en el rango de fechas
     */
    List<Book> findByStatusAndFinishDateBetween(BookStatus status,
                                                LocalDate startDate,
                                                LocalDate endDate);

    /**
     * BUSCAR LIBROS POR NÚMERO DE PÁGINAS
     *
     * Ejemplo: Libros con más de 500 páginas
     *
     * @param pages Número mínimo de páginas
     * @return Lista de libros "grandes"
     */
    List<Book> findByTotalPagesGreaterThan(Integer pages);

    /**
     * BUSCAR CON MÚLTIPLES CRITERIOS
     *
     * Combinamos status Y búsqueda en título
     * Útil para filtros avanzados en la UI
     *
     * @param status Estado del libro
     * @param title Parte del título
     * @param pageable Configuración de paginación
     * @return Página de resultados
     */
    Page<Book> findByStatusAndTitleContainingIgnoreCase(BookStatus status,
                                                        String title,
                                                        Pageable pageable);

    // ========================================
    // QUERIES PERSONALIZADAS CON @Query
    // ========================================

    /**
     * BUSCAR LIBROS POR APELLIDO DEL AUTOR
     *
     * ¿Por qué @Query personalizada?
     * - Involucra relaciones (Book → Author)
     * - Spring no puede generar automáticamente queries tan complejas
     *
     * JPQL (Java Persistence Query Language):
     * - Similar a SQL pero trabaja con entidades Java
     * - 'b' es alias para Book, 'a' es alias para Author
     * - JOIN b.authors = conecta Book con sus autores
     *
     * @param authorLastName Apellido del autor a buscar
     * @return Lista de libros escritos por autores con ese apellido
     */
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.lastName = :authorLastName")
    List<Book> findByAuthorLastName(@Param("authorLastName") String authorLastName);

    /**
     * BUSCAR LIBROS POR NOMBRE DEL GÉNERO
     *
     * Otra query con relaciones: Book → Genre
     *
     * @param genreName Nombre del género (ej: "Terror", "Ciencia Ficción")
     * @return Lista de libros de ese género
     */
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.name = :genreName")
    List<Book> findByGenreName(@Param("genreName") String genreName);

    /**
     * BÚSQUEDA COMBINADA: AUTOR Y GÉNERO
     *
     * Query más compleja que combina ambas relaciones
     * Útil para: "Libros de terror escritos por Stephen King"
     *
     * @param authorLastName Apellido del autor
     * @param genreName Nombre del género
     * @return Lista de libros que cumplen ambos criterios
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "JOIN b.authors a " +
            "JOIN b.genres g " +
            "WHERE a.lastName = :authorLastName AND g.name = :genreName")
    List<Book> findByAuthorLastNameAndGenreName(@Param("authorLastName") String authorLastName,
                                                @Param("genreName") String genreName);

    // ========================================
    // QUERIES DE ESTADÍSTICAS Y CONTEOS
    // ========================================

    /**
     * CONTAR LIBROS POR STATUS
     *
     * COUNT en lugar de SELECT → devuelve número, no lista
     *
     * @param status Estado del libro
     * @return Número total de libros en ese estado
     *
     * Ejemplo: countByStatus(FINISHED) → 47 libros terminados
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = :status")
    Long countByStatus(@Param("status") BookStatus status);

    /**
     * CONTAR LIBROS TERMINADOS EN UN AÑO ESPECÍFICO
     *
     * YEAR(b.finishDate) extrae el año de una fecha
     * Útil para estadísticas anuales
     *
     * @param year Año a consultar (ej: 2025)
     * @return Número de libros terminados ese año
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE YEAR(b.finishDate) = :year")
    Long countBooksFinishedInYear(@Param("year") int year);

    /**
     * PROMEDIO DE PÁGINAS DE LIBROS TERMINADOS
     *
     * AVG calcula promedio
     * Solo considera libros terminados para estadística real
     *
     * @return Promedio de páginas de libros que he terminado
     */
    @Query("SELECT AVG(b.totalPages) FROM Book b WHERE b.status = 'FINISHED'")
    Double getAveragePagesOfFinishedBooks();

    /**
     * LIBROS CON MEJOR RATING
     *
     * ORDER BY ... DESC ordena de mayor a menor rating
     *
     * @param pageable Para limitar resultados (ej: top 10)
     * @return Página con libros mejor calificados
     */
    @Query("SELECT b FROM Book b WHERE b.personalRating IS NOT NULL " +
            "ORDER BY b.personalRating DESC")
    Page<Book> findTopRatedBooks(Pageable pageable);

    // ========================================
    // QUERIES DE ANÁLISIS TEMPORAL
    // ========================================

    /**
     * LIBROS LEÍDOS EN UN RANGO DE FECHAS
     *
     * Para análisis como: "¿Qué leí durante el verano?"
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de libros terminados en ese período
     */
    @Query("SELECT b FROM Book b WHERE b.finishDate BETWEEN :startDate AND :endDate")
    List<Book> findBooksFinishedBetweenDates(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * LIBROS SIN TERMINAR HACE TIEMPO
     *
     * Para encontrar libros abandonados o en pausa muy tiempo
     * currentDate = fecha actual
     *
     * @param cutoffDate Fecha límite (ej: hace 6 meses)
     * @return Lista de libros iniciados pero no terminados desde esa fecha
     */
    @Query("SELECT b FROM Book b WHERE b.startDate < :cutoffDate " +
            "AND b.status IN ('READING', 'ON_HOLD') " +
            "ORDER BY b.startDate ASC")
    List<Book> findAbandonedBooks(@Param("cutoffDate") LocalDate cutoffDate);

    // ========================================
    // QUERIES PARA DASHBOARD Y ESTADÍSTICAS
    // ========================================

    /**
     * RESUMEN DE LECTURA POR AÑO
     *
     * Query compleja que agrupa por año y cuenta libros
     * Devuelve Object[] porque son múltiples columnas
     *
     * @return Array de [año, cantidad] para cada año
     *
     * Ejemplo resultado: [[2023, 25], [2024, 31], [2025, 12]]
     */
    @Query("SELECT YEAR(b.finishDate) as year, COUNT(b) as count " +
            "FROM Book b WHERE b.finishDate IS NOT NULL " +
            "GROUP BY YEAR(b.finishDate) " +
            "ORDER BY year DESC")
    List<Object[]> getReadingStatsByYear();

    /**
     * PROGRESO DE LECTURA ACTUAL
     *
     * Solo libros que estoy leyendo actualmente
     * Útil para mostrar "libros en progreso" en dashboard
     *
     * @return Lista de libros con status READING
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'READING' " +
            "ORDER BY b.updatedAt DESC")
    List<Book> getCurrentlyReadingBooks();

    // ========================================
    // MÉTODOS DE BÚSQUEDA AVANZADA
    // ========================================

    /**
     * BÚSQUEDA GENERAL (FULL TEXT SEARCH SIMPLE)
     *
     * Busca en título, descripción y notas personales
     * Para barra de búsqueda general en la UI
     *
     * @param searchTerm Término a buscar
     * @return Lista de libros que contienen el término
     */
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.personalNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);

    /**
     * LIBROS RECOMENDADOS PARA LEER
     *
     * Lógica: libros en WISHLIST, ordenados por rating (si existe)
     * o por fecha de creación (los más recientes primero)
     *
     * @param pageable Para limitar resultados
     * @return Página de libros recomendados
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'WISHLIST' " +
            "ORDER BY " +
            "CASE WHEN b.personalRating IS NOT NULL THEN b.personalRating ELSE 0 END DESC, " +
            "b.createdAt DESC")
    Page<Book> findRecommendedBooks(Pageable pageable);

    /*
     * NOTAS IMPORTANTES SOBRE REPOSITORIES:
     *
     * 1. NAMING CONVENTIONS:
     *    - findBy... → SELECT WHERE
     *    - countBy... → SELECT COUNT WHERE
     *    - deleteBy... → DELETE WHERE
     *    - existsBy... → SELECT EXISTS WHERE
     *
     * 2. QUERY KEYWORDS:
     *    - Containing → LIKE %valor%
     *    - StartingWith → LIKE valor%
     *    - EndingWith → LIKE %valor
     *    - IgnoreCase → UPPER/LOWER comparison
     *    - OrderBy... → ORDER BY
     *    - GreaterThan, LessThan → > <
     *    - Between → BETWEEN
     *
     * 3. PERFORMANCE TIPS:
     *    - Usa paginación para listas grandes
     *    - Indices en columnas frecuentemente buscadas
     *    - LAZY loading para relaciones
     *    - COUNT en lugar de size() en listas
     *
     * 4. TESTING:
     *    - Cada método debería tener test unitario
     *    - Usar @DataJpaTest para tests de repository
     *    - Probar casos edge: listas vacías, parámetros null
     */
}