package com.mybookshelf.mybookshelf_backend.repository;

// IMPORTS: Librerías necesarias para Spring Data JPA
import com.mybookshelf.mybookshelf_backend.model.Author;  // Nuestra entidad Author
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
 * INTERFAZ AuthorRepository - "Gestor de Autores"
 *
 * ¿Para qué sirve AuthorRepository?
 * - Buscar autores por nombre, apellido, nacionalidad
 * - Encontrar autores con más libros escritos
 * - Estadísticas de autores (más productivos, por época, etc.)
 * - Gestionar información biográfica de autores
 *
 * CASOS DE USO TÍPICOS:
 * - "Buscar todos los autores cuyo apellido contenga 'García'"
 * - "Encontrar el autor exacto 'Gabriel García Márquez'"
 * - "Listar autores nacidos en el siglo XX"
 * - "¿Qué autores colombianos tengo en mi biblioteca?"
 * - "Autores más prolíficos (que han escrito más libros)"
 *
 * FUNCIONALIDADES:
 * 1. Búsquedas por nombre y apellido
 * 2. Filtros por nacionalidad y fecha de nacimiento
 * 3. Búsquedas de autores con relación a sus libros
 * 4. Estadísticas de productividad de autores
 * 5. Búsquedas para autocompletado en UI
 */
