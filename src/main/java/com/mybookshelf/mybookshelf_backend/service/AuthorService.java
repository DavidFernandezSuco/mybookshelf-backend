package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.dto.AuthorCreateDTO;    // DTO para crear autores
import com.mybookshelf.mybookshelf_backend.dto.AuthorDTO;          // DTO de respuesta
import com.mybookshelf.mybookshelf_backend.mapper.AuthorMapper;    // Mapper para conversiones
import com.mybookshelf.mybookshelf_backend.model.Author;          // Entidad Author
import com.mybookshelf.mybookshelf_backend.model.Book;            // Entidad Book para relaciones
import com.mybookshelf.mybookshelf_backend.repository.AuthorRepository; // Repository de autores
import com.mybookshelf.mybookshelf_backend.repository.BookRepository;   // Para verificar relaciones
import org.springframework.data.domain.Page;                      // Para paginación
import org.springframework.data.domain.Pageable;                  // Configuración de paginación
import org.springframework.stereotype.Service;                    // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CLASE AuthorService - "Gestor Inteligente de Autores" ACTUALIZADA
 *
 * ACTUALIZACIÓN: Agregados métodos DTO siguiendo patrón de BookService
 *
 * MANTIENE: Toda la lógica de negocio existente
 * AGREGA: Métodos para API REST con DTOs
 *
 * ARQUITECTURA:
 * - Métodos Entity (existentes) - Para lógica interna
 * - Métodos DTO (nuevos) - Para API REST
 * - AuthorMapper - Para conversiones Entity ↔ DTO
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Operaciones transaccionales por defecto
public class AuthorService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS ACTUALIZADA
    // ========================================

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuthorMapper authorMapper; // ← AGREGADO

    /**
     * CONSTRUCTOR: Inyección de dependencias ACTUALIZADA
     */
    public AuthorService(AuthorRepository authorRepository,
                         BookRepository bookRepository,
                         AuthorMapper authorMapper) { // ← AGREGADO
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.authorMapper = authorMapper; // ← AGREGADO
    }

    // ========================================
    // MÉTODOS DTO NUEVOS (PARA API REST)
    // ========================================

    /**
     * OBTENER TODOS LOS AUTORES CON PAGINACIÓN (DTO)
     *
     * SIGUIENDO PATRÓN BookService.getAllBooks()
     *
     * @param pageable Configuración de paginación
     * @return Page<AuthorDTO> para respuesta API
     */
    @Transactional(readOnly = true)
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(authorMapper::toDTO);
    }

    /**
     * OBTENER AUTOR POR ID (DTO)
     *
     * SIGUIENDO PATRÓN BookService.getBookById()
     *
     * @param id ID del autor
     * @return AuthorDTO encontrado
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public AuthorDTO getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
        return authorMapper.toDTO(author);
    }

    /**
     * CREAR NUEVO AUTOR (DTO)
     *
     * SIGUIENDO PATRÓN BookService.createBook()
     * Incluye todas las validaciones existentes
     *
     * @param createDTO DTO con datos del autor a crear
     * @return AuthorDTO del autor creado
     */
    public AuthorDTO createAuthor(AuthorCreateDTO createDTO) {
        // VALIDACIÓN: Usar mapper para verificar DTO
        if (!authorMapper.isValidForConversion(createDTO)) {
            throw new RuntimeException("Invalid author data provided");
        }

        // CONVERSIÓN: DTO → Entity
        Author author = authorMapper.toEntity(createDTO);

        // LÓGICA DE NEGOCIO: Aplicar mismas validaciones que método existente
        // VALIDACIÓN 1: Campos obligatorios
        if (author.getFirstName() == null || author.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("Author first name is required");
        }
        if (author.getLastName() == null || author.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Author last name is required");
        }

        // NORMALIZACIÓN: Usando método existente
        String normalizedFirstName = normalizeNameString(author.getFirstName());
        String normalizedLastName = normalizeNameString(author.getLastName());

        author.setFirstName(normalizedFirstName);
        author.setLastName(normalizedLastName);

        // VALIDACIÓN 2: Verificar duplicado exacto
        Optional<Author> existingAuthor = authorRepository.findByFullName(
                normalizedFirstName, normalizedLastName);
        if (existingAuthor.isPresent()) {
            throw new RuntimeException("Author '" + author.getFullName() + "' already exists");
        }

        // VALIDACIÓN 3: Verificar duplicados potenciales
        List<Author> potentialDuplicates = authorRepository.findPotentialDuplicates(
                normalizedFirstName, normalizedLastName);
        if (!potentialDuplicates.isEmpty()) {
            // Log warning pero no bloquear creación
            System.out.println("WARNING: Potential duplicate authors found for: " + author.getFullName());
            potentialDuplicates.forEach(duplicate ->
                    System.out.println("  - " + duplicate.getFullName()));
        }

        // VALIDACIÓN 4: Fecha de nacimiento lógica
        if (author.getBirthDate() != null) {
            LocalDate now = LocalDate.now();
            if (author.getBirthDate().isAfter(now)) {
                throw new RuntimeException("Birth date cannot be in the future");
            }
            if (author.getBirthDate().isBefore(LocalDate.of(1800, 1, 1))) {
                throw new RuntimeException("Birth date seems too old (before 1800)");
            }
        }

        // NORMALIZACIÓN: Limpiar nacionalidad si existe
        if (author.getNationality() != null) {
            author.setNationality(normalizeNameString(author.getNationality()));
        }

        // GUARDAR: Entity en BD
        Author savedAuthor = authorRepository.save(author);

        // CONVERSIÓN: Entity → DTO para respuesta
        return authorMapper.toDTO(savedAuthor);
    }

    /**
     * ACTUALIZAR AUTOR (DTO)
     *
     * SIGUIENDO PATRÓN BookService (aunque BookService no tiene update completo)
     *
     * @param id ID del autor a actualizar
     * @param updateDTO Datos actualizados
     * @return AuthorDTO actualizado
     */
    public AuthorDTO updateAuthor(Long id, AuthorCreateDTO updateDTO) {
        // BUSCAR: Autor existente
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        // ACTUALIZAR: Usando mapper + lógica existente
        Author updatedAuthor = authorMapper.updateEntityFromDTO(existingAuthor, updateDTO);

        // APLICAR: Validaciones adicionales si cambió el nombre
        if (updateDTO.getFirstName() != null || updateDTO.getLastName() != null) {
            // Re-verificar duplicados
            Optional<Author> duplicateAuthor = authorRepository.findByFullName(
                    updatedAuthor.getFirstName(), updatedAuthor.getLastName());
            if (duplicateAuthor.isPresent() && !duplicateAuthor.get().getId().equals(id)) {
                throw new RuntimeException("Author '" + updatedAuthor.getFullName() + "' already exists");
            }
        }

        // GUARDAR: Cambios en BD
        Author savedAuthor = authorRepository.save(updatedAuthor);

        // CONVERSIÓN: Entity → DTO para respuesta
        return authorMapper.toDTO(savedAuthor);
    }

    /**
     * ELIMINAR AUTOR (DTO)
     *
     * SIGUIENDO PATRÓN BookService.deleteBook()
     *
     * @param id ID del autor a eliminar
     */
    public void deleteAuthor(Long id) {
        // VERIFICAR: Que existe (lanza excepción si no)
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        // VALIDACIÓN: Verificar que no tiene libros (usando lógica existente)
        Long bookCount = authorRepository.countBooksByAuthor(id);
        if (bookCount > 0) {
            throw new RuntimeException("Cannot delete author with " + bookCount +
                    " associated books. Remove books first or reassign them to other authors.");
        }

        // ELIMINAR: Del repositorio
        authorRepository.delete(author);
    }

    /**
     * BUSCAR AUTORES (DTO)
     *
     * SIGUIENDO PATRÓN BookService.searchBooks()
     *
     * @param query Término de búsqueda
     * @return Lista de AuthorDTO que coinciden
     */
    @Transactional(readOnly = true)
    public List<AuthorDTO> searchAuthors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // USAR: Método existente para búsqueda
        List<Author> authors = searchAuthorEntities(query.trim());

        // CONVERSIÓN: List<Author> → List<AuthorDTO>
        return authors.stream()
                .map(authorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * OBTENER AUTORES PARA AUTOCOMPLETADO (DTO)
     *
     * NUEVO: Método especializado para UI
     *
     * @param searchTerm Término parcial
     * @param pageable Configuración (máximo resultados)
     * @return Page<AuthorDTO> para autocompletado
     */
    @Transactional(readOnly = true)
    public Page<AuthorDTO> getAuthorsForAutocomplete(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Devolver autores más populares (con más libros)
            return authorRepository.findMostProlificAuthors(pageable)
                    .map(authorMapper::toAutocompleteDTO);
        }
        return authorRepository.findForAutocomplete(searchTerm.trim(), pageable)
                .map(authorMapper::toAutocompleteDTO);
    }

    // ========================================
    // MÉTODOS ENTITY EXISTENTES (MANTENER TODOS)
    // ========================================

    /**
     * BUSCAR O CREAR AUTOR (ENTITY - MANTENER)
     *
     * Función clave para evitar duplicados al crear libros
     * USADO INTERNAMENTE por BookService
     */
    public Author findOrCreateAuthor(String firstName, String lastName) {
        // Normalizar nombres
        String normalizedFirstName = normalizeNameString(firstName);
        String normalizedLastName = normalizeNameString(lastName);

        // Buscar autor existente
        Optional<Author> existingAuthor = authorRepository.findByFullName(
                normalizedFirstName, normalizedLastName);

        if (existingAuthor.isPresent()) {
            return existingAuthor.get();
        }

        // Crear nuevo autor si no existe
        Author newAuthor = new Author(normalizedFirstName, normalizedLastName);
        return authorRepository.save(newAuthor);
    }

    /**
     * BÚSQUEDA GENERAL DE AUTORES (ENTITY - MANTENER)
     *
     * Método interno para búsquedas
     */
    @Transactional(readOnly = true)
    public List<Author> searchAuthorEntities(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return authorRepository.searchByName(searchTerm.trim());
    }

    /**
     * CREAR AUTOR (ENTITY - MANTENER)
     *
     * Método original con validaciones completas
     */
    public Author createAuthorEntity(Author author) {
        // VALIDACIÓN 1: Campos obligatorios
        if (author.getFirstName() == null || author.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("Author first name is required");
        }
        if (author.getLastName() == null || author.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Author last name is required");
        }

        // NORMALIZACIÓN: Limpiar y capitalizar nombres
        String normalizedFirstName = normalizeNameString(author.getFirstName());
        String normalizedLastName = normalizeNameString(author.getLastName());

        author.setFirstName(normalizedFirstName);
        author.setLastName(normalizedLastName);

        // VALIDACIÓN 2: Verificar duplicado exacto
        Optional<Author> existingAuthor = authorRepository.findByFullName(
                normalizedFirstName, normalizedLastName);
        if (existingAuthor.isPresent()) {
            throw new RuntimeException("Author '" + author.getFullName() + "' already exists");
        }

        // VALIDACIÓN 3: Verificar duplicados potenciales
        List<Author> potentialDuplicates = authorRepository.findPotentialDuplicates(
                normalizedFirstName, normalizedLastName);
        if (!potentialDuplicates.isEmpty()) {
            // Log warning pero no bloquear creación
            System.out.println("WARNING: Potential duplicate authors found for: " + author.getFullName());
            potentialDuplicates.forEach(duplicate ->
                    System.out.println("  - " + duplicate.getFullName()));
        }

        // VALIDACIÓN 4: Fecha de nacimiento lógica
        if (author.getBirthDate() != null) {
            LocalDate now = LocalDate.now();
            if (author.getBirthDate().isAfter(now)) {
                throw new RuntimeException("Birth date cannot be in the future");
            }
            if (author.getBirthDate().isBefore(LocalDate.of(1800, 1, 1))) {
                throw new RuntimeException("Birth date seems too old (before 1800)");
            }
        }

        // NORMALIZACIÓN: Limpiar nacionalidad si existe
        if (author.getNationality() != null) {
            author.setNationality(normalizeNameString(author.getNationality()));
        }

        return authorRepository.save(author);
    }

    /**
     * ACTUALIZAR AUTOR (ENTITY - MANTENER)
     */
    public Author updateAuthorEntity(Long id, Author updatedAuthor) {
        Author existingAuthor = getAuthorEntityById(id);

        // Actualizar solo campos no nulos
        if (updatedAuthor.getFirstName() != null) {
            existingAuthor.setFirstName(normalizeNameString(updatedAuthor.getFirstName()));
        }
        if (updatedAuthor.getLastName() != null) {
            existingAuthor.setLastName(normalizeNameString(updatedAuthor.getLastName()));
        }
        if (updatedAuthor.getBiography() != null) {
            existingAuthor.setBiography(updatedAuthor.getBiography());
        }
        if (updatedAuthor.getBirthDate() != null) {
            existingAuthor.setBirthDate(updatedAuthor.getBirthDate());
        }
        if (updatedAuthor.getNationality() != null) {
            existingAuthor.setNationality(normalizeNameString(updatedAuthor.getNationality()));
        }

        return authorRepository.save(existingAuthor);
    }

    /**
     * OBTENER AUTOR POR ID (ENTITY - MANTENER)
     */
    @Transactional(readOnly = true)
    public Author getAuthorEntityById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }

    // ========================================
    // TODOS LOS DEMÁS MÉTODOS EXISTENTES (MANTENER)
    // ========================================

    /**
     * OBTENER TODOS LOS AUTORES (ENTITY - MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Author> getAllAuthorEntities() {
        return authorRepository.findAllOrderedByName();
    }

    /**
     * AUTORES POR APELLIDO (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Author> getAuthorsByLastName(String lastName) {
        return authorRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * AUTORES POR NACIONALIDAD (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Author> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationalityIgnoreCase(nationality);
    }

    /**
     * AUTORES MÁS PROLÍFICOS (MANTENER)
     */
    @Transactional(readOnly = true)
    public Page<Author> getMostProlificAuthors(Pageable pageable) {
        return authorRepository.findMostProlificAuthors(pageable);
    }

    /**
     * ESTADÍSTICAS DE UN AUTOR (MANTENER)
     */
    @Transactional(readOnly = true)
    public AuthorStatistics getAuthorStatistics(Long authorId) {
        Author author = getAuthorEntityById(authorId);

        AuthorStatistics stats = new AuthorStatistics();
        stats.setAuthor(author);

        // Contar libros del autor
        Long bookCount = authorRepository.countBooksByAuthor(authorId);
        stats.setTotalBooks(bookCount);

        // Obtener libros del autor para análisis detallado
        List<Book> authorBooks = bookRepository.findByAuthorLastName(author.getLastName());

        // Análisis por estado
        long finishedBooks = authorBooks.stream()
                .mapToLong(book -> book.isFinished() ? 1 : 0)
                .sum();
        long readingBooks = authorBooks.stream()
                .mapToLong(book -> book.isCurrentlyReading() ? 1 : 0)
                .sum();
        long wishlistBooks = authorBooks.stream()
                .mapToLong(book -> book.getStatus().name().equals("WISHLIST") ? 1 : 0)
                .sum();

        stats.setFinishedBooks(finishedBooks);
        stats.setCurrentlyReading(readingBooks);
        stats.setInWishlist(wishlistBooks);

        // Promedio de páginas
        Double averagePages = authorBooks.stream()
                .filter(book -> book.getTotalPages() != null)
                .mapToInt(Book::getTotalPages)
                .average()
                .orElse(0.0);
        stats.setAveragePages(averagePages);

        // Rating promedio
        Double averageRating = authorBooks.stream()
                .filter(book -> book.getPersonalRating() != null)
                .mapToDouble(book -> book.getPersonalRating().doubleValue())
                .average()
                .orElse(0.0);
        stats.setAverageRating(averageRating);

        return stats;
    }

    /**
     * ESTADÍSTICAS POR NACIONALIDAD (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNationalityStatistics() {
        return authorRepository.getAuthorStatsByNationality();
    }

    /**
     * ESTADÍSTICAS POR DÉCADA (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDecadeStatistics() {
        return authorRepository.getAuthorStatsByDecade();
    }

    /**
     * AUTORES CONTEMPORÁNEOS (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Author> getContemporaryAuthors(int yearsAgo) {
        LocalDate cutoffDate = LocalDate.now().minusYears(yearsAgo);
        return authorRepository.findContemporaryAuthors(cutoffDate);
    }

    /**
     * ELIMINAR AUTOR (ENTITY - MANTENER)
     */
    public void deleteAuthorEntity(Long authorId) {
        Author author = getAuthorEntityById(authorId);

        // VALIDACIÓN: Verificar que no tiene libros
        Long bookCount = authorRepository.countBooksByAuthor(authorId);
        if (bookCount > 0) {
            throw new RuntimeException("Cannot delete author with " + bookCount +
                    " associated books. Remove books first or reassign them to other authors.");
        }

        authorRepository.delete(author);
    }

    /**
     * FUSIONAR AUTORES DUPLICADOS (MANTENER)
     */
    public Author mergeAuthors(Long mainAuthorId, Long duplicateAuthorId) {
        if (mainAuthorId.equals(duplicateAuthorId)) {
            throw new RuntimeException("Cannot merge author with itself");
        }

        Author mainAuthor = getAuthorEntityById(mainAuthorId);
        Author duplicateAuthor = getAuthorEntityById(duplicateAuthorId);

        // Obtener libros del autor duplicado
        List<Book> duplicateBooks = bookRepository.findByAuthorLastName(duplicateAuthor.getLastName());

        // Transferir libros al autor principal
        for (Book book : duplicateBooks) {
            book.removeAuthor(duplicateAuthor);
            book.addAuthor(mainAuthor);
            bookRepository.save(book);
        }

        // Combinar información biográfica si es útil
        if (mainAuthor.getBiography() == null && duplicateAuthor.getBiography() != null) {
            mainAuthor.setBiography(duplicateAuthor.getBiography());
        }
        if (mainAuthor.getBirthDate() == null && duplicateAuthor.getBirthDate() != null) {
            mainAuthor.setBirthDate(duplicateAuthor.getBirthDate());
        }
        if (mainAuthor.getNationality() == null && duplicateAuthor.getNationality() != null) {
            mainAuthor.setNationality(duplicateAuthor.getNationality());
        }

        // Guardar autor principal actualizado
        Author updatedMainAuthor = authorRepository.save(mainAuthor);

        // Eliminar autor duplicado
        authorRepository.delete(duplicateAuthor);

        return updatedMainAuthor;
    }

    /**
     * DETECTAR AUTORES DUPLICADOS POTENCIALES (MANTENER)
     */
    @Transactional(readOnly = true)
    public List<Author> findPotentialDuplicates() {
        List<Author> allAuthors = authorRepository.findAll();
        return allAuthors.stream()
                .filter(author -> {
                    List<Author> potentials = authorRepository.findPotentialDuplicates(
                            author.getFirstName(), author.getLastName());
                    return !potentials.isEmpty();
                })
                .toList();
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PRIVADOS (MANTENER)
    // ========================================

    /**
     * NORMALIZAR STRING DE NOMBRES
     */
    private String normalizeNameString(String name) {
        if (name == null) return null;

        // Limpiar espacios extra
        String cleaned = name.trim().replaceAll("\\s+", " ");

        // Si está vacío después de limpiar
        if (cleaned.isEmpty()) return cleaned;

        // Capitalizar cada palabra
        String[] words = cleaned.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");

            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }

        return result.toString();
    }

    // ========================================
    // CLASE INTERNA PARA ESTADÍSTICAS (MANTENER)
    // ========================================

    /**
     * DTO para estadísticas detalladas de un autor
     */
    public static class AuthorStatistics {
        private Author author;
        private Long totalBooks;
        private Long finishedBooks;
        private Long currentlyReading;
        private Long inWishlist;
        private Double averagePages;
        private Double averageRating;

        // Getters y setters
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }

        public Long getTotalBooks() { return totalBooks; }
        public void setTotalBooks(Long totalBooks) { this.totalBooks = totalBooks; }

        public Long getFinishedBooks() { return finishedBooks; }
        public void setFinishedBooks(Long finishedBooks) { this.finishedBooks = finishedBooks; }

        public Long getCurrentlyReading() { return currentlyReading; }
        public void setCurrentlyReading(Long currentlyReading) { this.currentlyReading = currentlyReading; }

        public Long getInWishlist() { return inWishlist; }
        public void setInWishlist(Long inWishlist) { this.inWishlist = inWishlist; }

        public Double getAveragePages() { return averagePages; }
        public void setAveragePages(Double averagePages) { this.averagePages = averagePages; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    }

    /*
     * RESUMEN DE ACTUALIZACIÓN:
     *
     * ✅ AGREGADO:
     *    - AuthorMapper injection
     *    - 6 métodos DTO para API REST:
     *      * getAllAuthors(Pageable) → Page<AuthorDTO>
     *      * getAuthorById(Long) → AuthorDTO
     *      * createAuthor(AuthorCreateDTO) → AuthorDTO
     *      * updateAuthor(Long, AuthorCreateDTO) → AuthorDTO
     *      * deleteAuthor(Long) → void
     *      * searchAuthors(String) → List<AuthorDTO>
     *      * getAuthorsForAutocomplete() → Page<AuthorDTO>
     *
     * ✅ MANTENIDO:
     *    - TODOS los métodos existentes (Entity-based)
     *    - TODA la lógica de negocio existente
     *    - TODAS las validaciones existentes
     *    - TODOS los métodos de utilidad
     *
     * ✅ PATRÓN:
     *    - Sigue EXACTAMENTE el patrón de BookService
     *    - Mismos nombres de métodos
     *    - Misma estructura de validaciones
     *    - Misma gestión de errores
     *
     * ✅ COMPATIBILIDAD:
     *    - BookService puede seguir usando findOrCreateAuthor()
     *    - Métodos internos siguen funcionando igual
     *    - Solo se agregaron métodos DTO para API REST
     */
}