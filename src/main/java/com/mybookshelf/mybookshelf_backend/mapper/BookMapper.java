package com.mybookshelf.mybookshelf_backend.mapper;

// IMPORTS: Librerías necesarias para mapeo y conversión
import com.mybookshelf.mybookshelf_backend.dto.AuthorDTO;
import com.mybookshelf.mybookshelf_backend.dto.BookCreateDTO;
import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.dto.GenreDTO;
import com.mybookshelf.mybookshelf_backend.model.Author;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.Genre;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CLASE BookMapper - Convertidor entre Entity y DTO
 *
 * ¿Para qué sirve un Mapper?
 * - Convierte entidades JPA a DTOs para respuestas API
 * - Convierte DTOs de creación a entidades para persistir
 * - Maneja relaciones complejas (authors, genres)
 * - Calcula campos derivados automáticamente
 * - Evita exposición directa de entidades en API
 *
 * RESPONSABILIDADES:
 * 1. Entity → DTO (para respuestas GET)
 * 2. CreateDTO → Entity (para requests POST)
 * 3. Cálculo de campos derivados
 * 4. Manejo de relaciones bidireccionales
 * 5. Validación de datos durante conversión
 */
@Component // Spring gestiona este mapper como un bean
public class BookMapper {

    // ========================================
    // CONVERSIÓN ENTITY → DTO (Para respuestas)
    // ========================================

