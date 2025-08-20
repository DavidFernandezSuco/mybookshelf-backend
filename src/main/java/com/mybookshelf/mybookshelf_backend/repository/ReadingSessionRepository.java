package com.mybookshelf.mybookshelf_backend.repository;

// IMPORTS: Librerías necesarias para Spring Data JPA
import com.mybookshelf.mybookshelf_backend.model.ReadingSession; // Nuestra entidad ReadingSession
import com.mybookshelf.mybookshelf_backend.model.ReadingMood;    // Enum ReadingMood
import org.springframework.data.domain.Page;                      // Para paginación
import org.springframework.data.domain.Pageable;                  // Configuración de paginación
import org.springframework.data.jpa.repository.JpaRepository;     // Interfaz base
import org.springframework.data.jpa.repository.Query;             // Para queries personalizadas
import org.springframework.data.repository.query.Param;           // Para parámetros en queries
import org.springframework.stereotype.Repository;                 // Anotación de Spring

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * INTERFAZ ReadingSessionRepository - "Analista de Patrones de Lectura"
 *
 * ¿Para qué sirve ReadingSessionRepository?
 * - Analizar patrones de lectura (cuándo, cuánto, cómo leo)
 * - Estadísticas de productividad (páginas por día, tiempo por sesión)
 * - Tracking de progreso de libros específicos
 * - Análisis emocional de sesiones de lectura
 * - Reportes temporales (diario, semanal, mensual, anual)
 *
 * DATOS QUE PODEMOS ANALIZAR:
 * - "¿Cuánto tiempo leo en promedio por sesión?"
 * - "¿Qué días de la semana leo más?"
 * - "¿En qué horarios soy más productivo?"
 * - "¿Cómo afecta mi estado de ánimo a la lectura?"
 * - "¿Cuántas páginas leo por mes?"
 * - "¿Qué libro me está tomando más tiempo?"
 *
 * FUNCIONALIDADES:
 * 1. Tracking de sesiones por libro
 * 2. Análisis temporal (por fecha, hora, duración)
 * 3. Estadísticas de productividad (páginas/tiempo)
 * 4. Análisis emocional (ReadingMood)
 * 5. Comparativas y tendencias
 * 6. Reportes para dashboard
 */
