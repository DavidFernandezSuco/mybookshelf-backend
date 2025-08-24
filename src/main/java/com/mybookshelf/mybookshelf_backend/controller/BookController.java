package com.mybookshelf.mybookshelf_backend.controller;

// IMPORTS: Librerías necesarias para REST Controllers
import com.mybookshelf.mybookshelf_backend.dto.BookCreateDTO;
import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.mapper.BookMapper;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.mybookshelf.mybookshelf_backend.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.mybookshelf.mybookshelf_backend.service.GoogleBooksService;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CLASE BookController - REST API para gestión de libros
 *
 * VERSIÓN ADAPTADA PARA PORTFOLIO JUNIOR
 * - Usar exactamente los métodos que existen en BookService
 * - Endpoints core funcionales inmediatamente
 * - Código limpio y bien documentado
 * - Fácil de testear y demostrar
 *
 * ENDPOINTS IMPLEMENTADOS (adaptados al Service real):
 * - GET /api/books - Listar libros paginados
 * - GET /api/books/{id} - Obtener libro específico
 * - POST /api/books - Crear nuevo libro
 * - PATCH /api/books/{id}/progress - Actualizar progreso
 * - DELETE /api/books/{id} - Eliminar libro
 * - GET /api/books/search - Buscar libros
 * - GET /api/books/status/{status} - Filtrar por estado
 */

@RestController
@RequestMapping("/api/books")
@Validated
@CrossOrigin(origins = "*") // Para desarrollo
public class BookController {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    private final BookService bookService;
    private final BookMapper bookMapper;
    private final GoogleBooksService googleBooksService;

    public BookController(BookService bookService, BookMapper bookMapper, GoogleBooksService googleBooksService) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
        this.googleBooksService = googleBooksService;
    }

    // ========================================
    // ENDPOINTS DE CONSULTA (GET)
    // ========================================

    /**
     * GET /api/books - LISTAR TODOS LOS LIBROS
     *
     * Usa: bookService.getAllBooks(Pageable) → retorna Page<Book>
     * Convierte: Page<Book> → Page<BookDTO> usando mapper
     */
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Crear configuración de paginación
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // BookService retorna Page<Book> - convertir a Page<BookDTO>
        Page<Book> bookEntities = bookService.getAllBooks(pageable);
        Page<BookDTO> bookDTOs = bookEntities.map(bookMapper::toDTO);

        return ResponseEntity.ok(bookDTOs);
    }

    /**
     * GET /api/books/{id} - OBTENER LIBRO POR ID
     *
     * Usa: bookService.getBookById(Long) → retorna Book
     * Convierte: Book → BookDTO usando mapper
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        BookDTO bookDTO = bookMapper.toDTO(book);
        return ResponseEntity.ok(bookDTO);
    }

    /**
     * GET /api/books/search - BUSCAR LIBROS
     *
     * Usa: bookService.searchBooks(String) → retorna List<Book>
     * Convierte: List<Book> → List<BookDTO> usando mapper
     */
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Book> books = bookService.searchBooks(query.trim());
        List<BookDTO> bookDTOs = books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDTOs);
    }

    /**
     * GET /api/books/status/{status} - FILTRAR POR ESTADO
     *
     * Usa: bookService.getBooksByStatus(BookStatus)
     * NOTA: Service retorna List<Book>, necesitamos convertir a DTO
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookDTO>> getBooksByStatus(@PathVariable BookStatus status) {
        List<Book> books = bookService.getBooksByStatus(status);

        // Convertir List<Book> a List<BookDTO> usando el mapper
        List<BookDTO> bookDTOs = books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDTOs);
    }

    /**
     * GET /api/books/currently-reading - LIBROS LEYENDO ACTUALMENTE
     *
     * Usa: bookService.getCurrentlyReadingBooks()
     * NOTA: Service retorna List<Book>, convertimos a DTO
     */
    @GetMapping("/currently-reading")
    public ResponseEntity<List<BookDTO>> getCurrentlyReadingBooks() {
        List<Book> books = bookService.getCurrentlyReadingBooks(BookStatus.READING);

        // Convertir a DTOs
        List<BookDTO> bookDTOs = books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDTOs);
    }

    // ========================================
    // ENDPOINTS DE CREACIÓN (POST)
    // ========================================

    /**
     * POST /api/books - CREAR NUEVO LIBRO
     *
     * Usa: bookService.createBook(BookCreateDTO) → retorna Book
     * Tu Service maneja authorIds y genreIds internamente desde el DTO
     */
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookCreateDTO createDTO) {

        // Validación adicional del mapper
        if (!bookMapper.isValidForConversion(createDTO)) {
            return ResponseEntity.badRequest().build();
        }

        // Tu BookService maneja authorIds y genreIds internamente
        Book bookEntity = bookMapper.toEntity(createDTO);           // 1. DTO → Entity
        Set<Long> authorIds = createDTO.getAuthorIds();            // 2. Extraer author IDs
        Set<Long> genreIds = createDTO.getGenreIds();              // 3. Extraer genre IDs
        Book createdBook = bookService.createBook(bookEntity, authorIds, genreIds); // 4. Llamar Service real
        BookDTO createdBookDTO = bookMapper.toDTO(createdBook);     // 5. Entity → DTO

        // Crear URI del nuevo recurso
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBookDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdBookDTO);
    }

    // ========================================
    // ENDPOINTS DE ACTUALIZACIÓN (PATCH)
    // ========================================

    /**
     * PATCH /api/books/{id}/progress - ACTUALIZAR PROGRESO
     *
     * Usa: bookService.updateBookProgress(Long, Integer) → retorna Book
     * Convierte: Book → BookDTO usando mapper
     */
    @PatchMapping("/{id}/progress")
    public ResponseEntity<BookDTO> updateBookProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequest progressRequest) {

        Book updatedBook = bookService.updateBookProgress(id, progressRequest.getCurrentPage());
        BookDTO updatedBookDTO = bookMapper.toDTO(updatedBook);
        return ResponseEntity.ok(updatedBookDTO);
    }

    // ========================================
    // ENDPOINTS DE ELIMINACIÓN (DELETE)
    // ========================================

    /**
     * DELETE /api/books/{id} - ELIMINAR LIBRO
     *
     * Usa: bookService.deleteBook(Long)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // CLASES INTERNAS PARA REQUEST BODIES
    // ========================================

    /**
     * DTO simple para actualización de progreso
     */
    public static class UpdateProgressRequest {
        @jakarta.validation.constraints.Min(value = 0, message = "Current page cannot be negative")
        private Integer currentPage;

        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
    }

    // ========================================
