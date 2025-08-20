package com.mybookshelf.mybookshelf_backend.repository;

// IMPORTS: Librerías necesarias para Spring Data JPA
import com.mybookshelf.mybookshelf_backend.model.Genre;  // Nuestra entidad Genre
import org.springframework.data.domain.Page;               // Para paginación
import org.springframework.data.domain.Pageable;           // Configuración de paginación
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base
import org.springframework.data.jpa.repository.Query;      // Para queries personalizadas
import org.springframework.data.repository.query.Param;    // Para parámetros en queries
import org.springframework.stereotype.Repository;          // Anotación de Spring

import java.util.List;
import java.util.Optional;

/**
 * INTERFAZ GenreRepository - "Gestor de Géneros Literarios"
 *
 * ¿Para qué sirve GenreRepository?
 * - Gestionar categorías/géneros de libros (Terror, Ciencia Ficción, Romance, etc.)
 * - Estadísticas de popularidad de géneros en tu biblioteca
 * - Búsquedas para filtros y categorización
 * - Análisis de preferencias de lectura
 *
 * CASOS DE USO TÍPICOS:
 * - "¿Cuál es mi género favorito?" (más libros)
 * - "Buscar género 'Ciencia Ficción'"
 * - "¿Qué géneros no tengo representados?"
 * - "Top 5 géneros más leídos"
 * - "Géneros para autocompletado en formularios"
 *
 * CARACTERÍSTICAS DE GÉNEROS:
 * - Pocos en cantidad (normalmente 10-50 géneros máximo)
 * - Se reutilizan mucho (un género para muchos libros)
 * - Útiles para filtros y estadísticas
 * - Importantes para recomendaciones futuras
 */
