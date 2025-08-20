package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.model.BookStatus;  // Enum para estados de libros
import com.mybookshelf.mybookshelf_backend.model.Book;        // Entidad Book
import com.mybookshelf.mybookshelf_backend.repository.BookRepository; // Repository principal
import com.mybookshelf.mybookshelf_backend.repository.ReadingSessionRepository; // Para análisis de sesiones
import org.springframework.stereotype.Service;                // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.time.LocalDate;
import java.util.*;

/**
 * CLASE AnalyticsService - "Centro de Estadísticas Básicas"
 *
 * ¿Para qué sirve AnalyticsService?
 * - Generar dashboard con métricas clave
 * - Analizar progreso básico de lectura
 * - Proporcionar estadísticas fundamentales
 * - Calcular métricas simples y efectivas
 *
 * CASOS DE USO PRINCIPALES:
 * - "Dashboard principal" - Métricas más importantes
 * - "Progreso anual" - Libros leídos por año
 * - "Estadísticas básicas" - Contadores por estado
 * - "Métricas de productividad" - Tasas y promedios
 *
 * VERSIÓN SIMPLIFICADA:
 * - Solo usa métodos que ya existen en repositories
 * - Enfoque en métricas esenciales y funcionales
 * - Evita queries complejas que aún no están implementadas
 * - Base sólida para expandir funcionalidad
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional(readOnly = true) // Solo operaciones de lectura por defecto
public class AnalyticsService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    /**
     * REPOSITORIES: Acceso a datos básicos
     *
     * Solo usamos repositories principales para evitar dependencias complejas
     */
    private final BookRepository bookRepository;
    private final ReadingSessionRepository sessionRepository;

    /**
     * CONSTRUCTOR: Inyección de dependencias
     *
     * Spring inyecta automáticamente los repositories
     * Constructor injection es la forma recomendada
     */
    public AnalyticsService(BookRepository bookRepository,
                            ReadingSessionRepository sessionRepository) {
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
    }

    // ========================================
    // DASHBOARD PRINCIPAL
    // ========================================

    /**
     * OBTENER ESTADÍSTICAS PARA DASHBOARD PRINCIPAL
     *
     * Métricas más importantes para la vista principal:
     * - Contadores básicos por estado
     * - Progreso del año actual
     * - Métricas fundamentales
     *
     * @return DTO con todas las estadísticas clave
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // CONTADORES BÁSICOS: Estados de libros
        stats.setTotalBooks(bookRepository.count());
        stats.setBooksReading(bookRepository.countByStatus(BookStatus.READING));
        stats.setBooksFinished(bookRepository.countByStatus(BookStatus.FINISHED));
        stats.setBooksWishlist(bookRepository.countByStatus(BookStatus.WISHLIST));
        stats.setBooksAbandoned(bookRepository.countByStatus(BookStatus.ABANDONED));
        stats.setBooksOnHold(bookRepository.countByStatus(BookStatus.ON_HOLD));

        // ESTADÍSTICAS TEMPORALES: Año actual
        int currentYear = LocalDate.now().getYear();
        stats.setBooksFinishedThisYear(bookRepository.countBooksFinishedInYear(currentYear));

        // MÉTRICAS CALCULADAS
        stats.setCompletionRate(calculateCompletionRate());
        stats.setAveragePages(calculateAveragePages());

        // COMPARACIÓN CON AÑO ANTERIOR
        if (currentYear > 2020) {
            Long previousYearBooks = bookRepository.countBooksFinishedInYear(currentYear - 1);
            stats.setYearOverYearGrowth(calculateGrowthPercentage(previousYearBooks, stats.getBooksFinishedThisYear()));
        }

        return stats;
    }

    /**
     * OBTENER MÉTRICAS RÁPIDAS
     *
     * Versión simplificada para widgets o notificaciones
     *
     * @return DTO con métricas básicas
     */
    public QuickStatsDTO getQuickStats() {
        QuickStatsDTO stats = new QuickStatsDTO();

        stats.setBooksReading(bookRepository.countByStatus(BookStatus.READING));
        stats.setBooksFinished(bookRepository.countByStatus(BookStatus.FINISHED));
        stats.setTotalBooks(bookRepository.count());

        // Calcular porcentaje de progreso general
        if (stats.getTotalBooks() > 0) {
            stats.setProgressPercentage((stats.getBooksFinished().doubleValue() / stats.getTotalBooks().doubleValue()) * 100);
        } else {
            stats.setProgressPercentage(0.0);
        }

        return stats;
    }

    // ========================================
    // ANÁLISIS TEMPORAL BÁSICO
    // ========================================

    /**
     * OBTENER PROGRESO ANUAL SIMPLIFICADO
     *
     * Análisis año por año de libros terminados
     *
     * @return Lista de DTOs con progreso anual
     */
    public List<YearlyProgressDTO> getYearlyProgress() {
        List<YearlyProgressDTO> yearlyProgress = new ArrayList<>();

        // Obtener rango de años con datos
        int currentYear = LocalDate.now().getYear();
        int startYear = Math.max(2020, currentYear - 5); // Últimos 5 años máximo

        for (int year = startYear; year <= currentYear; year++) {
            YearlyProgressDTO yearData = new YearlyProgressDTO();
            yearData.setYear(year);

            // ESTADÍSTICA PRINCIPAL: Libros terminados
            Long booksFinished = bookRepository.countBooksFinishedInYear(year);
            yearData.setBooksFinished(booksFinished);

            // COMPARACIÓN con año anterior
            if (year > startYear) {
                Long previousYearBooks = bookRepository.countBooksFinishedInYear(year - 1);
                yearData.setGrowthFromPreviousYear(calculateGrowthPercentage(previousYearBooks, booksFinished));
            }

            // ESTADO ACTUAL (solo para el año en curso)
            if (year == currentYear) {
                yearData.setIsCurrentYear(true);
                yearData.setCurrentYearProjection(calculateYearEndProjection(year, booksFinished));
            }

            yearlyProgress.add(yearData);
        }

        return yearlyProgress;
    }

    /**
     * OBTENER ESTADÍSTICAS POR MES DEL AÑO ACTUAL
     *
     * Progreso mes a mes para el año en curso
     *
     * @return Lista de DTOs con progreso mensual
     */
    public List<MonthlyProgressDTO> getCurrentYearMonthlyProgress() {
        List<MonthlyProgressDTO> monthlyProgress = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            MonthlyProgressDTO monthData = new MonthlyProgressDTO();
            monthData.setYear(currentYear);
            monthData.setMonth(month);
            monthData.setMonthName(getMonthName(month));

            // CÁLCULO MANUAL de libros terminados en el mes
            Long booksFinished = getBooksFinishedInMonth(currentYear, month);
            monthData.setBooksFinished(booksFinished);

            // MARCAR mes actual
            if (month == LocalDate.now().getMonthValue()) {
                monthData.setIsCurrentMonth(true);
            }

            monthlyProgress.add(monthData);
        }

        return monthlyProgress;
    }

    // ========================================
    // ANÁLISIS DE ESTADOS Y PROGRESO
    // ========================================

    /**
     * ANÁLISIS DE DISTRIBUCIÓN POR ESTADOS
     *
     * @return Mapa con porcentajes por cada estado
     */
    public Map<String, Double> getStatusDistribution() {
        Map<String, Double> distribution = new HashMap<>();
        Long totalBooks = bookRepository.count();

        if (totalBooks == 0) {
            return distribution; // Retornar mapa vacío si no hay libros
        }

        // Calcular porcentajes para cada estado
        for (BookStatus status : BookStatus.values()) {
            Long count = bookRepository.countByStatus(status);
            Double percentage = (count.doubleValue() / totalBooks.doubleValue()) * 100;
            distribution.put(status.name(), percentage);
        }

        return distribution;
    }

    /**
     * ANÁLISIS DE LIBROS ACTUALMENTE LEYENDO
     *
     * @return Lista con información de libros en progreso
     */
    public List<ReadingProgressDTO> getCurrentlyReadingAnalysis() {
        List<Book> readingBooks = bookRepository.findByStatus(BookStatus.READING);
        List<ReadingProgressDTO> progressList = new ArrayList<>();

        for (Book book : readingBooks) {
            ReadingProgressDTO progress = new ReadingProgressDTO();
            progress.setBookId(book.getId());
            progress.setTitle(book.getTitle());
            progress.setCurrentPage(book.getCurrentPage() != null ? book.getCurrentPage() : 0);
            progress.setTotalPages(book.getTotalPages() != null ? book.getTotalPages() : 0);

            // Calcular porcentaje de progreso
            if (progress.getTotalPages() > 0) {
                progress.setProgressPercentage(
                        (progress.getCurrentPage().doubleValue() / progress.getTotalPages().doubleValue()) * 100);
            } else {
                progress.setProgressPercentage(0.0);
            }

            // Calcular páginas restantes
            progress.setPagesRemaining(Math.max(0, progress.getTotalPages() - progress.getCurrentPage()));

            progressList.add(progress);
        }

        // Ordenar por progreso descendente
        progressList.sort((a, b) -> Double.compare(b.getProgressPercentage(), a.getProgressPercentage()));

        return progressList;
    }

    // ========================================
    // MÉTRICAS DE PRODUCTIVIDAD
    // ========================================

    /**
     * CALCULAR ESTADÍSTICAS DE PRODUCTIVIDAD
     *
     * @return DTO con métricas de rendimiento
     */
    public ProductivityStatsDTO getProductivityStats() {
        ProductivityStatsDTO stats = new ProductivityStatsDTO();

        // MÉTRICAS BÁSICAS
        Long totalBooks = bookRepository.count();
        Long finishedBooks = bookRepository.countByStatus(BookStatus.FINISHED);
        Long abandonedBooks = bookRepository.countByStatus(BookStatus.ABANDONED);

        stats.setTotalBooks(totalBooks);
        stats.setFinishedBooks(finishedBooks);
        stats.setAbandonedBooks(abandonedBooks);

        // TASAS CALCULADAS
        if (totalBooks > 0) {
            stats.setCompletionRate((finishedBooks.doubleValue() / totalBooks.doubleValue()) * 100);
            stats.setAbandonmentRate((abandonedBooks.doubleValue() / totalBooks.doubleValue()) * 100);
        }

        // PROMEDIOS
        stats.setAveragePagesPerBook(calculateAveragePages());

        // MÉTRICAS TEMPORALES
        int currentYear = LocalDate.now().getYear();
        stats.setBooksThisYear(bookRepository.countBooksFinishedInYear(currentYear));

        // Calcular promedio mensual del año actual
        int currentMonth = LocalDate.now().getMonthValue();
        if (currentMonth > 0) {
            stats.setAverageBooksPerMonth(stats.getBooksThisYear().doubleValue() / currentMonth);
        }

        return stats;
    }

    /**
     * OBTENER TOP LIBROS POR RATING
     *
     * @return Lista de libros mejor calificados
     */
    public List<TopBookDTO> getTopRatedBooks() {
        List<Book> allBooks = bookRepository.findAll();
        List<TopBookDTO> topBooks = new ArrayList<>();

        for (Book book : allBooks) {
            if (book.getPersonalRating() != null && book.getPersonalRating().doubleValue() > 0) {
                TopBookDTO topBook = new TopBookDTO();
                topBook.setTitle(book.getTitle());
                topBook.setRating(book.getPersonalRating().doubleValue());
                topBook.setPages(book.getTotalPages() != null ? book.getTotalPages() : 0);
                topBook.setStatus(book.getStatus().name());
                topBooks.add(topBook);
            }
        }

        // Ordenar por rating descendente y tomar top 10
        topBooks.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        return topBooks.stream().limit(10).toList();
    }

    // ========================================
    // MÉTODOS DE CÁLCULO PRIVADOS
    // ========================================

    /**
     * CALCULAR TASA DE FINALIZACIÓN
     */
    private Double calculateCompletionRate() {
        Long totalBooks = bookRepository.count();
        Long finishedBooks = bookRepository.countByStatus(BookStatus.FINISHED);

        if (totalBooks == 0) return 0.0;
        return (finishedBooks.doubleValue() / totalBooks.doubleValue()) * 100;
    }

    /**
     * CALCULAR PROMEDIO DE PÁGINAS
     */
    private Double calculateAveragePages() {
        List<Book> allBooks = bookRepository.findAll();

        if (allBooks.isEmpty()) return 0.0;

        return allBooks.stream()
                .filter(book -> book.getTotalPages() != null && book.getTotalPages() > 0)
                .mapToInt(Book::getTotalPages)
                .average()
                .orElse(0.0);
    }

    /**
     * CALCULAR PORCENTAJE DE CRECIMIENTO
     */
    private Double calculateGrowthPercentage(Long oldValue, Long newValue) {
        if (oldValue == null || oldValue == 0) {
            return newValue != null && newValue > 0 ? 100.0 : 0.0;
        }
        if (newValue == null) newValue = 0L;

        return ((newValue.doubleValue() - oldValue.doubleValue()) / oldValue.doubleValue()) * 100;
    }

    /**
     * PROYECCIÓN PARA FIN DE AÑO
     */
    private Long calculateYearEndProjection(int year, Long booksFinishedSoFar) {
        if (year != LocalDate.now().getYear()) return booksFinishedSoFar;

        int dayOfYear = LocalDate.now().getDayOfYear();
        int daysInYear = LocalDate.of(year, 12, 31).getDayOfYear();

        if (dayOfYear == 0) return 0L;

        double dailyRate = booksFinishedSoFar.doubleValue() / dayOfYear;
        return Math.round(dailyRate * daysInYear);
    }

    /**
     * CONTAR LIBROS TERMINADOS EN UN MES ESPECÍFICO
     */
    private Long getBooksFinishedInMonth(int year, int month) {
        // Implementación manual usando findAll y filtrado
        List<Book> finishedBooks = bookRepository.findByStatus(BookStatus.FINISHED);

        return finishedBooks.stream()
                .filter(book -> book.getFinishDate() != null)
                .filter(book -> book.getFinishDate().getYear() == year)
                .filter(book -> book.getFinishDate().getMonthValue() == month)
                .count();
    }

    /**
     * OBTENER NOMBRE DEL MES
     */
    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }

    // ========================================
    // CLASES INTERNAS PARA DTOs
    // ========================================

    /**
     * DTO para estadísticas del dashboard principal
     */
    public static class DashboardStatsDTO {
        private Long totalBooks;
        private Long booksReading;
        private Long booksFinished;
        private Long booksWishlist;
        private Long booksAbandoned;
        private Long booksOnHold;
        private Long booksFinishedThisYear;
        private Double completionRate;
        private Double averagePages;
        private Double yearOverYearGrowth;

        // Getters y setters
        public Long getTotalBooks() { return totalBooks; }
        public void setTotalBooks(Long totalBooks) { this.totalBooks = totalBooks; }
        public Long getBooksReading() { return booksReading; }
        public void setBooksReading(Long booksReading) { this.booksReading = booksReading; }
        public Long getBooksFinished() { return booksFinished; }
        public void setBooksFinished(Long booksFinished) { this.booksFinished = booksFinished; }
        public Long getBooksWishlist() { return booksWishlist; }
        public void setBooksWishlist(Long booksWishlist) { this.booksWishlist = booksWishlist; }
        public Long getBooksAbandoned() { return booksAbandoned; }
        public void setBooksAbandoned(Long booksAbandoned) { this.booksAbandoned = booksAbandoned; }
        public Long getBooksOnHold() { return booksOnHold; }
        public void setBooksOnHold(Long booksOnHold) { this.booksOnHold = booksOnHold; }
        public Long getBooksFinishedThisYear() { return booksFinishedThisYear; }
        public void setBooksFinishedThisYear(Long booksFinishedThisYear) { this.booksFinishedThisYear = booksFinishedThisYear; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getAveragePages() { return averagePages; }
        public void setAveragePages(Double averagePages) { this.averagePages = averagePages; }
        public Double getYearOverYearGrowth() { return yearOverYearGrowth; }
        public void setYearOverYearGrowth(Double yearOverYearGrowth) { this.yearOverYearGrowth = yearOverYearGrowth; }
    }

    /**
     * DTO para métricas rápidas
     */
    public static class QuickStatsDTO {
        private Long booksReading;
        private Long booksFinished;
        private Long totalBooks;
        private Double progressPercentage;

        // Getters y setters
        public Long getBooksReading() { return booksReading; }
        public void setBooksReading(Long booksReading) { this.booksReading = booksReading; }
        public Long getBooksFinished() { return booksFinished; }
        public void setBooksFinished(Long booksFinished) { this.booksFinished = booksFinished; }
        public Long getTotalBooks() { return totalBooks; }
        public void setTotalBooks(Long totalBooks) { this.totalBooks = totalBooks; }
        public Double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    }

    /**
     * DTO para progreso anual
     */
    public static class YearlyProgressDTO {
        private int year;
        private Long booksFinished;
        private Double growthFromPreviousYear;
        private boolean isCurrentYear;
        private Long currentYearProjection;

        // Getters y setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public Long getBooksFinished() { return booksFinished; }
        public void setBooksFinished(Long booksFinished) { this.booksFinished = booksFinished; }
        public Double getGrowthFromPreviousYear() { return growthFromPreviousYear; }
        public void setGrowthFromPreviousYear(Double growthFromPreviousYear) { this.growthFromPreviousYear = growthFromPreviousYear; }
        public boolean isCurrentYear() { return isCurrentYear; }
        public void setIsCurrentYear(boolean isCurrentYear) { this.isCurrentYear = isCurrentYear; }
        public Long getCurrentYearProjection() { return currentYearProjection; }
        public void setCurrentYearProjection(Long currentYearProjection) { this.currentYearProjection = currentYearProjection; }
    }

    /**
     * DTO para progreso mensual
     */
    public static class MonthlyProgressDTO {
        private int year;
        private int month;
        private String monthName;
        private Long booksFinished;
        private boolean isCurrentMonth;

        // Getters y setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }
        public Long getBooksFinished() { return booksFinished; }
        public void setBooksFinished(Long booksFinished) { this.booksFinished = booksFinished; }
        public boolean isCurrentMonth() { return isCurrentMonth; }
        public void setIsCurrentMonth(boolean isCurrentMonth) { this.isCurrentMonth = isCurrentMonth; }
    }

    /**
     * DTO para progreso de lectura actual
     */
    public static class ReadingProgressDTO {
        private Long bookId;
        private String title;
        private Integer currentPage;
        private Integer totalPages;
        private Double progressPercentage;
        private Integer pagesRemaining;

        // Getters y setters
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
        public Double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
        public Integer getPagesRemaining() { return pagesRemaining; }
        public void setPagesRemaining(Integer pagesRemaining) { this.pagesRemaining = pagesRemaining; }
    }

    /**
     * DTO para estadísticas de productividad
     */
    public static class ProductivityStatsDTO {
        private Long totalBooks;
        private Long finishedBooks;
        private Long abandonedBooks;
        private Double completionRate;
        private Double abandonmentRate;
        private Double averagePagesPerBook;
        private Long booksThisYear;
        private Double averageBooksPerMonth;

        // Getters y setters
        public Long getTotalBooks() { return totalBooks; }
        public void setTotalBooks(Long totalBooks) { this.totalBooks = totalBooks; }
        public Long getFinishedBooks() { return finishedBooks; }
        public void setFinishedBooks(Long finishedBooks) { this.finishedBooks = finishedBooks; }
        public Long getAbandonedBooks() { return abandonedBooks; }
        public void setAbandonedBooks(Long abandonedBooks) { this.abandonedBooks = abandonedBooks; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getAbandonmentRate() { return abandonmentRate; }
        public void setAbandonmentRate(Double abandonmentRate) { this.abandonmentRate = abandonmentRate; }
        public Double getAveragePagesPerBook() { return averagePagesPerBook; }
        public void setAveragePagesPerBook(Double averagePagesPerBook) { this.averagePagesPerBook = averagePagesPerBook; }
        public Long getBooksThisYear() { return booksThisYear; }
        public void setBooksThisYear(Long booksThisYear) { this.booksThisYear = booksThisYear; }
        public Double getAverageBooksPerMonth() { return averageBooksPerMonth; }
        public void setAverageBooksPerMonth(Double averageBooksPerMonth) { this.averageBooksPerMonth = averageBooksPerMonth; }
    }

    /**
     * DTO para top libros
     */
    public static class TopBookDTO {
        private String title;
        private Double rating;
        private Integer pages;
        private String status;

        // Getters y setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /*
     * NOTAS IMPORTANTES SOBRE AnalyticsService SIMPLIFICADO:
     *
     * 1. ENFOQUE PRÁCTICO:
     *    - Solo usa métodos que ya existen en repositories
     *    - Evita queries complejas que requieren implementación adicional
     *    - Mantiene funcionalidad esencial del dashboard
     *
     * 2. MÉTRICAS IMPLEMENTADAS:
     *    - Contadores básicos por estado de libro
     *    - Análisis temporal (anual y mensual)
     *    - Tasas de finalización y abandono
     *    - Progreso de libros actualmente leyendo
     *
     * 3. CÁLCULOS MANUALES:
     *    - Filtrado en memoria cuando no hay queries específicas
     *    - Proyecciones simples basadas en tendencias actuales
     *    - Porcentajes y promedios calculados dinámicamente
     *
     * 4. ESCALABILIDAD:
     *    - Estructura preparada para añadir queries optimizadas
     *    - DTOs bien definidos para diferentes tipos de análisis
     *    - Métodos modulares fáciles de extender
     *
     * 5. PERFORMANCE:
     *    - Operaciones read-only para todas las consultas
     *    - Cálculos eficientes con streams de Java
     *    - Evita queries N+1 usando findAll cuando es apropiado
     *
     * Esta versión proporcionará un dashboard funcional y útil
     * mientras mantienes la aplicación compilando correctamente.
     */
}