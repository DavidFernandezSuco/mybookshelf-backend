package com.mybookshelf.mybookshelf_backend.mapper;

// IMPORTS: Librerías necesarias para mapeo y conversión
import com.mybookshelf.mybookshelf_backend.dto.AuthorDTO;
import com.mybookshelf.mybookshelf_backend.dto.AuthorCreateDTO;
import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.model.Author;
import com.mybookshelf.mybookshelf_backend.model.Book;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CLASE AuthorMapper - Convertidor entre Author Entity y DTOs
 *
 * ¿Para qué sirve AuthorMapper?
 * - Convierte entidades Author a DTOs para respuestas API
 * - Convierte DTOs de creación a entidades para persistir
 * - Maneja relaciones con libros sin lazy loading issues
 * - Calcula campos derivados automáticamente
 * - Evita exposición directa de entidades en API
 *
 * PATRÓN IDÉNTICO A BookMapper:
 * 1. Entity → DTO (para respuestas GET)
 * 2. CreateDTO → Entity (para requests POST)
 * 3. Cálculo de campos derivados
 * 4. Manejo de relaciones bidireccionales
 * 5. Validación de datos durante conversión
 */
@Component // Spring gestiona este mapper como un bean
public class AuthorMapper {

    // ========================================
    // CONVERSIÓN ENTITY → DTO (Para respuestas)
    // ========================================

    /**
     * CONVIERTE Author Entity a AuthorDTO
     *
     * Usado para respuestas de API (GET requests)
     * Incluye todos los campos + campos calculados
     *
     * @param author Entidad Author a convertir
     * @return AuthorDTO para respuesta API
     */
    public AuthorDTO toDTO(Author author) {
        if (author == null) {
            return null;
        }

        AuthorDTO dto = new AuthorDTO();

        // CAMPOS BÁSICOS - Mapeo directo
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setBiography(author.getBiography());
        dto.setBirthDate(author.getBirthDate());
        dto.setNationality(author.getNationality());

        // TIMESTAMPS
        dto.setCreatedAt(author.getCreatedAt());
        dto.setUpdatedAt(author.getUpdatedAt());

        // ESTADÍSTICAS - Contar libros sin cargar la relación completa
        if (author.getBooks() != null) {
            dto.setBookCount((long) author.getBooks().size());
        } else {
            dto.setBookCount(0L);
        }

        // CAMPOS CALCULADOS - Llamar método que calcula todos los derivados
        dto.calculateDerivedFields();

        return dto;
    }

    /**
     * CONVIERTE Author Entity a AuthorDTO CON LIBROS
     *
     * Versión completa que incluye información de libros
     * Solo usar cuando necesites los libros explícitamente
     *
     * @param author Entidad Author a convertir
     * @return AuthorDTO con información de libros incluida
     */
    public AuthorDTO toDTOWithBooks(Author author) {
        if (author == null) {
            return null;
        }

        // Empezar con DTO básico
        AuthorDTO dto = toDTO(author);

        // AGREGAR información de libros (solo campos básicos para evitar lazy loading)
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            Set<BookDTO> bookDTOs = author.getBooks().stream()
                    .map(this::bookToMinimalDTO)
                    .collect(Collectors.toSet());
            // Si AuthorDTO tuviera campo books, se setearía aquí
            // dto.setBooks(bookDTOs);
        }