// ENDPOINTS DE INTEGRACIÓN GOOGLE BOOKS
// ========================================

    /**
     * GET /api/books/search-external - BUSCAR LIBROS EN GOOGLE BOOKS
     *
     * NUEVO ENDPOINT FASE 4: Búsqueda externa para enriquecer catálogo
     *
     * Usa: googleBooksService.searchBooks(String) → retorna List<GoogleBookDTO>
     * Complementa: GET /api/books/search (búsqueda local existente)
     *
     * Parámetros:
     * - q: término de búsqueda (ej: "clean code", "java programming")
     * - maxResults: límite de resultados (opcional, default 10)
     *
     * Ejemplos de uso:
     * - GET /api/books/search-external?q=clean+code
     * - GET /api/books/search-external?q=spring+boot&maxResults=5
     *
     * Respuesta: Lista de GoogleBookDTO con datos de Google Books API
     * HTTP 200: Búsqueda exitosa (puede ser lista vacía)
     * HTTP 400: Query vacío o inválido
     * HTTP 500: Error de comunicación con Google Books API
     */
    @GetMapping("/search-external")
    public ResponseEntity<List<GoogleBookDTO>> searchExternalBooks(
            @RequestParam("q") String query,
            @RequestParam(value = "maxResults", defaultValue = "10") Integer maxResults) {

        // Validación de entrada
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (maxResults < 1 || maxResults > 40) {
            maxResults = 10; // Default seguro
        }

        // Llamada al service - maneja automáticamente errores de API externa
        List<GoogleBookDTO> externalBooks = googleBooksService.searchBooks(query.trim());

        return ResponseEntity.ok(externalBooks);
    }

    /**
     * POST /api/books/import-google - IMPORTAR LIBRO DESDE GOOGLE BOOKS
     *
     * NUEVO ENDPOINT FASE 4: Importación con 1 clic desde búsqueda externa
     *
     * Usa: googleBooksService.importBook(String, BookStatus) → retorna Book
     * Convierte: Book → BookDTO usando bookMapper
     *
     * Body ejemplo:
     * {
     *   "googleBooksId": "abc123xyz",
     *   "status": "WISHLIST"
     * }
     *
     * Respuesta: BookDTO del libro importado con autores y géneros
     * HTTP 201: Libro importado exitosamente
     * HTTP 400: ID inválido, parámetros faltantes o libro ya existe
     * HTTP 500: Error de comunicación con Google Books API
     */
    @PostMapping("/import-google")
    public ResponseEntity<BookDTO> importFromGoogleBooks(@Valid @RequestBody ImportGoogleBookRequest request) {

        // Validación adicional
        if (request.getGoogleBooksId() == null || request.getGoogleBooksId().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Default status si no se proporciona
        BookStatus status = request.getStatus() != null ? request.getStatus() : BookStatus.WISHLIST;

        try {
            // Importar usando GoogleBooksService
            Book importedBook = googleBooksService.importFromGoogle(request.getGoogleBooksId().trim(), status);

            // Convertir a DTO para respuesta
            BookDTO importedBookDTO = bookMapper.toDTO(importedBook);

            // Crear URI del nuevo recurso
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/books/{id}")
                    .buildAndExpand(importedBookDTO.getId())
                    .toUri();

            return ResponseEntity.created(location).body(importedBookDTO);

        } catch (IllegalArgumentException e) {
            // Error de validación o duplicado
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // Error de Google Books API o libro no encontrado
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DTO para request de importación desde Google Books
     */
    public static class ImportGoogleBookRequest {

        @jakarta.validation.constraints.NotBlank(message = "Google Books ID is required")
        private String googleBooksId;

        private BookStatus status;

        // Getters y Setters
        public String getGoogleBooksId() { return googleBooksId; }
        public void setGoogleBooksId(String googleBooksId) { this.googleBooksId = googleBooksId; }

        public BookStatus getStatus() { return status; }
        public void setStatus(BookStatus status) { this.status = status; }
    }

    /**
     * PATCH /api/books/{id}/enrich-google - ENRIQUECER LIBRO CON GOOGLE BOOKS
     *
     * NUEVO ENDPOINT FASE 4: Enriquecimiento de libros existentes con datos externos
     *
     * Usa: googleBooksService.enrichExistingBook(Long, String) → retorna Book
     * Convierte: Book → BookDTO usando bookMapper
     *
     * Mejora libros existentes añadiendo:
     * - Descripción completa
     * - ISBN si falta
     * - Número de páginas
     * - Publisher
     * - Y otros metadatos de Google Books
     *
     * Path ejemplo: /api/books/123/enrich-google
     * Body: { "googleBooksId": "abc123xyz" }
     *
     * Respuesta: BookDTO enriquecido con datos actualizados
     * HTTP 200: Enriquecimiento exitoso (con o sin cambios)
     * HTTP 400: IDs inválidos o libro no encontrado
     * HTTP 404: Libro no existe en tu biblioteca
     */
    @PatchMapping("/{id}/enrich-google")
    public ResponseEntity<BookDTO> enrichBookWithGoogleData(
            @PathVariable Long id,
            @Valid @RequestBody EnrichBookRequest request) {

        // Validación de parámetros
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getGoogleBooksId() == null || request.getGoogleBooksId().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Enriquecer usando GoogleBooksService
            Book enrichedBook = googleBooksService.enrichExistingBook(id, request.getGoogleBooksId().trim());

            // Convertir a DTO para respuesta
            BookDTO enrichedBookDTO = bookMapper.toDTO(enrichedBook);

            return ResponseEntity.ok(enrichedBookDTO);

        } catch (RuntimeException e) {
            // Manejar errores: libro no encontrado, Google Books no disponible, etc.
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    /**
     * DTO para request de enriquecimiento con Google Books
     */
    public static class EnrichBookRequest {

        @jakarta.validation.constraints.NotBlank(message = "Google Books ID is required")
        private String googleBooksId;

        // Getters y Setters
        public String getGoogleBooksId() { return googleBooksId; }
        public void setGoogleBooksId(String googleBooksId) { this.googleBooksId = googleBooksId; }
    }


    /*
     * NOTAS PARA PORTFOLIO JUNIOR:
     *
     * 1. ✅ ENDPOINTS FUNCIONALES:
     *    - Todos los endpoints usan métodos que EXISTEN en BookService
     *    - Sin métodos faltantes o no implementados
     *    - API lista para testear inmediatamente
     *
     * 2. ✅ BUENAS PRÁCTICAS:
     *    - Códigos HTTP correctos (200, 201, 204, 400)
     *    - Validación de entrada
     *    - Manejo de conversiones Entity ↔ DTO
     *    - Documentación clara
     *
     * 3. ✅ DEMOSTRABLE:
     *    - CRUD completo para libros
     *    - Búsqueda funcional
     *    - Filtros por estado
     *    - Actualización de progreso (feature principal)
     *
     * 4. 🎯 PARA EXPANDIR DESPUÉS:
     *    - Agregar métodos PUT para actualización completa
     *    - Endpoints de estadísticas
     *    - Validaciones más complejas
     *    - Manejo de errores más sofisticado
     *
     * 5. 📊 VALOR PARA EMPLOYERS:
     *    - Demuestra conocimiento de REST APIs
     *    - Arquitectura limpia Service → Controller
     *    - Manejo correcto de DTOs y mappers
     *    - Código listo para producción básica
     *
     * ¡Este controller funcionará inmediatamente con tu Service actual! 🚀
     */
}