@Repository // Indica que es un componente de acceso a datos
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS POR NOMBRE
    // ========================================

    /**
     * BUSCAR GÉNERO POR NOMBRE EXACTO (INSENSIBLE A MAYÚSCULAS)
     *
     * Método automático: findByNameIgnoreCase
     * Spring traduce a: WHERE UPPER(name) = UPPER(?)
     *
     * Como name es único, devolvemos Optional<Genre>
     *
     * @param name Nombre exacto del género
     * @return Optional con el género si existe
     *
     * Ejemplos:
     * - findByNameIgnoreCase("terror") → Genre{name="Terror"}
     * - findByNameIgnoreCase("CIENCIA FICCIÓN") → Genre{name="Ciencia Ficción"}
     */
    Optional<Genre> findByNameIgnoreCase(String name);

    /**
     * BUSCAR GÉNEROS POR NOMBRE PARCIAL
     *
     * Para autocompletado y búsquedas flexibles
     *
     * @param name Parte del nombre del género
     * @return Lista de géneros que contienen el texto
     *
     * Ejemplo:
     * - findByNameContainingIgnoreCase("fic") → ["Ciencia Ficción", "Ficción Histórica"]
     */
    List<Genre> findByNameContainingIgnoreCase(String name);

    /**
     * TODOS LOS GÉNEROS ORDENADOS ALFABÉTICAMENTE
     *
     * Para mostrar en listas desplegables y menús
     *
     * @return Lista de todos los géneros ordenada por nombre
     */
    @Query("SELECT g FROM Genre g ORDER BY g.name")
    List<Genre> findAllOrderedByName();

    // ========================================
    // BÚSQUEDAS RELACIONADAS CON LIBROS
    // ========================================

    /**
     * GÉNEROS QUE TIENEN AL MENOS UN LIBRO
     *
     * Evita mostrar géneros "vacíos" sin libros asignados
     * Útil para filtros en UI - solo mostrar géneros relevantes
     *
     * @return Lista de géneros que tienen libros
     */
    @Query("SELECT g FROM Genre g WHERE EXISTS (SELECT 1 FROM Book b WHERE g MEMBER OF b.genres)")
    List<Genre> findGenresWithBooks();

    /**
     * GÉNEROS SIN LIBROS ASIGNADOS
     *
     * Para limpieza de datos - encontrar géneros huérfanos
     * Útil para mantenimiento de la base de datos
     *
     * @return Lista de géneros sin libros
     */
    @Query("SELECT g FROM Genre g WHERE NOT EXISTS (SELECT 1 FROM Book b WHERE g MEMBER OF b.genres)")
    List<Genre> findGenresWithoutBooks();

    /**
     * GÉNEROS CON CANTIDAD DE LIBROS
     *
     * Devuelve géneros ordenados por popularidad (más libros = más popular)
     *
     * @param pageable Para limitar resultados (ej: top 10)
     * @return Página de géneros ordenados por número de libros
     */
    @Query("SELECT g FROM Genre g " +
            "LEFT JOIN g.books b " +
            "GROUP BY g.id " +
            "ORDER BY COUNT(b) DESC")
    Page<Genre> findGenresByPopularity(Pageable pageable);

    /**
     * CONTAR LIBROS POR GÉNERO
     *
     * Para estadísticas específicas de un género
     *
     * @param genreId ID del género
     * @return Número de libros de ese género
     */
    @Query("SELECT COUNT(b) FROM Book b JOIN b.genres g WHERE g.id = :genreId")
    Long countBooksByGenre(@Param("genreId") Long genreId);

    // ========================================
    // ESTADÍSTICAS Y ANÁLISIS DE GÉNEROS
    // ========================================

    /**
     * ESTADÍSTICAS DE POPULARIDAD DE GÉNEROS
     *
     * Query más importante: agrupa géneros por número de libros
     * Perfecto para gráficos de barras, estadísticas de lectura
     *
     * @return Array de [nombre_género, cantidad_libros] ordenado por popularidad
     *
     * Ejemplo resultado:
     * [["Ciencia Ficción", 15], ["Terror", 12], ["Romance", 8], ["Historia", 5]]
     */
    @Query("SELECT g.name, COUNT(b) as bookCount " +
            "FROM Genre g " +
            "LEFT JOIN g.books b " +
            "GROUP BY g.id, g.name " +
            "ORDER BY COUNT(b) DESC, g.name")
    List<Object[]> getGenrePopularityStats();

    /**
     * TOP GÉNEROS MÁS POPULARES
     *
     * Solo géneros que tienen libros, ordenados por cantidad
     *
     * @param pageable Número máximo de géneros a devolver
     * @return Lista de géneros más populares
     */
    @Query("SELECT g FROM Genre g " +
            "JOIN g.books b " +
            "GROUP BY g.id " +
            "ORDER BY COUNT(b) DESC")
    Page<Genre> findTopGenres(Pageable pageable);

    /**
     * GÉNEROS MENOS REPRESENTADOS
     *
     * Encuentra géneros con pocos libros - útil para diversificar biblioteca
     *
     * @param maxBooks Máximo número de libros (ej: géneros con menos de 3 libros)
     * @return Lista de géneros poco representados
     */
    @Query("SELECT g FROM Genre g " +
            "LEFT JOIN g.books b " +
            "GROUP BY g.id " +
            "HAVING COUNT(b) <= :maxBooks " +
            "ORDER BY COUNT(b), g.name")
    List<Genre> findUnderrepresentedGenres(@Param("maxBooks") int maxBooks);

    /**
     * PROMEDIO DE LIBROS POR GÉNERO
     *
     * Estadística general: ¿cuántos libros tengo en promedio por género?
     *
     * @return Promedio de libros por género
     */
    @Query("SELECT AVG(bookCount) FROM (" +
            "SELECT COUNT(b) as bookCount " +
            "FROM Genre g " +
            "LEFT JOIN g.books b " +
            "GROUP BY g.id" +
            ")")
    Double getAverageBooksPerGenre();

    // ========================================
    // BÚSQUEDAS PARA UI Y AUTOCOMPLETADO
    // ========================================

    /**
     * GÉNEROS PARA AUTOCOMPLETADO
     *
     * Devuelve pocos resultados para campos de autocompletado
     * Priorizando géneros que tienen libros
     *
     * @param searchTerm Término parcial del nombre
     * @param pageable Limitación de resultados (ej: máximo 10)
     * @return Página de géneros que coinciden
     */
    @Query("SELECT g FROM Genre g " +
            "WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY " +
            "CASE WHEN EXISTS(SELECT 1 FROM Book b WHERE g MEMBER OF b.genres) THEN 0 ELSE 1 END, " +
            "g.name")
    Page<Genre> findForAutocomplete(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * GÉNEROS ACTIVOS (CON LIBROS) PARA FILTROS
     *
     * Solo géneros que tienen libros, ordenados alfabéticamente
     * Perfecto para filtros de búsqueda en UI
     *
     * @return Lista de géneros activos ordenados por nombre
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN g.books b " +
            "ORDER BY g.name")
    List<Genre> findActiveGenresForFilters();

    // ========================================
    // ANÁLISIS COMPARATIVO DE GÉNEROS
    // ========================================

    /**
     * DISTRIBUCIÓN DE GÉNEROS
     *
     * Análisis completo: nombre, cantidad, porcentaje del total
     * Útil para dashboards y reportes detallados
     *
     * @return Array de [nombre, cantidad, porcentaje] para cada género
     */
    @Query("SELECT g.name, " +
            "COUNT(b) as bookCount, " +
            "ROUND((COUNT(b) * 100.0 / (SELECT COUNT(book) FROM Book book)), 2) as percentage " +
            "FROM Genre g " +
            "LEFT JOIN g.books b " +
            "GROUP BY g.id, g.name " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> getGenreDistributionStats();

    /**
     * GÉNEROS SIMILARES POR NOMBRE
     *
     * Encuentra géneros con nombres parecidos para detectar posibles duplicados
     * Útil para limpieza de datos
     *
     * @param genreName Nombre del género a comparar
     * @return Lista de géneros con nombres similares
     */
    @Query("SELECT g FROM Genre g " +
            "WHERE LOWER(g.name) != LOWER(:genreName) " +
            "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :genreName, '%')) " +
            "OR LOWER(:genreName) LIKE LOWER(CONCAT('%', g.name, '%'))) " +
            "ORDER BY g.name")
    List<Genre> findSimilarGenres(@Param("genreName") String genreName);

    // ========================================
    // QUERIES PARA RECOMENDACIONES
    // ========================================

    /**
     * GÉNEROS RECOMENDADOS PARA DIVERSIFICAR
     *
     * Encuentra géneros con pocos libros para sugerir diversificación
     * Excluye géneros completamente vacíos
     *
     * @param maxBooks Umbral para considerar "poco representado"
     * @return Lista de géneros para diversificar biblioteca
     */
    @Query("SELECT g FROM Genre g " +
            "JOIN g.books b " +
            "GROUP BY g.id " +
            "HAVING COUNT(b) > 0 AND COUNT(b) <= :maxBooks " +
            "ORDER BY COUNT(b), g.name")
    List<Genre> findGenresForDiversification(@Param("maxBooks") int maxBooks);

    /**
     * GÉNEROS COMPLETAMENTE NUEVOS
     *
     * Géneros que existen pero no tienen ningún libro
     * Para sugerir exploración de nuevas categorías
     *
     * @return Lista de géneros sin explorar
     */
    @Query("SELECT g FROM Genre g " +
            "WHERE NOT EXISTS (SELECT 1 FROM Book b WHERE g MEMBER OF b.genres) " +
            "ORDER BY g.name")
    List<Genre> findUnexploredGenres();

    /**
     * VERIFICAR EXISTENCIA DE GÉNERO
     *
     * Para validaciones antes de crear nuevos géneros
     * Evita duplicados con diferentes mayúsculas/minúsculas
     *
     * @param name Nombre del género a verificar
     * @return true si el género ya existe
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
            "FROM Genre g WHERE LOWER(g.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /*
     * NOTAS IMPORTANTES SOBRE GenreRepository:
     *
     * 1. CARACTERÍSTICAS ESPECIALES DE GÉNEROS:
     *    - Pocos en cantidad (típicamente 10-50)
     *    - Se reutilizan mucho (Many-to-Many con Book)
     *    - Útiles para análisis estadístico
     *    - Importantes para filtros y categorización
     *
     * 2. CASOS DE USO PRINCIPALES:
     *    - Filtros de búsqueda en UI
     *    - Estadísticas de preferencias de lectura
     *    - Autocompletado en formularios
     *    - Recomendaciones de diversificación
     *    - Limpieza de datos (duplicados, huérfanos)
     *
     * 3. PERFORMANCE CONSIDERATIONS:
     *    - Los géneros son pocos, no necesitan paginación estricta
     *    - Cache bien porque cambian poco
     *    - DISTINCT importante en JOINs con books
     *    - Índice en name field para búsquedas
     *
     * 4. UI/UX APPLICATIONS:
     *    - Gráficos de barras con popularidad
     *    - Filtros de género en búsquedas
     *    - Tags/chips para categorización visual
     *    - Sugerencias de géneros para explorar
     *
     * 5. FUTURAS MEJORAS:
     *    - Jerarquía de géneros (Terror > Terror Psicológico)
     *    - Sinónimos de géneros (Sci-Fi = Ciencia Ficción)
     *    - Integración con APIs de clasificación externa
     *    - Machine learning para auto-categorización
     */
}