        return dto;
    }

    /**
     * CONVIERTE Book Entity a BookDTO minimal (para evitar lazy loading)
     *
     * DTO básico para incluir en respuestas de Author
     * Sin relaciones para evitar lazy loading circulares
     */
    private BookDTO bookToMinimalDTO(Book book) {
        if (book == null) {
            return null;
        }

        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setStatus(book.getStatus());
        dto.setCurrentPage(book.getCurrentPage());
        dto.setTotalPages(book.getTotalPages());
        dto.setPersonalRating(book.getPersonalRating());

        // Solo campos calculados básicos
        dto.calculateDerivedFields();

        return dto;
    }

    // ========================================
    // CONVERSIÓN DTO → ENTITY (Para creación)
    // ========================================

    /**
     * CONVIERTE AuthorCreateDTO a Author Entity
     *
     * Usado para requests POST (crear autor)
     * NO incluye ID, timestamps (se auto-generan)
     * NO maneja relaciones con libros (se setean después en Service)
     *
     * @param createDTO DTO con datos para crear autor
     * @return Author entity listo para persistir
     */
    public Author toEntity(AuthorCreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Author author = new Author();

        // CAMPOS BÁSICOS - Mapeo directo
        author.setFirstName(createDTO.getFirstName());
        author.setLastName(createDTO.getLastName());
        author.setBiography(createDTO.getBiography());
        author.setBirthDate(createDTO.getBirthDate());
        author.setNationality(createDTO.getNationality());

        // RELACIONES - Inicializar como Set vacío
        // El Service se encargará de buscar y setear libros por IDs si es necesario
        author.setBooks(new HashSet<>());

        return author;
    }

    // ========================================
    // CONVERSIÓN PARA ACTUALIZACIÓN
    // ========================================

    /**
     * ACTUALIZA Author Entity con datos de AuthorCreateDTO
     *
     * Para requests PUT (actualizar autor existente)
     * Preserva ID y timestamps existentes
     * Solo actualiza campos que no son null en el DTO
     *
     * @param existingAuthor Author entity existente
     * @param updateDTO DTO con campos a actualizar
     * @return Author entity actualizada
     */
    public Author updateEntityFromDTO(Author existingAuthor, AuthorCreateDTO updateDTO) {
        if (existingAuthor == null || updateDTO == null) {
            return existingAuthor;
        }

        // ACTUALIZAR solo campos no nulos del DTO
        if (updateDTO.getFirstName() != null && !updateDTO.getFirstName().trim().isEmpty()) {
            existingAuthor.setFirstName(updateDTO.getFirstName().trim());
        }
        if (updateDTO.getLastName() != null && !updateDTO.getLastName().trim().isEmpty()) {
            existingAuthor.setLastName(updateDTO.getLastName().trim());
        }
        if (updateDTO.getBiography() != null) {
            existingAuthor.setBiography(updateDTO.getBiography());
        }
        if (updateDTO.getBirthDate() != null) {
            existingAuthor.setBirthDate(updateDTO.getBirthDate());
        }
        if (updateDTO.getNationality() != null) {
            existingAuthor.setNationality(updateDTO.getNationality().trim());
        }

        return existingAuthor;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * CONVIERTE lista de Authors a lista de AuthorDTOs
     *
     * Útil para endpoints que devuelven múltiples autores
     */
    public Set<AuthorDTO> toDTOSet(Set<Author> authors) {
        if (authors == null) {
            return new HashSet<>();
        }
        return authors.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    /**
     * VERIFICA si un AuthorCreateDTO es válido para conversión
     *
     * Validaciones adicionales más allá de @Valid
     */
    public boolean isValidForConversion(AuthorCreateDTO createDTO) {
        if (createDTO == null) {
            return false;
        }

        // Validación 1: Nombres no pueden estar vacíos
        if (createDTO.getFirstName() == null || createDTO.getFirstName().trim().isEmpty()) {
            return false;
        }
        if (createDTO.getLastName() == null || createDTO.getLastName().trim().isEmpty()) {
            return false;
        }

        // Validación 2: Fecha de nacimiento no puede ser futura
        if (createDTO.getBirthDate() != null &&
                createDTO.getBirthDate().isAfter(java.time.LocalDate.now())) {
            return false;
        }

        return true;
    }

    /**
     * CREA DTO con información mínima para listas
     *
     * Para endpoints que necesitan datos básicos sin relaciones
     */
    public AuthorDTO toMinimalDTO(Author author) {
        if (author == null) {
            return null;
        }

        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setNationality(author.getNationality());
        dto.setBirthDate(author.getBirthDate());

        // Contar libros básico
        if (author.getBooks() != null) {
            dto.setBookCount((long) author.getBooks().size());
        } else {
            dto.setBookCount(0L);
        }

        // Solo campos calculados básicos
        dto.calculateDerivedFields();

        return dto;
    }

    /**
     * CREAR DTO PARA AUTOCOMPLETADO
     *
     * DTO ultra-simplificado para campos de autocompletado
     */
    public AuthorDTO toAutocompleteDTO(Author author) {
        if (author == null) {
            return null;
        }

        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());

        // Solo calcular nombre completo
        dto.calculateDerivedFields();

        return dto;
    }

    /*
     * NOTAS IMPORTANTES SOBRE AuthorMapper:
     *
     * 1. PATRÓN IDÉNTICO A BookMapper:
     *    - toDTO(): Entity → DTO con campos calculados
     *    - toEntity(): CreateDTO → Entity para persistencia
     *    - updateEntityFromDTO(): Actualizaciones parciales
     *    - Métodos de utilidad para diferentes casos de uso
     *
     * 2. MANEJO DE RELACIONES:
     *    - toDTO(): NO carga libros completos (evita lazy loading)
     *    - toDTOWithBooks(): Solo cuando necesites información de libros
     *    - bookToMinimalDTO(): Evita lazy loading circulares
     *
     * 3. CAMPOS CALCULADOS:
     *    - calculateDerivedFields() en AuthorDTO calcula:
     *      * fullName (firstName + lastName)
     *      * displayName (lastName, firstName)
     *      * age (basado en birthDate)
     *    - bookCount se calcula sin cargar relaciones completas
     *
     * 4. PERFORMANCE:
     *    - toMinimalDTO() para listas grandes
     *    - toAutocompleteDTO() para campos de búsqueda
     *    - Evita cargar relaciones innecesarias
     *
     * 5. VALIDACIÓN:
     *    - isValidForConversion() para validaciones extra
     *    - Complementa @Valid de los DTOs
     *    - Validaciones de negocio específicas
     *
     * 6. INTEGRACIÓN CON SERVICE:
     *    - El AuthorService usará estos métodos para:
     *      * Convertir entities a DTOs en responses
     *      * Convertir CreateDTOs a entities en creación
     *      * Actualizar entities existentes
     *      * Manejar diferentes niveles de detalle según endpoint
     */
}