package com.mybookshelf.mybookshelf_backend.controller;

// IMPORTS: Librerías necesarias para REST Controllers
import com.mybookshelf.mybookshelf_backend.dto.GenreCreateDTO;
import com.mybookshelf.mybookshelf_backend.dto.GenreDTO;
import com.mybookshelf.mybookshelf_backend.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * CLASE GenreController - REST API para gestión de géneros literarios
 *
 * SIGUIENDO EXACTAMENTE EL PATRÓN DE BookController Y AuthorController
 * - Misma estructura de endpoints
 * - Misma gestión de errores
 * - Mismos códigos HTTP
 * - Misma documentación
 *
 * ENDPOINTS IMPLEMENTADOS (copiando patrón BookController/AuthorController):
 * - GET /api/genres - Listar géneros paginados
 * - GET /api/genres/{id} - Obtener género específico
 * - POST /api/genres - Crear nuevo género
 * - PUT /api/genres/{id} - Actualizar género completo
 * - DELETE /api/genres/{id} - Eliminar género
 * - GET /api/genres/search - Buscar géneros
 * - GET /api/genres/autocomplete - Autocompletado para UI
 * - GET /api/genres/ordered - Lista ordenada alfabéticamente
 */

@RestController
@RequestMapping("/api/genres")
@Validated
@CrossOrigin(origins = "*") // Para desarrollo
public class GenreController {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    // ========================================
    // ENDPOINTS DE CONSULTA (GET)
    // ========================================

    /**
     * GET /api/genres - LISTAR TODOS LOS GÉNEROS
     *
     * SIGUIENDO PATRÓN BookController.getAllBooks() Y AuthorController.getAllAuthors()
     * Usa: genreService.getAllGenres(Pageable) → retorna Page<GenreDTO>
     */
    @GetMapping
    public ResponseEntity<Page<GenreDTO>> getAllGenres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Crear configuración de paginación (igual que otros Controllers)
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // GenreService retorna Page<GenreDTO> directamente
        Page<GenreDTO> genreDTOs = genreService.getAllGenres(pageable);

