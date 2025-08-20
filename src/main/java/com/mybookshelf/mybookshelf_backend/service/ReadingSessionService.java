package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.model.Book;           // Entidad Book
import com.mybookshelf.mybookshelf_backend.model.ReadingSession; // Entidad ReadingSession
import com.mybookshelf.mybookshelf_backend.model.ReadingMood;     // Enum ReadingMood
import com.mybookshelf.mybookshelf_backend.repository.BookRepository; // Para verificar libros
import com.mybookshelf.mybookshelf_backend.repository.ReadingSessionRepository; // Repository principal
import org.springframework.stereotype.Service;                   // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * CLASE ReadingSessionService - "Gestor de Sesiones de Lectura"
 *
 * ¿Para qué sirve ReadingSessionService?
 * - Gestionar sesiones individuales de lectura
 * - Calcular tiempo total de lectura y velocidad
 * - Validar consistencia temporal de sesiones
 * - Generar estadísticas básicas de productividad
 *
 * CASOS DE USO PRINCIPALES:
 * - "Crear sesión de lectura" - Al empezar a leer
 * - "Terminar sesión" - Al parar de leer con páginas leídas
 * - "Obtener sesiones por libro" - Historial de lectura
 * - "Calcular estadísticas" - Tiempo total, páginas, etc.
 *
 * LÓGICA DE NEGOCIO ESPECIAL:
 * - Validación de tiempos lógicos (fin después de inicio)
 * - Cálculo automático de duración
 * - Detección de sesiones anómalamente largas
 * - Integración con progreso de libro
 *
 * VERSIÓN SIMPLIFICADA:
 * Solo usa métodos que ya existen en ReadingSessionRepository
 * para evitar errores de compilación
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Todas las operaciones serán transaccionales por defecto
public class ReadingSessionService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    /**
     * REPOSITORIES: Acceso a datos
     *
     * sessionRepository - Gestión de sesiones de lectura
     * bookRepository - Verificación de libros existentes
     */
    private final ReadingSessionRepository sessionRepository;
    private final BookRepository bookRepository;

    /**
     * CONSTRUCTOR: Inyección de dependencias
     *
     * Spring inyecta automáticamente los repositories
     * Constructor injection es la forma recomendada
     */
    public ReadingSessionService(ReadingSessionRepository sessionRepository,
                                 BookRepository bookRepository) {
        this.sessionRepository = sessionRepository;
        this.bookRepository = bookRepository;
    }

    // ========================================
    // OPERACIONES CRUD CON LÓGICA DE NEGOCIO
    // ========================================

    /**
     * CREAR NUEVA SESIÓN DE LECTURA
     *
     * Lógica de negocio aplicada:
     * 1. Validar que el libro existe
     * 2. Establecer tiempos por defecto si no se proporcionan
     * 3. Validar consistencia temporal
     * 4. Validar páginas leídas lógicas
     *
     * @param bookId ID del libro para la sesión
     * @param session Datos de la sesión
     * @return Sesión creada con validaciones aplicadas
     */
    public ReadingSession createSession(Long bookId, ReadingSession session) {
        // VALIDACIÓN 1: Verificar que el libro existe
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // CONFIGURACIÓN: Establecer libro en la sesión
        session.setBook(book);

        // CONFIGURACIÓN 2: Tiempo de inicio por defecto
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }

        // CONFIGURACIÓN 3: Páginas leídas por defecto
        if (session.getPagesRead() == null) {
            session.setPagesRead(0);
        }

        // VALIDACIÓN 2: Páginas leídas no pueden ser negativas
        if (session.getPagesRead() < 0) {
            throw new RuntimeException("Pages read cannot be negative");
        }

        // VALIDACIÓN 3: Páginas leídas no pueden exceder el libro
        if (book.getTotalPages() != null && session.getPagesRead() > book.getTotalPages()) {
            throw new RuntimeException("Pages read (" + session.getPagesRead() +
                    ") cannot exceed total book pages (" + book.getTotalPages() + ")");
        }

        // VALIDACIÓN 4: Tiempo lógico (si hay tiempo de fin)
        if (session.getEndTime() != null) {
            if (session.getEndTime().isBefore(session.getStartTime())) {
                throw new RuntimeException("End time cannot be before start time");
            }

            // VALIDACIÓN 5: Duración razonable (máximo 12 horas por sesión)
            Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
            if (duration.toHours() > 12) {
                throw new RuntimeException("Reading session cannot exceed 12 hours. " +
                        "Please split into multiple sessions.");
            }

            // VALIDACIÓN 6: Sesión no puede ser en el futuro
            if (session.getEndTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("End time cannot be in the future");
            }
        }

        // CONFIGURACIÓN 4: Mood por defecto si no se especifica
        if (session.getMood() == null) {
            session.setMood(ReadingMood.FOCUSED); // Mood por defecto
        }

        // LÓGICA DE NEGOCIO: Auto-generar notas si están vacías
        if (session.getNotes() == null || session.getNotes().trim().isEmpty()) {
            session.setNotes(generateDefaultNotes(session));
        }

        return sessionRepository.save(session);
    }

    /**
     * TERMINAR SESIÓN ACTIVA
     *
     * Para sesiones que se iniciaron sin tiempo de fin
     *
     * @param sessionId ID de la sesión a terminar
     * @param pagesRead Páginas leídas durante la sesión
     * @param mood Humor durante la lectura (opcional)
     * @return Sesión actualizada y terminada
     */
    public ReadingSession endSession(Long sessionId, Integer pagesRead, ReadingMood mood) {
        ReadingSession session = getSessionById(sessionId);

        // VALIDACIÓN: No puede terminar una sesión ya terminada
        if (session.getEndTime() != null) {
            throw new RuntimeException("Session is already ended");
        }

        // CONFIGURACIÓN: Establecer tiempo de fin
        session.setEndTime(LocalDateTime.now());

        // ACTUALIZACIÓN: Páginas leídas si se proporciona
        if (pagesRead != null) {
            if (pagesRead < 0) {
                throw new RuntimeException("Pages read cannot be negative");
            }
            session.setPagesRead(pagesRead);
        }

        // ACTUALIZACIÓN: Mood si se proporciona
        if (mood != null) {
            session.setMood(mood);
        }

        // VALIDACIÓN: Duración mínima razonable (al menos 1 minuto)
        Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
        if (duration.toMinutes() < 1) {
            throw new RuntimeException("Reading session must be at least 1 minute long");
        }

        // LÓGICA DE NEGOCIO: Actualizar notas con estadísticas
        if (session.getNotes() == null || session.getNotes().contains("Session in progress")) {
            session.setNotes(generateSessionSummary(session));
        }

        return sessionRepository.save(session);
    }

    /**
     * OBTENER SESIÓN POR ID
     *
     * @param id ID de la sesión
     * @return ReadingSession encontrada
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public ReadingSession getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading session not found with id: " + id));
    }

    /**
     * ACTUALIZAR SESIÓN EXISTENTE
     *
     * @param sessionId ID de la sesión a actualizar
     * @param updatedSession Datos actualizados
     * @return Sesión actualizada
     */
    public ReadingSession updateSession(Long sessionId, ReadingSession updatedSession) {
        ReadingSession existingSession = getSessionById(sessionId);

        // Actualizar solo campos no nulos
        if (updatedSession.getStartTime() != null) {
            existingSession.setStartTime(updatedSession.getStartTime());
        }

        if (updatedSession.getEndTime() != null) {
            existingSession.setEndTime(updatedSession.getEndTime());
        }

        if (updatedSession.getPagesRead() != null) {
            if (updatedSession.getPagesRead() < 0) {
                throw new RuntimeException("Pages read cannot be negative");
            }
            existingSession.setPagesRead(updatedSession.getPagesRead());
        }

        if (updatedSession.getMood() != null) {
            existingSession.setMood(updatedSession.getMood());
        }

        if (updatedSession.getNotes() != null) {
            existingSession.setNotes(updatedSession.getNotes());
        }

        // VALIDACIÓN: Consistencia temporal después de actualizaciones
        validateSessionTiming(existingSession);

        return sessionRepository.save(existingSession);
    }

    // ========================================
    // OPERACIONES DE CONSULTA
    // ========================================

    /**
     * OBTENER SESIONES POR LIBRO
     *
     * @param bookId ID del libro
     * @return Lista de sesiones ordenadas por fecha (más recientes primero)
     */
    @Transactional(readOnly = true)
    public List<ReadingSession> getSessionsByBook(Long bookId) {
        // Verificar que el libro existe
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with id: " + bookId);
        }
        return sessionRepository.findByBookIdOrderByStartTimeDesc(bookId);
    }

    /**
     * OBTENER SESIONES POR FECHA
     *
     * @param date Fecha específica
     * @return Lista de sesiones de esa fecha
     */
    @Transactional(readOnly = true)
    public List<ReadingSession> getSessionsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return sessionRepository.findSessionsBetweenDates(startOfDay, endOfDay);
    }

    // ========================================
    // ESTADÍSTICAS BÁSICAS
    // ========================================

    /**
     * OBTENER PÁGINAS TOTALES LEÍDAS PARA UN LIBRO
     *
     * @param bookId ID del libro
     * @return Total de páginas leídas en todas las sesiones
     */
    @Transactional(readOnly = true)
    public Integer getTotalPagesReadForBook(Long bookId) {
        Integer total = sessionRepository.getTotalPagesReadForBook(bookId);
        return total != null ? total : 0;
    }

    /**
     * CALCULAR TIEMPO TOTAL DE LECTURA PARA UN LIBRO
     *
     * @param bookId ID del libro
     * @return Duración total en horas
     */
    @Transactional(readOnly = true)
    public Double getTotalReadingTimeForBook(Long bookId) {
        List<ReadingSession> sessions = getSessionsByBook(bookId);

        return sessions.stream()
                .filter(session -> session.getEndTime() != null)
                .mapToDouble(session -> Duration.between(session.getStartTime(), session.getEndTime()).toHours())
                .sum();
    }

    /**
     * CALCULAR VELOCIDAD PROMEDIO DE LECTURA PARA UN LIBRO
     *
     * @param bookId ID del libro
     * @return Páginas por hora
     */
    @Transactional(readOnly = true)
    public Double getAverageReadingSpeedForBook(Long bookId) {
        Integer totalPages = getTotalPagesReadForBook(bookId);
        Double totalHours = getTotalReadingTimeForBook(bookId);

        if (totalHours > 0) {
            return totalPages.doubleValue() / totalHours;
        }
        return 0.0;
    }

    /**
     * OBTENER ESTADÍSTICAS BÁSICAS DE UN LIBRO
     *
     * @param bookId ID del libro
     * @return Estadísticas consolidadas
     */
    @Transactional(readOnly = true)
    public BookReadingStats getBookReadingStats(Long bookId) {
        List<ReadingSession> sessions = getSessionsByBook(bookId);

        BookReadingStats stats = new BookReadingStats();
        stats.setTotalSessions((long) sessions.size());
        stats.setTotalPagesRead(getTotalPagesReadForBook(bookId));
        stats.setTotalReadingHours(getTotalReadingTimeForBook(bookId));
        stats.setAverageReadingSpeed(getAverageReadingSpeedForBook(bookId));

        // Fechas de lectura
        if (!sessions.isEmpty()) {
            stats.setFirstReadingDate(sessions.get(sessions.size() - 1).getStartTime().toLocalDate());
            stats.setLastReadingDate(sessions.get(0).getStartTime().toLocalDate());
        }

        return stats;
    }

    // ========================================
    // OPERACIONES AVANZADAS
    // ========================================

    /**
     * ELIMINAR SESIÓN
     *
     * @param sessionId ID de la sesión a eliminar
     */
    public void deleteSession(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("Reading session not found with id: " + sessionId);
        }
        sessionRepository.deleteById(sessionId);
    }

    /**
     * ELIMINAR TODAS LAS SESIONES DE UN LIBRO
     *
     * @param bookId ID del libro
     * @return Número de sesiones eliminadas
     */
    public int deleteSessionsByBook(Long bookId) {
        List<ReadingSession> sessions = getSessionsByBook(bookId);
        sessionRepository.deleteAll(sessions);
        return sessions.size();
    }

    /**
     * DUPLICAR SESIÓN
     *
     * Útil para corregir errores o sesiones similares
     *
     * @param sessionId ID de la sesión a duplicar
     * @return Nueva sesión creada
     */
    public ReadingSession duplicateSession(Long sessionId) {
        ReadingSession original = getSessionById(sessionId);

        ReadingSession duplicate = new ReadingSession();
        duplicate.setBook(original.getBook());
        duplicate.setStartTime(LocalDateTime.now()); // Nueva fecha
        duplicate.setPagesRead(original.getPagesRead());
        duplicate.setMood(original.getMood());
        duplicate.setNotes(original.getNotes() + " (Duplicated)");
        // No copiar endTime para que sea una sesión nueva

        return sessionRepository.save(duplicate);
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================================

    /**
     * VALIDAR TIMING DE SESIÓN
     */
    private void validateSessionTiming(ReadingSession session) {
        if (session.getEndTime() != null &&
                session.getEndTime().isBefore(session.getStartTime())) {
            throw new RuntimeException("End time cannot be before start time");
        }

        if (session.getEndTime() != null) {
            Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
            if (duration.toHours() > 12) {
                throw new RuntimeException("Reading session cannot exceed 12 hours");
            }
        }
    }

    /**
     * GENERAR NOTAS POR DEFECTO
     */
    private String generateDefaultNotes(ReadingSession session) {
        if (session.getEndTime() == null) {
            return "Session in progress...";
        } else {
            Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
            return String.format("Read for %d minutes", duration.toMinutes());
        }
    }

    /**
     * GENERAR RESUMEN DE SESIÓN
     */
    private String generateSessionSummary(ReadingSession session) {
        Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
        int pages = session.getPagesRead() != null ? session.getPagesRead() : 0;

        String summary = String.format("Session: %d pages in %d minutes",
                pages, duration.toMinutes());

        if (duration.toMinutes() > 0 && pages > 0) {
            double pagesPerHour = (double) pages / duration.toHours();
            summary += String.format(" (%.1f pages/hour)", pagesPerHour);
        }

        return summary;
    }

    // ========================================
    // CLASE INTERNA PARA ESTADÍSTICAS
    // ========================================

    /**
     * DTO para estadísticas básicas de lectura de un libro
     */
    public static class BookReadingStats {
        private Long totalSessions;
        private Integer totalPagesRead;
        private Double totalReadingHours;
        private Double averageReadingSpeed;
        private LocalDate firstReadingDate;
        private LocalDate lastReadingDate;

        // Getters y setters
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }

        public Integer getTotalPagesRead() { return totalPagesRead; }
        public void setTotalPagesRead(Integer totalPagesRead) { this.totalPagesRead = totalPagesRead; }

        public Double getTotalReadingHours() { return totalReadingHours; }
        public void setTotalReadingHours(Double totalReadingHours) { this.totalReadingHours = totalReadingHours; }

        public Double getAverageReadingSpeed() { return averageReadingSpeed; }
        public void setAverageReadingSpeed(Double averageReadingSpeed) { this.averageReadingSpeed = averageReadingSpeed; }

        public LocalDate getFirstReadingDate() { return firstReadingDate; }
        public void setFirstReadingDate(LocalDate firstReadingDate) { this.firstReadingDate = firstReadingDate; }

        public LocalDate getLastReadingDate() { return lastReadingDate; }
        public void setLastReadingDate(LocalDate lastReadingDate) { this.lastReadingDate = lastReadingDate; }
    }

    /*
     * NOTAS IMPORTANTES SOBRE ReadingSessionService:
     *
     * 1. VERSIÓN SIMPLIFICADA:
     *    - Solo usa métodos que ya existen en ReadingSessionRepository
     *    - Evita errores de compilación
     *    - Mantiene funcionalidad esencial
     *
     * 2. VALIDACIONES ESENCIALES:
     *    - Consistencia temporal (fin después de inicio)
     *    - Duraciones razonables (máximo 12 horas)
     *    - Páginas leídas no negativas
     *    - No sesiones en el futuro
     *
     * 3. FUNCIONALIDAD CLAVE:
     *    - Crear y terminar sesiones
     *    - Calcular estadísticas básicas
     *    - Validaciones de tiempo y páginas
     *    - Integración con BookService
     *
     * 4. ESTADÍSTICAS IMPLEMENTADAS:
     *    - Tiempo total por libro
     *    - Páginas totales por libro
     *    - Velocidad promedio de lectura
     *    - Estadísticas consolidadas
     *
     * 5. ESCALABILIDAD:
     *    - Estructura preparada para añadir más features
     *    - Métodos modulares y reutilizables
     *    - Separación clara de responsabilidades
     */
}