@Repository // Indica que es un componente de acceso a datos
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS POR LIBRO
    // ========================================

    /**
     * SESIONES DE UN LIBRO ESPECÍFICO
     *
     * Método automático: findByBookIdOrderByStartTimeDesc
     * Ordenadas por fecha más reciente primero
     *
     * @param bookId ID del libro
     * @return Lista de sesiones de ese libro, más recientes primero
     *
     * Uso: Ver historial de lectura de un libro específico
     */
    List<ReadingSession> findByBookIdOrderByStartTimeDesc(Long bookId);

    /**
     * ÚLTIMA SESIÓN DE UN LIBRO
     *
     * Para saber cuándo fue la última vez que leí un libro
     *
     * @param bookId ID del libro
     * @return La sesión más reciente de ese libro
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.book.id = :bookId " +
            "ORDER BY rs.startTime DESC LIMIT 1")
    ReadingSession findLastSessionByBook(@Param("bookId") Long bookId);

    /**
     * TOTAL DE PÁGINAS LEÍDAS DE UN LIBRO
     *
     * Suma todas las páginas leídas en todas las sesiones de un libro
     * Útil para verificar progreso real vs. progreso registrado en Book
     *
     * @param bookId ID del libro
     * @return Total de páginas leídas en todas las sesiones
     */
    @Query("SELECT COALESCE(SUM(rs.pagesRead), 0) FROM ReadingSession rs WHERE rs.book.id = :bookId")
    Integer getTotalPagesReadForBook(@Param("bookId") Long bookId);

    /**
     * SESIONES EN PROGRESO (SIN TERMINAR)
     *
     * Encuentra sesiones que tienen startTime pero no endTime
     * Útil para detectar sesiones "olvidadas" o en progreso
     *
     * @return Lista de sesiones no terminadas
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.endTime IS NULL ORDER BY rs.startTime DESC")
    List<ReadingSession> findSessionsInProgress();

    // ========================================
    // BÚSQUEDAS TEMPORALES
    // ========================================

    /**
     * SESIONES ENTRE FECHAS
     *
     * Para análisis por períodos: "¿Qué leí esta semana?"
     *
     * @param fromDate Fecha de inicio
     * @param toDate Fecha de fin
     * @return Lista de sesiones en ese rango temporal
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.startTime >= :fromDate AND rs.startTime <= :toDate " +
            "ORDER BY rs.startTime DESC")
    List<ReadingSession> findSessionsBetweenDates(@Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate);

    /**
     * SESIONES DE HOY
     *
     * Para dashboard diario: "¿Qué he leído hoy?"
     *
     * @param today Fecha de hoy
     * @return Lista de sesiones de hoy
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE DATE(rs.startTime) = :today " +
            "ORDER BY rs.startTime DESC")
    List<ReadingSession> findTodaySessions(@Param("today") LocalDate today);

    /**
     * SESIONES DE ESTA SEMANA
     *
     * @param weekStart Inicio de la semana
     * @param weekEnd Final de la semana
     * @return Lista de sesiones de esta semana
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.startTime >= :weekStart AND rs.startTime <= :weekEnd " +
            "ORDER BY rs.startTime DESC")
    List<ReadingSession> findThisWeekSessions(@Param("weekStart") LocalDateTime weekStart,
                                              @Param("weekEnd") LocalDateTime weekEnd);

    /**
     * SESIONES POR MES
     *
     * @param year Año
     * @param month Mes (1-12)
     * @return Lista de sesiones de ese mes
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE YEAR(rs.startTime) = :year AND MONTH(rs.startTime) = :month " +
            "ORDER BY rs.startTime DESC")
    List<ReadingSession> findSessionsByMonth(@Param("year") int year, @Param("month") int month);

    // ========================================
    // ANÁLISIS DE PRODUCTIVIDAD
    // ========================================

    /**
     * PROMEDIO DE PÁGINAS POR SESIÓN
     *
     * Estadística general de productividad
     *
     * @return Promedio de páginas leídas por sesión
     */
    @Query("SELECT AVG(rs.pagesRead) FROM ReadingSession rs WHERE rs.pagesRead > 0")
    Double getAveragePagesPerSession();

    /**
     * PROMEDIO DE DURACIÓN POR SESIÓN (EN MINUTOS)
     *
     * Solo sesiones completadas (con endTime)
     *
     * @return Promedio de duración en minutos
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, rs.startTime, rs.endTime)) " +
            "FROM ReadingSession rs WHERE rs.endTime IS NOT NULL")
    Double getAverageDurationInMinutes();

    /**
     * TOTAL DE PÁGINAS LEÍDAS EN UN PERÍODO
     *
     * Para reportes: "He leído 487 páginas este mes"
     *
     * @param fromDate Fecha de inicio
     * @param toDate Fecha de fin
     * @return Total de páginas leídas en el período
     */
    @Query("SELECT COALESCE(SUM(rs.pagesRead), 0) FROM ReadingSession rs " +
            "WHERE rs.startTime >= :fromDate AND rs.startTime <= :toDate")
    Integer getTotalPagesInPeriod(@Param("fromDate") LocalDateTime fromDate,
                                  @Param("toDate") LocalDateTime toDate);

    /**
     * TOTAL DE TIEMPO LEÍDO EN UN PERÍODO (EN MINUTOS)
     *
     * Solo sesiones completadas
     *
     * @param fromDate Fecha de inicio
     * @param toDate Fecha de fin
     * @return Total de minutos leídos en el período
     */
    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, rs.startTime, rs.endTime)), 0) " +
            "FROM ReadingSession rs " +
            "WHERE rs.startTime >= :fromDate AND rs.startTime <= :toDate AND rs.endTime IS NOT NULL")
    Long getTotalReadingTimeInPeriod(@Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate);

    /**
     * SESIÓN MÁS LARGA
     *
     * @return La sesión con mayor duración
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.endTime IS NOT NULL " +
            "ORDER BY TIMESTAMPDIFF(MINUTE, rs.startTime, rs.endTime) DESC LIMIT 1")
    ReadingSession findLongestSession();

    /**
     * SESIÓN CON MÁS PÁGINAS
     *
     * @return La sesión donde leí más páginas
     */
    @Query("SELECT rs FROM ReadingSession rs ORDER BY rs.pagesRead DESC LIMIT 1")
    ReadingSession findMostProductiveSession();

    // ========================================
    // ANÁLISIS POR ESTADO DE ÁNIMO
    // ========================================

    /**
     * SESIONES POR ESTADO DE ÁNIMO
     *
     * @param mood Estado de ánimo
     * @return Lista de sesiones con ese mood
     */
    List<ReadingSession> findByMoodOrderByStartTimeDesc(ReadingMood mood);

    /**
     * ESTADÍSTICAS DE MOOD
     *
     * Agrupa sesiones por estado de ánimo y cuenta cuántas hay de cada una
     *
     * @return Array de [mood, cantidad] para cada estado
     */
    @Query("SELECT rs.mood, COUNT(rs) FROM ReadingSession rs " +
            "WHERE rs.mood IS NOT NULL " +
            "GROUP BY rs.mood " +
            "ORDER BY COUNT(rs) DESC")
    List<Object[]> getMoodStatistics();

    /**
     * PROMEDIO DE PÁGINAS POR MOOD
     *
     * ¿Leo más páginas cuando estoy emocionado vs. cansado?
     *
     * @return Array de [mood, promedio_páginas] para cada estado
     */
    @Query("SELECT rs.mood, AVG(rs.pagesRead) FROM ReadingSession rs " +
            "WHERE rs.mood IS NOT NULL AND rs.pagesRead > 0 " +
            "GROUP BY rs.mood " +
            "ORDER BY AVG(rs.pagesRead) DESC")
    List<Object[]> getAveragePagesPerMood();

    /**
     * SESIONES POSITIVAS vs NEGATIVAS
     *
     * Compara productividad entre moods positivos y negativos
     *
     * @return Sesiones con moods positivos (EXCITED, RELAXED, FOCUSED)
     */
    @Query("SELECT rs FROM ReadingSession rs " +
            "WHERE rs.mood IN ('EXCITED', 'RELAXED', 'FOCUSED') " +
            "ORDER BY rs.startTime DESC")
    List<ReadingSession> findPositiveMoodSessions();

    // ========================================
    // ANÁLISIS TEMPORAL AVANZADO
    // ========================================

    /**
     * ESTADÍSTICAS POR DÍA DE LA SEMANA
     *
     * ¿Qué días leo más? (Lunes=1, Domingo=7)
     *
     * @return Array de [día_semana, cantidad_sesiones, promedio_páginas]
     */
    @Query("SELECT DAYOFWEEK(rs.startTime) as dayOfWeek, " +
            "COUNT(rs) as sessionCount, " +
            "AVG(rs.pagesRead) as avgPages " +
            "FROM ReadingSession rs " +
            "WHERE rs.pagesRead > 0 " +
            "GROUP BY DAYOFWEEK(rs.startTime) " +
            "ORDER BY dayOfWeek")
    List<Object[]> getWeekdayStatistics();

    /**
     * ESTADÍSTICAS POR HORA DEL DÍA
     *
     * ¿A qué horas soy más productivo?
     *
     * @return Array de [hora, cantidad_sesiones, promedio_páginas]
     */
    @Query("SELECT HOUR(rs.startTime) as hour, " +
            "COUNT(rs) as sessionCount, " +
            "AVG(rs.pagesRead) as avgPages " +
            "FROM ReadingSession rs " +
            "WHERE rs.pagesRead > 0 " +
            "GROUP BY HOUR(rs.startTime) " +
            "ORDER BY hour")
    List<Object[]> getHourlyStatistics();

    /**
     * TENDENCIA MENSUAL
     *
     * Progreso mes a mes para gráficos de tendencia
     *
     * @return Array de [año, mes, total_páginas, total_sesiones]
     */
    @Query("SELECT YEAR(rs.startTime) as year, " +
            "MONTH(rs.startTime) as month, " +
            "SUM(rs.pagesRead) as totalPages, " +
            "COUNT(rs) as sessionCount " +
            "FROM ReadingSession rs " +
            "GROUP BY YEAR(rs.startTime), MONTH(rs.startTime) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyTrends();

    // ========================================
    // REPORTES Y DASHBOARD
    // ========================================

    /**
     * RESUMEN DIARIO
     *
     * Para dashboard: sesiones, páginas y tiempo de hoy
     *
     * @param today Fecha de hoy
     * @return Array con [cantidad_sesiones, total_páginas, total_minutos]
     */
    @Query("SELECT COUNT(rs), " +
            "COALESCE(SUM(rs.pagesRead), 0), " +
            "COALESCE(SUM(TIMESTAMPDIFF(MINUTE, rs.startTime, rs.endTime)), 0) " +
            "FROM ReadingSession rs " +
            "WHERE DATE(rs.startTime) = :today")
    List<Object[]> getDailySummary(@Param("today") LocalDate today);

    /**
     * TOP DÍAS MÁS PRODUCTIVOS
     *
     * Días donde leí más páginas
     *
     * @param pageable Para limitar resultados
     * @return Lista de fechas con más páginas leídas
     */
    @Query("SELECT DATE(rs.startTime) as date, SUM(rs.pagesRead) as totalPages " +
            "FROM ReadingSession rs " +
            "WHERE rs.pagesRead > 0 " +
            "GROUP BY DATE(rs.startTime) " +
            "ORDER BY SUM(rs.pagesRead) DESC")
    Page<Object[]> findMostProductiveDays(Pageable pageable);

    /**
     * RACHA ACTUAL DE LECTURA
     *
     * ¿Cuántos días consecutivos he leído?
     * Query compleja que cuenta días consecutivos desde hoy hacia atrás
     *
     * @return Número de días consecutivos con al menos una sesión
     */
    @Query("SELECT COUNT(DISTINCT DATE(rs.startTime)) " +
            "FROM ReadingSession rs " +
            "WHERE DATE(rs.startTime) >= (SELECT MAX(consecutive_date) FROM (" +
            "SELECT DATE(rs2.startTime) as consecutive_date " +
            "FROM ReadingSession rs2 " +
            "WHERE DATE(rs2.startTime) <= CURRENT_DATE " +
            "GROUP BY DATE(rs2.startTime) " +
            "ORDER BY consecutive_date DESC" +
            ") AS recent_days)")
    Integer getCurrentReadingStreak();

    /**
     * LIBROS EN PROGRESO CON ÚLTIMA SESIÓN
     *
     * Para dashboard: libros que estoy leyendo con cuándo fue la última sesión
     *
     * @return Array de [libro_título, última_fecha_sesión, días_desde_última_sesión]
     */
    @Query("SELECT b.title, " +
            "MAX(rs.startTime), " +
            "DATEDIFF(CURRENT_DATE, DATE(MAX(rs.startTime))) as daysSince " +
            "FROM ReadingSession rs " +
            "JOIN rs.book b " +
            "WHERE b.status = 'READING' " +
            "GROUP BY b.id, b.title " +
            "ORDER BY daysSince")
    List<Object[]> getBooksInProgressWithLastSession();

    /*
     * NOTAS IMPORTANTES SOBRE ReadingSessionRepository:
     *
     * 1. ANÁLISIS TEMPORAL POTENTE:
     *    - Permite analizar patrones de lectura por día, hora, semana
     *    - Tendencias de productividad a lo largo del tiempo
     *    - Identificación de momentos más productivos
     *
     * 2. MÉTRICAS DE PRODUCTIVIDAD:
     *    - Páginas por sesión, tiempo por sesión
     *    - Velocidad de lectura (páginas por minuto)
     *    - Comparativas entre diferentes períodos
     *
     * 3. ANÁLISIS EMOCIONAL:
     *    - Correlación entre estado de ánimo y productividad
     *    - Identificación de condiciones óptimas de lectura
     *    - Patrones emocionales en la lectura
     *
     * 4. DASHBOARD Y REPORTES:
     *    - Métricas en tiempo real para motivación
     *    - Trends y progreso histórico
     *    - Gamificación (rachas, records personales)
     *
     * 5. PERFORMANCE CONSIDERATIONS:
     *    - Índices en startTime para queries temporales
     *    - Paginación en queries que pueden devolver muchos resultados
     *    - COALESCE para evitar nulls en SUMs
     *    - Cache en estadísticas que no cambian frecuentemente
     *
     * 6. FUTURAS MEJORAS:
     *    - Machine learning para predecir mejores momentos de lectura
     *    - Correlación con factores externos (clima, día laborable)
     *    - Recomendaciones de horarios óptimos
     *    - Alertas de inactividad (hace tiempo que no lees)
     */
}