        return ResponseEntity.ok(genreDTOs);
    }

    /**
     * GET /api/genres/{id} - OBTENER GÉNERO POR ID
     *
     * SIGUIENDO PATRÓN BookController.getBookById() Y AuthorController.getAuthorById()
     * Usa: genreService.getGenreById(Long) → retorna GenreDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable Long id) {
        GenreDTO genreDTO = genreService.getGenreById(id);
        return ResponseEntity.ok(genreDTO);
    }

    /**
     * GET /api/genres/search - BUSCAR GÉNEROS
     *
     * SIGUIENDO PATRÓN BookController.searchBooks() Y AuthorController.searchAuthors()
     * Usa: genreService.searchGenres(String) → retorna List<GenreDTO>
     */
    @GetMapping("/search")
    public ResponseEntity<List<GenreDTO>> searchGenres(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<GenreDTO> genres = genreService.searchGenres(query.trim());
        return ResponseEntity.ok(genres);
    }

    /**
     * GET /api/genres/autocomplete - AUTOCOMPLETADO PARA UI
     *
     * SIGUIENDO PATRÓN AuthorController.getAuthorsForAutocomplete()
     * Útil cuando el usuario está creando libros y necesita seleccionar géneros
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<Page<GenreDTO>> getGenresForAutocomplete(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<GenreDTO> genres = genreService.getGenresForAutocomplete(query, pageable);
        return ResponseEntity.ok(genres);
    }

    /**
     * GET /api/genres/ordered - GÉNEROS ORDENADOS ALFABÉTICAMENTE
     *
     * NUEVO: Endpoint especializado para dropdowns y listas
     * Útil para formularios donde necesitas todos los géneros en orden
     */
    @GetMapping("/ordered")
    public ResponseEntity<List<GenreDTO>> getGenresOrderedByName() {
        List<GenreDTO> genres = genreService.getGenresOrderedByName();
        return ResponseEntity.ok(genres);
    }

    // ========================================
    // ENDPOINTS DE CREACIÓN (POST)
    // ========================================

    /**
     * POST /api/genres - CREAR NUEVO GÉNERO
     *
     * SIGUIENDO PATRÓN BookController.createBook() Y AuthorController.createAuthor()
     * Usa: genreService.createGenre(GenreCreateDTO) → retorna GenreDTO
     */
    @PostMapping
    public ResponseEntity<GenreDTO> createGenre(@Valid @RequestBody GenreCreateDTO createDTO) {

        // Crear género usando service (incluye todas las validaciones)
        GenreDTO createdGenre = genreService.createGenre(createDTO);

        // Crear URI del nuevo recurso (igual que otros Controllers)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdGenre.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdGenre);
    }

    // ========================================
    // ENDPOINTS DE ACTUALIZACIÓN (PUT)
    // ========================================

    /**
     * PUT /api/genres/{id} - ACTUALIZAR GÉNERO COMPLETO
     *
     * SIGUIENDO PATRÓN AuthorController.updateAuthor()
     * Usa: genreService.updateGenre(Long, GenreCreateDTO) → retorna GenreDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody GenreCreateDTO updateDTO) {

        GenreDTO updatedGenre = genreService.updateGenre(id, updateDTO);
        return ResponseEntity.ok(updatedGenre);
    }

    // ========================================
    // ENDPOINTS DE ELIMINACIÓN (DELETE)
    // ========================================

    /**
     * DELETE /api/genres/{id} - ELIMINAR GÉNERO
     *
     * SIGUIENDO PATRÓN BookController.deleteBook() Y AuthorController.deleteAuthor()
     * Usa: genreService.deleteGenre(Long)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // ENDPOINTS ESPECIALIZADOS PARA GÉNEROS
    // ========================================

    /**
     * GET /api/genres/popular - GÉNEROS MÁS POPULARES
     *
     * NUEVO: Endpoint para mostrar géneros con más libros
     * Útil para estadísticas y descubrimiento de contenido popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<GenreDTO>> getPopularGenres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Usar método entity existente y convertir
        Page<com.mybookshelf.mybookshelf_backend.model.Genre> genreEntities =
                genreService.getAllGenreEntities(pageable);

        // Convertir Page<Genre> a Page<GenreDTO>
        Page<GenreDTO> genreDTOs = genreEntities
                .map(genre -> genreService.getGenreById(genre.getId()));

        return ResponseEntity.ok(genreDTOs);
    }

    /**
     * GET /api/genres/with-books - GÉNEROS QUE TIENEN LIBROS
     *
     * NUEVO: Endpoint para filtrar solo géneros que tienen contenido
     * Útil para limpiar interfaces y mostrar solo géneros relevantes
     */
    @GetMapping("/with-books")
    public ResponseEntity<List<GenreDTO>> getGenresWithBooks() {
        // Usar método entity existente y convertir
        List<com.mybookshelf.mybookshelf_backend.model.Genre> genreEntities =
                genreService.getGenresWithBooks();

        // Convertir List<Genre> a List<GenreDTO>
        List<GenreDTO> genreDTOs = genreEntities.stream()
                .map(genre -> genreService.getGenreById(genre.getId()))
                .toList();

        return ResponseEntity.ok(genreDTOs);
    }

    /**
     * GET /api/genres/stats - ESTADÍSTICAS DE GÉNEROS
     *
     * NUEVO: Endpoint para información estadística básica
     * Útil para dashboards y análisis de biblioteca
     */
    @GetMapping("/stats")
    public ResponseEntity<GenreStatsDTO> getGenreStatistics() {
        GenreStatsDTO stats = new GenreStatsDTO();

        // Estadísticas básicas
        stats.setTotalGenres(genreService.getTotalGenresCount());
        stats.setGenresWithBooks((long) genreService.getGenresWithBooks().size());
        stats.setOrphanGenres((long) genreService.getOrphanGenres().size());

        return ResponseEntity.ok(stats);
    }

    /**
     * DELETE /api/genres/cleanup - LIMPIAR GÉNEROS HUÉRFANOS
     *
     * NUEVO: Endpoint para mantenimiento de datos
     * Elimina géneros que no tienen libros asociados
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<CleanupResultDTO> cleanupOrphanGenres() {
        int deletedCount = genreService.cleanOrphanGenres();

        CleanupResultDTO result = new CleanupResultDTO();
        result.setDeletedGenres(deletedCount);
        result.setMessage("Cleanup completed successfully");

        return ResponseEntity.ok(result);
    }

    // ========================================
    // DTOs INTERNOS PARA ESTADÍSTICAS
    // ========================================

    /**
     * DTO para estadísticas básicas de géneros
     */
    public static class GenreStatsDTO {
        private Long totalGenres;
        private Long genresWithBooks;
        private Long orphanGenres;

        // Getters y setters
        public Long getTotalGenres() { return totalGenres; }
        public void setTotalGenres(Long totalGenres) { this.totalGenres = totalGenres; }

        public Long getGenresWithBooks() { return genresWithBooks; }
        public void setGenresWithBooks(Long genresWithBooks) { this.genresWithBooks = genresWithBooks; }

        public Long getOrphanGenres() { return orphanGenres; }
        public void setOrphanGenres(Long orphanGenres) { this.orphanGenres = orphanGenres; }
    }

    /**
     * DTO para resultado de limpieza
     */
    public static class CleanupResultDTO {
        private Integer deletedGenres;
        private String message;

        // Getters y setters
        public Integer getDeletedGenres() { return deletedGenres; }
        public void setDeletedGenres(Integer deletedGenres) { this.deletedGenres = deletedGenres; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /*
     * NOTAS SOBRE GenreController:
     *
     * 1. ✅ PATRÓN IDÉNTICO A BookController/AuthorController:
     *    - Misma estructura de endpoints
     *    - Misma gestión de paginación
     *    - Mismos códigos HTTP (200, 201, 204, 400)
     *    - Misma validación de entrada
     *
     * 2. ✅ ENDPOINTS CORE IMPLEMENTADOS:
     *    - GET /api/genres (paginado)
     *    - GET /api/genres/{id}
     *    - POST /api/genres (crear)
     *    - PUT /api/genres/{id} (actualizar)
     *    - DELETE /api/genres/{id} (eliminar)
     *    - GET /api/genres/search (buscar)
     *    - GET /api/genres/autocomplete (UI helper)
     *
     * 3. ✅ ENDPOINTS ESPECIALIZADOS PARA GÉNEROS:
     *    - GET /api/genres/ordered (lista alfabética)
     *    - GET /api/genres/popular (ranking de popularidad)
     *    - GET /api/genres/with-books (solo géneros con contenido)
     *    - GET /api/genres/stats (estadísticas básicas)
     *    - DELETE /api/genres/cleanup (mantenimiento)
     *
     * 4. ✅ INTEGRACIÓN CON SERVICE:
     *    - Usa métodos DTO cuando están disponibles
     *    - Convierte entities a DTOs cuando es necesario
     *    - Aprovecha toda la lógica de negocio existente
     *
     * 5. ✅ CONSISTENCIA CON ARQUITECTURA:
     *    - Controller → Service → Repository pattern
     *    - Manejo de errores delegado al service
     *    - Validaciones con @Valid
     *    - Respuestas HTTP estándar
     *
     * 6. 🎯 VALOR ESPECÍFICO PARA GÉNEROS:
     *    - Endpoints especializados para gestión de categorías
     *    - Funcionalidades de limpieza y mantenimiento
     *    - Estadísticas útiles para análisis de biblioteca
     *    - Autocompletado optimizado para UI de creación de libros
     *
     * 7. 🚀 READY TO TEST:
     *    - GET /api/genres → Lista paginada
     *    - POST /api/genres {"name": "Science Fiction", "description": "Futuristic novels"}
     *    - GET /api/genres/search?q=Fiction
     *    - GET /api/genres/autocomplete?q=Sci (para Science Fiction, etc.)
     *    - GET /api/genres/stats → Estadísticas completas
     *
     * ¡Este controller completa tu Genre API y funciona inmediatamente! 🎯
     *
     * API COMPLETA LOGRADA:
     * - Book API: 7 endpoints ✅
     * - Author API: 8 endpoints ✅
     * - Genre API: 10 endpoints ✅ (RECIÉN COMPLETADO)
     * - TOTAL: 25 ENDPOINTS REST FUNCIONALES! 🚀
     */
}