@Repository // Indica que es un componente de acceso a datos
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS POR NOMBRE
    // ========================================

    /**
     * BUSCAR POR APELLIDO (INSENSIBLE A MAYÚSCULAS)
     *
     * Método automático: findByLastNameContainingIgnoreCase
     * Spring traduce a: WHERE UPPER(last_name) LIKE UPPER('%?%')
     *
     * @param lastName Parte del apellido a buscar
     * @return Lista de autores cuyo apellido contiene el texto
     *
     * Ejemplos:
     * - findByLastNameContainingIgnoreCase("garcía") → García Márquez, García Lorca
     * - findByLastNameContainingIgnoreCase("king") → Stephen King, Martin Luther King Jr.
     */
    List<Author> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * BUSCAR POR NOMBRE (INSENSIBLE A MAYÚSCULAS)
     *
     * Para cuando sabemos el nombre pero no el apellido
     *
     * @param firstName Parte del nombre a buscar
     * @return Lista de autores cuyo nombre contiene el texto
     *
     * Ejemplo:
     * - findByFirstNameContainingIgnoreCase("gabriel") → Gabriel García Márquez
     */
    List<Author> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * BUSCAR POR NOMBRE Y APELLIDO EXACTOS
     *
     * Para encontrar un autor específico cuando tenemos el nombre completo
     * Útil para evitar duplicados al añadir autores
     *
     * @param firstName Nombre exacto
     * @param lastName Apellido exacto
     * @return Optional con el autor si existe
     */
    @Query("SELECT a FROM Author a WHERE a.firstName = :firstName AND a.lastName = :lastName")
    Optional<Author> findByFullName(@Param("firstName") String firstName,
                                    @Param("lastName") String lastName);

    /**
     * BÚSQUEDA COMBINADA EN NOMBRE COMPLETO
     *
     * Busca en nombre O apellido - útil para barra de búsqueda general
     *
     * @param searchTerm Término a buscar en cualquier parte del nombre
     * @return Lista de autores que coinciden
     *
     * Ejemplo:
     * - searchByName("gabriel") → Gabriel García Márquez, Gabriel Celaya
     */
    @Query("SELECT a FROM Author a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Author> searchByName(@Param("searchTerm") String searchTerm);

    // ========================================
    // BÚSQUEDAS POR INFORMACIÓN BIOGRÁFICA
    // ========================================

    /**
     * BUSCAR POR NACIONALIDAD
     *
     * Para filtros como "Autores colombianos" o "Autores españoles"
     *
     * @param nationality Nacionalidad del autor
     * @return Lista de autores de esa nacionalidad
     */
    List<Author> findByNationalityIgnoreCase(String nationality);

    /**
     * BUSCAR POR RANGO DE FECHAS DE NACIMIENTO
     *
     * Para análisis por épocas: "Autores del siglo XX"
     *
     * @param startDate Fecha de nacimiento mínima
     * @param endDate Fecha de nacimiento máxima
     * @return Lista de autores nacidos en ese período
     */
    List<Author> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * AUTORES NACIDOS DESPUÉS DE UNA FECHA
     *
     * Para encontrar autores contemporáneos
     *
     * @param date Fecha límite
     * @return Lista de autores nacidos después de esa fecha
     */
    List<Author> findByBirthDateAfter(LocalDate date);

    /**
     * AUTORES CON BIOGRAFÍA
     *
     * Para mostrar solo autores que tienen información biográfica
     *
     * @return Lista de autores que tienen biografía registrada
     */
    @Query("SELECT a FROM Author a WHERE a.biography IS NOT NULL AND a.biography != ''")
    List<Author> findAuthorsWithBiography();

    // ========================================
    // BÚSQUEDAS RELACIONADAS CON LIBROS
    // ========================================

    /**
     * AUTORES QUE HAN ESCRITO AL MENOS UN LIBRO
     *
     * Evita mostrar autores "huérfanos" sin libros asignados
     * EXISTS verifica que exista al least una relación
     *
     * @return Lista de autores que tienen libros
     */
    @Query("SELECT a FROM Author a WHERE EXISTS (SELECT 1 FROM Book b WHERE a MEMBER OF b.authors)")
    List<Author> findAuthorsWithBooks();

    /**
     * AUTORES SIN LIBROS ASIGNADOS
     *
     * Para limpieza de datos - encontrar autores sin libros
     *
     * @return Lista de autores sin libros
     */
    @Query("SELECT a FROM Author a WHERE NOT EXISTS (SELECT 1 FROM Book b WHERE a MEMBER OF b.authors)")
    List<Author> findAuthorsWithoutBooks();

    /**
     * AUTORES MÁS PROLÍFICOS
     *
     * Ordena autores por número de libros escritos (de más a menos)
     *
     * @param pageable Para limitar resultados (ej: top 10)
     * @return Página de autores ordenados por productividad
     */
    @Query("SELECT a FROM Author a " +
            "LEFT JOIN a.books b " +
            "GROUP BY a.id " +
            "ORDER BY COUNT(b) DESC")
    Page<Author> findMostProlificAuthors(Pageable pageable);

    /**
     * CONTAR LIBROS POR AUTOR
     *
     * Para estadísticas específicas de un autor
     *
     * @param authorId ID del autor
     * @return Número de libros escritos por ese autor
     */
    @Query("SELECT COUNT(b) FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Long countBooksByAuthor(@Param("authorId") Long authorId);

    // ========================================
    // BÚSQUEDAS PARA AUTOCOMPLETADO Y UI
    // ========================================

    /**
     * AUTORES PARA AUTOCOMPLETADO
     *
     * Devuelve pocos resultados, ordenados alfabéticamente
     * Útil para campos de autocompletado en forms
     *
     * @param searchTerm Término parcial del nombre
     * @param pageable Limitación de resultados (ej: máximo 10)
     * @return Página de autores que coinciden
     */
    @Query("SELECT a FROM Author a WHERE " +
            "LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY a.lastName, a.firstName")
    Page<Author> findForAutocomplete(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * TODOS LOS AUTORES ORDENADOS ALFABÉTICAMENTE
     *
     * Para listas completas en UI, ordenados por apellido
     *
     * @return Lista de todos los autores ordenada alfabéticamente
     */
    @Query("SELECT a FROM Author a ORDER BY a.lastName, a.firstName")
    List<Author> findAllOrderedByName();

    // ========================================
    // ESTADÍSTICAS Y ANÁLISIS
    // ========================================

    /**
     * ESTADÍSTICAS POR NACIONALIDAD
     *
     * Agrupa autores por nacionalidad y cuenta cuántos hay de cada una
     * Útil para gráficos y estadísticas
     *
     * @return Array de [nacionalidad, cantidad] para cada país
     *
     * Ejemplo resultado: [["Colombian", 3], ["Spanish", 5], ["American", 12]]
     */
    @Query("SELECT a.nationality, COUNT(a) " +
            "FROM Author a " +
            "WHERE a.nationality IS NOT NULL " +
            "GROUP BY a.nationality " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> getAuthorStatsByNationality();

    /**
     * ESTADÍSTICAS POR DÉCADA DE NACIMIENTO
     *
     * Agrupa autores por década para análisis temporal
     *
     * @return Array de [década, cantidad] para cada período
     *
     * Ejemplo: [["1940s", 8], ["1950s", 12], ["1960s", 15]]
     */
    @Query("SELECT CONCAT(FLOOR(YEAR(a.birthDate)/10)*10, 's') as decade, COUNT(a) " +
            "FROM Author a " +
            "WHERE a.birthDate IS NOT NULL " +
            "GROUP BY FLOOR(YEAR(a.birthDate)/10) " +
            "ORDER BY decade")
    List<Object[]> getAuthorStatsByDecade();

    /**
     * PROMEDIO DE EDAD DE AUTORES
     *
     * Calcula edad promedio de autores vivos (aproximada)
     *
     * @return Edad promedio de autores en la biblioteca
     */
    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(a.birthDate)) " +
            "FROM Author a " +
            "WHERE a.birthDate IS NOT NULL")
    Double getAverageAuthorAge();

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    /**
     * AUTORES CONTEMPORÁNEOS
     *
     * Encuentra autores nacidos en los últimos X años
     * Para mostrar "autores modernos" o "autores actuales"
     *
     * @param yearsAgo Hace cuántos años (ej: 50 para últimos 50 años)
     * @return Lista de autores contemporáneos
     */
    @Query("SELECT a FROM Author a " +
            "WHERE a.birthDate > :cutoffDate " +
            "ORDER BY a.birthDate DESC")
    List<Author> findContemporaryAuthors(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * BUSCAR AUTORES POR PAÍS CON LIBROS
     *
     * Combina nacionalidad con existencia de libros
     *
     * @param nationality Nacionalidad del autor
     * @return Lista de autores de esa nacionalidad que tienen libros
     */
    @Query("SELECT DISTINCT a FROM Author a " +
            "JOIN a.books b " +
            "WHERE LOWER(a.nationality) = LOWER(:nationality) " +
            "ORDER BY a.lastName")
    List<Author> findByNationalityWithBooks(@Param("nationality") String nationality);

    /**
     * DUPLICADOS POTENCIALES
     *
     * Encuentra autores con nombres muy similares para detectar duplicados
     * Útil para limpieza de datos
     *
     * @param firstName Nombre a verificar
     * @param lastName Apellido a verificar
     * @return Lista de autores con nombres similares
     */
    @Query("SELECT a FROM Author a WHERE " +
            "(LOWER(a.firstName) = LOWER(:firstName) AND LOWER(a.lastName) != LOWER(:lastName)) OR " +
            "(LOWER(a.firstName) != LOWER(:firstName) AND LOWER(a.lastName) = LOWER(:lastName)) OR " +
            "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND " +
            " LOWER(a.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
    List<Author> findPotentialDuplicates(@Param("firstName") String firstName,
                                         @Param("lastName") String lastName);

    /*
     * NOTAS IMPORTANTES SOBRE AuthorRepository:
     *
     * 1. CASOS DE USO TÍPICOS:
     *    - Búsqueda de autores al añadir libros
     *    - Autocompletado en formularios
     *    - Estadísticas de biblioteca personal
     *    - Filtros por nacionalidad/época
     *    - Detección de duplicados
     *
     * 2. PERFORMANCE TIPS:
     *    - Usar paginación en búsquedas de autocompletado
     *    - Indexar firstName y lastName en producción
     *    - DISTINCT para evitar duplicados en JOINs
     *    - LEFT JOIN cuando la relación puede estar vacía
     *
     * 3. UI/UX CONSIDERATIONS:
     *    - findForAutocomplete para campos de búsqueda
     *    - findAllOrderedByName para dropdowns
     *    - searchByName para búsqueda general
     *    - Validar duplicados antes de guardar nuevos autores
     *
     * 4. FUTURAS MEJORAS:
     *    - Full-text search en biografías
     *    - Búsqueda fonética (soundex) para nombres similares
     *    - Integración con APIs externas (Wikipedia, etc.)
     *    - Caching para autores más consultados
     */
}