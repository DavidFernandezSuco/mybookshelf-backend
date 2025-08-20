package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.model.Author;      // Entidad Author
import com.mybookshelf.mybookshelf_backend.model.Book;        // Entidad Book para relaciones
import com.mybookshelf.mybookshelf_backend.repository.AuthorRepository; // Repository de autores
import com.mybookshelf.mybookshelf_backend.repository.BookRepository;   // Para verificar relaciones
import org.springframework.data.domain.Page;                  // Para paginación
import org.springframework.data.domain.Pageable;              // Configuración de paginación
import org.springframework.stereotype.Service;                // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CLASE AuthorService - "Gestor Inteligente de Autores"
 *
 * ¿Para qué sirve AuthorService?
 * - Gestionar autores con validaciones de negocio
 * - Evitar duplicados de autores
 * - Proporcionar búsquedas inteligentes
 * - Estadísticas de productividad de autores
 * - Manejo de relaciones con libros
 *
 * CASOS DE USO PRINCIPALES:
 * - "Buscar o crear autor" - Evita duplicados al añadir libros
 * - "Autores más prolíficos" - Estadísticas de productividad
 * - "Autocompletado" - Para formularios de creación de libros
 * - "Limpieza de datos" - Detectar y fusionar autores duplicados
 *
 * LÓGICA DE NEGOCIO ESPECIAL:
 * - Normalización de nombres (capitalización, espacios)
 * - Detección de duplicados potenciales
 * - Validación de datos biográficos
 * - Gestión de relaciones con libros
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Operaciones transaccionales por defecto
public class AuthorService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    /**
     * CONSTRUCTOR: Inyección de dependencias
     */
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    // ========================================
    // OPERACIONES CRUD CON LÓGICA DE NEGOCIO
    // ========================================

    /**
     * OBTENER TODOS LOS AUTORES (ORDENADOS ALFABÉTICAMENTE)
     *
     * @return Lista de autores ordenada por apellido, nombre
     */
    @Transactional(readOnly = true)
    public List<Author> getAllAuthors() {
        return authorRepository.findAllOrderedByName();
    }

    /**
     * OBTENER AUTORES CON PAGINACIÓN
     *
     * @param pageable Configuración de paginación
     * @return Página de autores
     */
    @Transactional(readOnly = true)
    public Page<Author> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    /**
     * OBTENER AUTOR POR ID
     *
     * @param id ID del autor
     * @return Author encontrado
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }

    /**
     * CREAR NUEVO AUTOR (CON VALIDACIONES)
     *
     * Lógica de negocio aplicada:
     * 1. Normalizar nombres (capitalizar, limpiar espacios)
     * 2. Verificar duplicados por nombre completo
     * 3. Validar datos obligatorios
     * 4. Verificar duplicados potenciales y advertir
     *
     * @param author Autor a crear
     * @return Autor creado
     */
    public Author createAuthor(Author author) {
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
     * ACTUALIZAR AUTOR
     *
     * @param id ID del autor a actualizar
     * @param updatedAuthor Datos actualizados
     * @return Autor actualizado
     */
    public Author updateAuthor(Long id, Author updatedAuthor) {
        Author existingAuthor = getAuthorById(id);

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

    // ========================================
    // OPERACIONES DE BÚSQUEDA INTELIGENTE
    // ========================================

    /**
     * BUSCAR O CREAR AUTOR (OPERACIÓN INTELIGENTE)
     *
     * Función clave para evitar duplicados al crear libros:
     * 1. Busca si ya existe el autor
     * 2. Si existe, lo devuelve
     * 3. Si no existe, lo crea
     *
     * @param firstName Nombre del autor
     * @param lastName Apellido del autor
     * @return Autor existente o recién creado
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
     * BÚSQUEDA GENERAL DE AUTORES
     *
     * Busca en nombre y apellido simultáneamente
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de autores que coinciden
     */
    @Transactional(readOnly = true)
    public List<Author> searchAuthors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return authorRepository.searchByName(searchTerm.trim());
    }

    /**
     * AUTORES PARA AUTOCOMPLETADO
     *
     * Devuelve pocos resultados optimizados para UIs
     *
     * @param searchTerm Término parcial
     * @param pageable Configuración (máximo resultados)
     * @return Página de autores para autocompletado
     */
    @Transactional(readOnly = true)
    public Page<Author> getAuthorsForAutocomplete(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Devolver autores más populares (con más libros)
            return authorRepository.findMostProlificAuthors(pageable);
        }
        return authorRepository.findForAutocomplete(searchTerm.trim(), pageable);
    }

    /**
     * AUTORES POR APELLIDO
     *
     * @param lastName Apellido a buscar
     * @return Lista de autores con ese apellido
     */
    @Transactional(readOnly = true)
    public List<Author> getAuthorsByLastName(String lastName) {
        return authorRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * AUTORES POR NACIONALIDAD
     *
     * @param nationality Nacionalidad
     * @return Lista de autores de esa nacionalidad
     */
    @Transactional(readOnly = true)
    public List<Author> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationalityIgnoreCase(nationality);
    }

    // ========================================
    // ESTADÍSTICAS Y ANÁLISIS
    // ========================================

    /**
     * AUTORES MÁS PROLÍFICOS
     *
     * @param pageable Configuración de paginación
     * @return Página de autores ordenados por número de libros
     */
    @Transactional(readOnly = true)
    public Page<Author> getMostProlificAuthors(Pageable pageable) {
        return authorRepository.findMostProlificAuthors(pageable);
    }

    /**
     * ESTADÍSTICAS DE UN AUTOR
     *
     * @param authorId ID del autor
     * @return Estadísticas detalladas del autor
     */
    @Transactional(readOnly = true)
    public AuthorStatistics getAuthorStatistics(Long authorId) {
        Author author = getAuthorById(authorId);

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
     * ESTADÍSTICAS POR NACIONALIDAD
     *
     * @return Lista de [nacionalidad, cantidad] ordenada por popularidad
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNationalityStatistics() {
        return authorRepository.getAuthorStatsByNationality();
    }

    /**
     * ESTADÍSTICAS POR DÉCADA DE NACIMIENTO
     *
     * @return Lista de [década, cantidad] para análisis temporal
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDecadeStatistics() {
        return authorRepository.getAuthorStatsByDecade();
    }

    /**
     * AUTORES CONTEMPORÁNEOS
     *
     * @param yearsAgo Hace cuántos años (ej: 50 para últimos 50 años)
     * @return Lista de autores contemporáneos
     */
    @Transactional(readOnly = true)
    public List<Author> getContemporaryAuthors(int yearsAgo) {
        LocalDate cutoffDate = LocalDate.now().minusYears(yearsAgo);
        return authorRepository.findContemporaryAuthors(cutoffDate);
    }

    // ========================================
    // OPERACIONES AVANZADAS
    // ========================================

    /**
     * ELIMINAR AUTOR (CON VALIDACIONES)
     *
     * Solo permite eliminar si no tiene libros asociados
     *
     * @param authorId ID del autor a eliminar
     */
    public void deleteAuthor(Long authorId) {
        Author author = getAuthorById(authorId);

        // VALIDACIÓN: Verificar que no tiene libros
        Long bookCount = authorRepository.countBooksByAuthor(authorId);
        if (bookCount > 0) {
            throw new RuntimeException("Cannot delete author with " + bookCount +
                    " associated books. Remove books first or reassign them to other authors.");
        }

        authorRepository.delete(author);
    }

    /**
     * FUSIONAR AUTORES DUPLICADOS
     *
     * Combina dos autores, moviendo todos los libros al autor principal
     *
     * @param mainAuthorId ID del autor principal (se mantiene)
     * @param duplicateAuthorId ID del autor duplicado (se elimina)
     * @return Autor principal actualizado
     */
    public Author mergeAuthors(Long mainAuthorId, Long duplicateAuthorId) {
        if (mainAuthorId.equals(duplicateAuthorId)) {
            throw new RuntimeException("Cannot merge author with itself");
        }

        Author mainAuthor = getAuthorById(mainAuthorId);
        Author duplicateAuthor = getAuthorById(duplicateAuthorId);

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
     * DETECTAR AUTORES DUPLICADOS POTENCIALES
     *
     * @return Lista de autores que podrían ser duplicados
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
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================================

    /**
     * NORMALIZAR STRING DE NOMBRES - VERSIÓN CORREGIDA
     *
     * Limpia espacios, capitaliza correctamente
     *
     * @param name Nombre a normalizar
     * @return Nombre normalizado
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

    // ALTERNATIVA MÁS SIMPLE CON APACHE COMMONS (si tienes la dependencia)
    private String normalizeNameStringAlternative(String name) {
        if (name == null) return null;

        // Usando Apache Commons Text (requiere dependencia)
        // return WordUtils.capitalizeFully(name.trim().replaceAll("\\s+", " "));

        // O usando Streams (Java 8+)
        return Arrays.stream(name.trim().toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) +
                        (word.length() > 1 ? word.substring(1) : ""))
                .collect(Collectors.joining(" "));
    }

    // ========================================
    // CLASE INTERNA PARA ESTADÍSTICAS
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
     * NOTAS IMPORTANTES SOBRE AuthorService:
     *
     * 1. PREVENCIÓN DE DUPLICADOS:
     *    - findOrCreateAuthor() evita duplicados al crear libros
     *    - Normalización de nombres para consistencia
     *    - Detección de duplicados potenciales
     *
     * 2. OPERACIONES INTELIGENTES:
     *    - Búsqueda en múltiples campos simultáneamente
     *    - Autocompletado optimizado para UIs
     *    - Fusión de autores duplicados
     *
     * 3. VALIDACIONES DE NEGOCIO:
     *    - Fechas de nacimiento lógicas
     *    - Nombres obligatorios y normalizados
     *    - Verificación antes de eliminar
     *
     * 4. ESTADÍSTICAS ÚTILES:
     *    - Productividad por autor
     *    - Distribución geográfica
     *    - Análisis temporal por décadas
     *
     * 5. MANTENIMIENTO DE DATOS:
     *    - Limpieza de duplicados
     *    - Normalización consistente
     *    - Integridad referencial con libros
     */
}