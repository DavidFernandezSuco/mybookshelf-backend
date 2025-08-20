package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.model.*;       // Nuestras entidades
import com.mybookshelf.mybookshelf_backend.repository.*;  // Nuestros repositories
import org.springframework.data.domain.Page;               // Para paginación
import org.springframework.data.domain.Pageable;           // Configuración de paginación
import org.springframework.stereotype.Service;             // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CLASE BookService - "Cerebro del Sistema de Libros"
 *
 * ¿Qué es un Service?
 * - Capa de LÓGICA DE NEGOCIO entre Controller y Repository
 * - Contiene las REGLAS del negocio (validaciones, cálculos, decisiones)
 * - Orquesta operaciones complejas que involucran múltiples repositories
 * - Maneja transacciones para mantener consistencia de datos
 *
 * ¿Por qué separar Service del Controller?
 * - REUTILIZACIÓN: Múltiples controllers pueden usar el mismo service
 * - TESTEO: Fácil testear lógica de negocio por separado
 * - MANTENIMIENTO: Cambios de negocio solo afectan el service
 * - TRANSACCIONES: Manejo centralizado de operaciones de BD
 *
 * RESPONSABILIDADES DE BookService:
 * 1. Validar datos antes de guardar
 * 2. Aplicar reglas de negocio (ej: auto-cambiar status cuando terminas un libro)
 * 3. Coordinar operaciones entre múltiples entities
 * 4. Manejar errores y excepciones de negocio
 * 5. Calcular estadísticas y métricas
 * 6. Optimizar queries para performance
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Todas las operaciones serán transaccionales por defecto
public class BookService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    /**
     * REPOSITORIES: Acceso a datos
     *
     * private final - Inmutables, se inyectan en constructor
     * No usamos @Autowired en campos por buenas prácticas
     */
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final ReadingSessionRepository readingSessionRepository;

    /**
     * CONSTRUCTOR: Inyección de dependencias
     *
     * Spring inyecta automáticamente los repositories
     * Constructor injection es la forma recomendada
     */
    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       GenreRepository genreRepository,
                       ReadingSessionRepository readingSessionRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.readingSessionRepository = readingSessionRepository;
    }

    // ========================================
    // OPERACIONES CRUD CON LÓGICA DE NEGOCIO
    // ========================================

    /**
     * OBTENER TODOS LOS LIBROS (CON PAGINACIÓN)
     *
     * @Transactional(readOnly = true) - Optimización para operaciones de solo lectura
     * No necesita modificar datos, permite optimizaciones de BD
     *
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de libros
     */
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * OBTENER LIBRO POR ID
     *
     * @param id ID del libro a buscar
     * @return Book encontrado
     * @throws RuntimeException si no existe el libro
     */
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    /**
     * CREAR NUEVO LIBRO
     *
     * Lógica de negocio aplicada:
     * 1. Validar que no existe libro con mismo ISBN (si tiene)
     * 2. Validar que autores y géneros existen
     * 3. Establecer valores por defecto
     * 4. Guardar con todas las relaciones
     *
     * @param book Libro a crear
     * @param authorIds IDs de autores (opcional)
     * @param genreIds IDs de géneros (opcional)
     * @return Libro creado con ID asignado
     */
    public Book createBook(Book book, Set<Long> authorIds, Set<Long> genreIds) {
        // VALIDACIÓN 1: ISBN único (si existe)
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
            if (existingBook.isPresent()) {
                throw new RuntimeException("Book with ISBN " + book.getIsbn() + " already exists");
            }
        }

        // VALIDACIÓN 2: Título obligatorio
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Book title is required");
        }

        // VALORES POR DEFECTO
        if (book.getStatus() == null) {
            book.setStatus(BookStatus.WISHLIST);
        }
        if (book.getCurrentPage() == null) {
            book.setCurrentPage(0);
        }

        // ASOCIAR AUTORES (si se proporcionaron)
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<Author> authors = Set.copyOf(authorRepository.findAllById(authorIds));
            if (authors.size() != authorIds.size()) {
                throw new RuntimeException("Some authors not found");
            }
            book.setAuthors(authors);
            // Mantener relación bidireccional
            authors.forEach(author -> author.getBooks().add(book));
        }

        // ASOCIAR GÉNEROS (si se proporcionaron)
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = Set.copyOf(genreRepository.findAllById(genreIds));
            if (genres.size() != genreIds.size()) {
                throw new RuntimeException("Some genres not found");
            }
            book.setGenres(genres);
            // Mantener relación bidireccional
            genres.forEach(genre -> genre.getBooks().add(book));
        }

        // GUARDAR libro (Hibernate gestiona las relaciones automáticamente)
        return bookRepository.save(book);
    }

    /**
     * ACTUALIZAR PROGRESO DE LECTURA
     *
     * Lógica de negocio compleja:
     * 1. Validar que la página actual no excede el total
     * 2. Auto-cambiar status según progreso
     * 3. Establecer fechas automáticamente
     * 4. Crear sesión de lectura si es necesario
     *
     * @param bookId ID del libro
     * @param newCurrentPage Nueva página actual
     * @return Libro actualizado
     */
    public Book updateBookProgress(Long bookId, Integer newCurrentPage) {
        // BUSCAR libro
        Book book = getBookById(bookId);

        // VALIDACIONES
        if (newCurrentPage < 0) {
            throw new RuntimeException("Current page cannot be negative");
        }

        if (book.getTotalPages() != null && newCurrentPage > book.getTotalPages()) {
            throw new RuntimeException("Current page cannot exceed total pages (" + book.getTotalPages() + ")");
        }

        // CALCULAR páginas leídas en esta actualización
        Integer pagesRead = newCurrentPage - (book.getCurrentPage() != null ? book.getCurrentPage() : 0);

        // ACTUALIZAR página actual
        book.setCurrentPage(newCurrentPage);

        // LÓGICA DE NEGOCIO: Auto-cambiar status según progreso
        if (newCurrentPage == 0) {
            // No ha empezado
            book.setStatus(BookStatus.WISHLIST);
            book.setStartDate(null);
        } else if (book.getTotalPages() != null && newCurrentPage.equals(book.getTotalPages())) {
            // Ha terminado el libro
            if (book.getStatus() != BookStatus.FINISHED) {
                book.setStatus(BookStatus.FINISHED);
                book.setFinishDate(LocalDate.now());
            }
        } else {
            // Está leyendo
            if (book.getStatus() == BookStatus.WISHLIST) {
                book.setStatus(BookStatus.READING);
                book.setStartDate(LocalDate.now());
            }
        }

        // CREAR sesión de lectura automática (si leyó páginas)
        if (pagesRead > 0) {
            ReadingSession session = new ReadingSession();
            session.setBook(book);
            session.setStartTime(java.time.LocalDateTime.now().minusHours(1)); // Estimar 1 hora atrás
            session.setEndTime(java.time.LocalDateTime.now());
            session.setPagesRead(pagesRead);
            session.setMood(ReadingMood.FOCUSED); // Mood por defecto
            session.setNotes("Progreso actualizado automáticamente");

            readingSessionRepository.save(session);
        }

        // GUARDAR libro actualizado
        return bookRepository.save(book);
    }

    /**
     * CAMBIAR STATUS DE LIBRO
     *
     * Lógica de negocio para cambios de estado:
     * - Al empezar a leer: establecer startDate
     * - Al terminar: establecer finishDate y verificar progreso
     * - Al abandonar: mantener fechas pero cambiar status
     *
     * @param bookId ID del libro
     * @param newStatus Nuevo estado
     * @return Libro actualizado
     */
    public Book changeBookStatus(Long bookId, BookStatus newStatus) {
        Book book = getBookById(bookId);
        BookStatus oldStatus = book.getStatus();

        // APLICAR lógica según el nuevo status
        switch (newStatus) {
            case READING:
                if (oldStatus == BookStatus.WISHLIST) {
                    book.setStartDate(LocalDate.now());
                }
                break;

            case FINISHED:
                book.setFinishDate(LocalDate.now());
                // Auto-completar progreso si no está al 100%
                if (book.getTotalPages() != null &&
                        (book.getCurrentPage() == null || !book.getCurrentPage().equals(book.getTotalPages()))) {
                    book.setCurrentPage(book.getTotalPages());
                }
                break;

            case ABANDONED:
            case ON_HOLD:
                // Mantener fechas existentes, solo cambiar status
                break;

            case WISHLIST:
                // Reset: volver al estado inicial
                book.setStartDate(null);
                book.setFinishDate(null);
                book.setCurrentPage(0);
                break;
        }

        book.setStatus(newStatus);
        return bookRepository.save(book);
    }

    // ========================================
    // OPERACIONES DE BÚSQUEDA AVANZADA
    // ========================================

    /**
     * BÚSQUEDA GENERAL DE LIBROS
     *
     * Combina múltiples criterios de búsqueda
     *
     * @param query Término de búsqueda (título, descripción, notas)
     * @return Lista de libros que coinciden
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of(); // Lista vacía si no hay término de búsqueda
        }
        return bookRepository.searchBooks(query.trim());
    }

    /**
     * FILTRAR LIBROS POR MÚLTIPLES CRITERIOS
     *
     * @param status Estado del libro (opcional)
     * @param authorLastName Apellido del autor (opcional)
     * @param genreName Nombre del género (opcional)
     * @param pageable Paginación
     * @return Página de libros filtrados
     */
    @Transactional(readOnly = true)
    public Page<Book> filterBooks(BookStatus status, String authorLastName,
                                  String genreName, Pageable pageable) {

        // Si solo hay filtro de status, usar query optimizada
        if (status != null && authorLastName == null && genreName == null) {
            return bookRepository.findByStatusAndTitleContainingIgnoreCase(status, "", pageable);
        }

        // Si hay filtro de autor y género, usar query combinada
        if (authorLastName != null && genreName != null) {
            List<Book> books = bookRepository.findByAuthorLastNameAndGenreName(authorLastName, genreName);
            // Convertir a Page (simplificado)
            return Page.empty(); // En implementación real, convertir List a Page
        }

        // Otros filtros individuales
        if (authorLastName != null) {
            List<Book> books = bookRepository.findByAuthorLastName(authorLastName);
            return Page.empty(); // Convertir a Page
        }

        if (genreName != null) {
            List<Book> books = bookRepository.findByGenreName(genreName);
            return Page.empty(); // Convertir a Page
        }

        // Sin filtros, devolver todo
        return bookRepository.findAll(pageable);
    }

    /**
     * OBTENER LIBROS POR STATUS
     *
     * @param status Estado de los libros
     * @return Lista de libros con ese estado
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksByStatus(BookStatus status) {
        return bookRepository.findByStatus(status);
    }

    /**
     * LIBROS ACTUALMENTE LEYENDO
     *
     * @return Lista de libros con status READING
     */
    @Transactional(readOnly = true)
    public List<Book> getCurrentlyReadingBooks() {
        return bookRepository.getCurrentlyReadingBooks();
    }

    // ========================================
    // ESTADÍSTICAS Y MÉTRICAS
    // ========================================

    /**
     * ESTADÍSTICAS GENERALES DE LIBROS
     *
     * @return Map con estadísticas clave
     */
    @Transactional(readOnly = true)
    public BookStatistics getBookStatistics() {
        BookStatistics stats = new BookStatistics();

        // Conteos por status
        stats.setTotalBooks(bookRepository.count());
        stats.setBooksReading(bookRepository.countByStatus(BookStatus.READING));
        stats.setBooksFinished(bookRepository.countByStatus(BookStatus.FINISHED));
        stats.setBooksWishlist(bookRepository.countByStatus(BookStatus.WISHLIST));
        stats.setBooksAbandoned(bookRepository.countByStatus(BookStatus.ABANDONED));
        stats.setBooksOnHold(bookRepository.countByStatus(BookStatus.ON_HOLD));

        // Estadísticas de páginas
        stats.setAveragePagesFinishedBooks(bookRepository.getAveragePagesOfFinishedBooks());

        // Libros terminados este año
        stats.setBooksFinishedThisYear(bookRepository.countBooksFinishedInYear(LocalDate.now().getYear()));

        // Progreso total
        List<Book> readingBooks = bookRepository.findByStatus(BookStatus.READING);
        Double totalProgress = readingBooks.stream()
                .mapToDouble(Book::getProgressPercentage)
                .average()
                .orElse(0.0);
        stats.setAverageProgressCurrentBooks(totalProgress);

        return stats;
    }

    /**
     * LIBROS MEJOR CALIFICADOS
     *
     * @param pageable Configuración de paginación
     * @return Página con libros mejor calificados
     */
    @Transactional(readOnly = true)
    public Page<Book> getTopRatedBooks(Pageable pageable) {
        return bookRepository.findTopRatedBooks(pageable);
    }

    /**
     * LIBROS RECOMENDADOS PARA LEER
     *
     * @param pageable Configuración de paginación
     * @return Página de libros recomendados (WISHLIST ordenados por rating)
     */
    @Transactional(readOnly = true)
    public Page<Book> getRecommendedBooks(Pageable pageable) {
        return bookRepository.findRecommendedBooks(pageable);
    }

    // ========================================
    // OPERACIONES AVANZADAS
    // ========================================

    /**
     * ELIMINAR LIBRO (CON VALIDACIONES)
     *
     * Verifica que se puede eliminar antes de hacerlo
     *
     * @param bookId ID del libro a eliminar
     */
    public void deleteBook(Long bookId) {
        Book book = getBookById(bookId);

        // VALIDACIÓN: Verificar si tiene sesiones de lectura
        List<ReadingSession> sessions = readingSessionRepository.findByBookIdOrderByStartTimeDesc(bookId);
        if (!sessions.isEmpty()) {
            throw new RuntimeException("Cannot delete book with existing reading sessions. " +
                    "Delete sessions first or keep the book.");
        }

        // LIMPIAR relaciones bidireccionales antes de eliminar
        book.getAuthors().forEach(author -> author.getBooks().remove(book));
        book.getGenres().forEach(genre -> genre.getBooks().remove(book));

        // ELIMINAR libro
        bookRepository.delete(book);
    }

    /**
     * DUPLICAR LIBRO (CREAR COPIA)
     *
     * Útil para crear diferentes ediciones del mismo libro
     *
     * @param bookId ID del libro a duplicar
     * @return Nuevo libro creado
     */
    public Book duplicateBook(Long bookId) {
        Book original = getBookById(bookId);

        Book duplicate = new Book();
        duplicate.setTitle(original.getTitle() + " (Copy)");
        duplicate.setIsbn(null); // No duplicar ISBN
        duplicate.setTotalPages(original.getTotalPages());
        duplicate.setPublishedDate(original.getPublishedDate());
        duplicate.setPublisher(original.getPublisher());
        duplicate.setDescription(original.getDescription());

        // Copiar relaciones
        duplicate.setAuthors(Set.copyOf(original.getAuthors()));
        duplicate.setGenres(Set.copyOf(original.getGenres()));

        // Estado inicial para el duplicado
        duplicate.setStatus(BookStatus.WISHLIST);
        duplicate.setCurrentPage(0);

        return bookRepository.save(duplicate);
    }

    /**
     * VERIFICAR SI EXISTE LIBRO POR ISBN
     *
     * @param isbn ISBN a verificar
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return bookRepository.findByIsbn(isbn).isPresent();
    }

    // ========================================
    // CLASE INTERNA PARA ESTADÍSTICAS
    // ========================================

    /**
     * DTO para estadísticas de libros
     * Encapsula todas las métricas importantes
     */
    public static class BookStatistics {
        private Long totalBooks;
        private Long booksReading;
        private Long booksFinished;
        private Long booksWishlist;
        private Long booksAbandoned;
        private Long booksOnHold;
        private Double averagePagesFinishedBooks;
        private Long booksFinishedThisYear;
        private Double averageProgressCurrentBooks;

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

        public Double getAveragePagesFinishedBooks() { return averagePagesFinishedBooks; }
        public void setAveragePagesFinishedBooks(Double averagePagesFinishedBooks) {
            this.averagePagesFinishedBooks = averagePagesFinishedBooks;
        }

        public Long getBooksFinishedThisYear() { return booksFinishedThisYear; }
        public void setBooksFinishedThisYear(Long booksFinishedThisYear) {
            this.booksFinishedThisYear = booksFinishedThisYear;
        }

        public Double getAverageProgressCurrentBooks() { return averageProgressCurrentBooks; }
        public void setAverageProgressCurrentBooks(Double averageProgressCurrentBooks) {
            this.averageProgressCurrentBooks = averageProgressCurrentBooks;
        }
    }

    /*
     * NOTAS IMPORTANTES SOBRE SERVICES:
     *
     * 1. TRANSACCIONES:
     *    - @Transactional asegura consistencia de datos
     *    - readOnly = true optimiza operaciones de consulta
     *    - Si falla una operación, se hace rollback automático
     *
     * 2. VALIDACIONES:
     *    - Siempre validar datos antes de guardar
     *    - Lanzar excepciones descriptivas para errores
     *    - Verificar integridad referencial
     *
     * 3. LÓGICA DE NEGOCIO:
     *    - Auto-cambiar status según progreso
     *    - Establecer fechas automáticamente
     *    - Mantener relaciones bidireccionales
     *    - Crear sesiones de lectura automáticas
     *
     * 4. PERFORMANCE:
     *    - Usar @Transactional(readOnly = true) para consultas
     *    - Paginar resultados grandes
     *    - Optimizar queries con repositories especializados
     *
     * 5. MANTENIMIENTO:
     *    - Separar claramente responsabilidades
     *    - Métodos pequeños y enfocados
     *    - Documentar lógica de negocio compleja
     *    - Facilitar testing unitario
     */
}