    /**
     * CONVIERTE Book Entity a BookDTO
     *
     * Usado para respuestas de API (GET requests)
     * Incluye todos los campos + campos calculados
     *
     * @param book Entidad Book a convertir
     * @return BookDTO para respuesta API
     */
    public BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }

        BookDTO dto = new BookDTO();

        // CAMPOS BÁSICOS - Mapeo directo
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setTotalPages(book.getTotalPages());
        dto.setCurrentPage(book.getCurrentPage());
        dto.setStatus(book.getStatus());

        // INFORMACIÓN ADICIONAL
        dto.setPublishedDate(book.getPublishedDate());
        dto.setPublisher(book.getPublisher());
        dto.setDescription(book.getDescription());

        // INFORMACIÓN PERSONAL
        dto.setPersonalRating(book.getPersonalRating());
        dto.setPersonalNotes(book.getPersonalNotes());
        dto.setStartDate(book.getStartDate());
        dto.setFinishDate(book.getFinishDate());

        // TIMESTAMPS
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());

        // RELACIONES - Convertir a DTOs para evitar lazy loading issues
        if (book.getAuthors() != null) {
            Set<AuthorDTO> authorDTOs = book.getAuthors().stream()
                    .map(this::authorToDTO)
                    .collect(Collectors.toSet());
            dto.setAuthors(authorDTOs);
        }

        if (book.getGenres() != null) {
            Set<GenreDTO> genreDTOs = book.getGenres().stream()
                    .map(this::genreToDTO)
                    .collect(Collectors.toSet());
            dto.setGenres(genreDTOs);
        }

        // ESTADÍSTICAS - Contar sesiones sin cargar la relación completa
        if (book.getReadingSessions() != null) {
            dto.setTotalReadingSessions((long) book.getReadingSessions().size());
        } else {
            dto.setTotalReadingSessions(0L);
        }

        // CAMPOS CALCULADOS - Llamar método que calcula todos los derivados
        dto.calculateDerivedFields();

        return dto;
    }

    /**
     * CONVIERTE Author Entity a AuthorDTO (simplificado)
     *
     * DTO básico para incluir en respuestas de Book
     * Sin información completa de Author (evita lazy loading)
     */
    private AuthorDTO authorToDTO(Author author) {
        if (author == null) {
            return null;
        }

        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setNationality(author.getNationality());
        dto.setBirthDate(author.getBirthDate());

        // Calcular campos derivados del autor
        dto.calculateDerivedFields();

        return dto;
    }

    /**
     * CONVIERTE Genre Entity a GenreDTO (simplificado)
     *
     * DTO básico para incluir en respuestas de Book
     */
    private GenreDTO genreToDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());

        // Calcular campos derivados del género
        dto.calculateDerivedFields();

        return dto;
    }

    // ========================================
    // CONVERSIÓN DTO → ENTITY (Para creación)
    // ========================================

    /**
     * CONVIERTE BookCreateDTO a Book Entity
     *
     * Usado para requests POST (crear libro)
     * NO incluye ID, timestamps (se auto-generan)
     * NO maneja relaciones (se setean después en Service)
     *
     * @param createDTO DTO con datos para crear libro
     * @return Book entity listo para persistir
     */
    public Book toEntity(BookCreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Book book = new Book();

        // CAMPOS BÁSICOS - Mapeo directo
        book.setTitle(createDTO.getTitle());
        book.setIsbn(createDTO.getIsbn());
        book.setTotalPages(createDTO.getTotalPages());
        book.setCurrentPage(createDTO.getCurrentPage());
        book.setStatus(createDTO.getStatus());

        // INFORMACIÓN ADICIONAL
        book.setPublishedDate(createDTO.getPublishedDate());
        book.setPublisher(createDTO.getPublisher());
        book.setDescription(createDTO.getDescription());

        // INFORMACIÓN PERSONAL INICIAL
        book.setPersonalRating(createDTO.getPersonalRating());
        book.setPersonalNotes(createDTO.getPersonalNotes());

        // RELACIONES - Inicializar como Sets vacíos
        // El Service se encargará de buscar y setear authors/genres por IDs
        book.setAuthors(new HashSet<>());
        book.setGenres(new HashSet<>());

        // LÓGICA DE NEGOCIO - Auto-setear startDate si está leyendo
        if (createDTO.getStatus() != null &&
                createDTO.getStatus().name().equals("READING") &&
                createDTO.getCurrentPage() != null &&
                createDTO.getCurrentPage() > 0) {
            book.setStartDate(java.time.LocalDate.now());
        }

        return book;
    }

    // ========================================
    // CONVERSIÓN PARA ACTUALIZACIÓN
    // ========================================

    /**
     * ACTUALIZA Book Entity con datos de BookCreateDTO
     *
     * Para requests PUT (actualizar libro existente)
     * Preserva ID y timestamps existentes
     * Solo actualiza campos que no son null en el DTO
     *
     * @param existingBook Book entity existente
     * @param updateDTO DTO con campos a actualizar
     * @return Book entity actualizada
     */
    public Book updateEntityFromDTO(Book existingBook, BookCreateDTO updateDTO) {
        if (existingBook == null || updateDTO == null) {
            return existingBook;
        }

        // ACTUALIZAR solo campos no nulos del DTO
        if (updateDTO.getTitle() != null) {
            existingBook.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getIsbn() != null) {
            existingBook.setIsbn(updateDTO.getIsbn());
        }
        if (updateDTO.getTotalPages() != null) {
            existingBook.setTotalPages(updateDTO.getTotalPages());
        }
        if (updateDTO.getCurrentPage() != null) {
            existingBook.setCurrentPage(updateDTO.getCurrentPage());
        }
        if (updateDTO.getStatus() != null) {
            existingBook.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getPublishedDate() != null) {
            existingBook.setPublishedDate(updateDTO.getPublishedDate());
        }
        if (updateDTO.getPublisher() != null) {
            existingBook.setPublisher(updateDTO.getPublisher());
        }
        if (updateDTO.getDescription() != null) {
            existingBook.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPersonalRating() != null) {
            existingBook.setPersonalRating(updateDTO.getPersonalRating());
        }
        if (updateDTO.getPersonalNotes() != null) {
            existingBook.setPersonalNotes(updateDTO.getPersonalNotes());
        }

        return existingBook;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * CONVIERTE lista de Books a lista de BookDTOs
     *
     * Útil para endpoints que devuelven múltiples libros
     */
    public Set<BookDTO> toDTOSet(Set<Book> books) {
        if (books == null) {
            return new HashSet<>();
        }
        return books.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    /**
     * VERIFICA si un BookCreateDTO es válido para conversión
     *
     * Validaciones adicionales más allá de @Valid
     */
    public boolean isValidForConversion(BookCreateDTO createDTO) {
        if (createDTO == null) {
            return false;
        }

        // Validaciones de negocio adicionales
        if (createDTO.getCurrentPage() != null &&
                createDTO.getTotalPages() != null &&
                createDTO.getCurrentPage() > createDTO.getTotalPages()) {
            return false; // Página actual no puede exceder total
        }

        return true;
    }

    /**
     * CREA DTO con información mínima para listas
     *
     * Para endpoints que necesitan datos básicos sin relaciones
     */
    public BookDTO toMinimalDTO(Book book) {
        if (book == null) {
            return null;
        }

        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setStatus(book.getStatus());
        dto.setCurrentPage(book.getCurrentPage());
        dto.setTotalPages(book.getTotalPages());

        // Solo campos calculados básicos
        dto.calculateDerivedFields();

        return dto;
    }

    /*
     * NOTAS IMPORTANTES SOBRE BookMapper:
     *
     * 1. SEPARACIÓN DE RESPONSABILIDADES:
     *    - Mapper: Solo conversión Entity ↔ DTO
     *    - Service: Lógica de negocio y persistencia
     *    - Controller: Manejo HTTP y validación
     *
     * 2. MANEJO DE RELACIONES:
     *    - toDTO(): Convierte relaciones a DTOs simples
     *    - toEntity(): NO maneja relaciones (lo hace Service)
     *    - Evita lazy loading exceptions
     *
     * 3. CAMPOS CALCULADOS:
     *    - Se calculan en los DTOs, no en entities
     *    - calculateDerivedFields() se llama automáticamente
     *    - Datos derivados no se persisten en BD
     *
     * 4. PERFORMANCE:
     *    - toMinimalDTO() para listas grandes
     *    - Evita cargar relaciones innecesarias
     *    - Streams para conversiones de colecciones
     *
     * 5. VALIDACIÓN:
     *    - isValidForConversion() para validaciones extra
     *    - Complementa @Valid de los DTOs
     *    - Validaciones de negocio específicas
     */
}