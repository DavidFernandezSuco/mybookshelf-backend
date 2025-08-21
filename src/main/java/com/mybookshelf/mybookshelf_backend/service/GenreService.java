package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.dto.GenreCreateDTO;    // DTO para crear géneros
import com.mybookshelf.mybookshelf_backend.dto.GenreDTO;          // DTO de respuesta
import com.mybookshelf.mybookshelf_backend.mapper.GenreMapper;    // Mapper para conversiones
import com.mybookshelf.mybookshelf_backend.model.Genre;          // Entidad Genre
import com.mybookshelf.mybookshelf_backend.repository.GenreRepository; // Repository de géneros
import org.springframework.data.domain.Page;                     // Para paginación
import org.springframework.data.domain.Pageable;                 // Configuración de paginación
import org.springframework.stereotype.Service;                   // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CLASE GenreService - "Gestor Básico de Géneros Literarios" ACTUALIZADA
 *
 * ACTUALIZACIÓN: Agregados métodos DTO siguiendo patrón de AuthorService
 *
 * MANTIENE: Toda la lógica de negocio existente
 * AGREGA: Métodos para API REST con DTOs
 *
 * ARQUITECTURA:
 * - Métodos Entity (existentes) - Para lógica interna
 * - Métodos DTO (nuevos) - Para API REST
 * - GenreMapper - Para conversiones Entity ↔ DTO
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Todas las operaciones serán transaccionales por defecto
public class GenreService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS ACTUALIZADA
    // ========================================

    /**
     * REPOSITORY: Acceso a datos de géneros
     */
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper; // ← AGREGADO

    /**
     * CONSTRUCTOR: Inyección de dependencias ACTUALIZADA
     */
    public GenreService(GenreRepository genreRepository,
                        GenreMapper genreMapper) { // ← AGREGADO
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper; // ← AGREGADO
    }

    // ========================================
    // MÉTODOS DTO NUEVOS (PARA API REST)
    // ========================================

    /**
     * OBTENER TODOS LOS GÉNEROS CON PAGINACIÓN (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.getAllAuthors()
     *
     * @param pageable Configuración de paginación
     * @return Page<GenreDTO> para respuesta API
     */
    @Transactional(readOnly = true)
    public Page<GenreDTO> getAllGenres(Pageable pageable) {
        return genreRepository.findAll(pageable)
                .map(genreMapper::toDTO);
    }

    /**
     * OBTENER GÉNERO POR ID (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.getAuthorById()
     *
     * @param id ID del género
     * @return GenreDTO encontrado
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public GenreDTO getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
        return genreMapper.toDTO(genre);
    }

    /**
     * CREAR NUEVO GÉNERO (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.createAuthor()
     * Incluye todas las validaciones existentes
     *
     * @param createDTO DTO con datos del género a crear
     * @return GenreDTO del género creado
     */
    public GenreDTO createGenre(GenreCreateDTO createDTO) {
        // VALIDACIÓN: Usar mapper para verificar DTO
        if (!genreMapper.isValidForConversion(createDTO)) {
            throw new RuntimeException("Invalid genre data provided");
        }

        // CONVERSIÓN: DTO → Entity
        Genre genre = genreMapper.toEntity(createDTO);

        // LÓGICA DE NEGOCIO: Aplicar mismas validaciones que método existente
        // VALIDACIÓN 1: Nombre obligatorio
        if (genre.getName() == null || genre.getName().trim().isEmpty()) {
            throw new RuntimeException("Genre name is required");
        }

        // NORMALIZACIÓN: Usando método existente
        String normalizedName = normalizeGenreName(genre.getName());

        // VALIDACIÓN 2: Verificar duplicado
        Optional<Genre> existingGenre = findGenreByNameIgnoreCase(normalizedName);
        if (existingGenre.isPresent()) {
            throw new RuntimeException("Genre '" + normalizedName + "' already exists");
        }

        // VALIDACIÓN 3: Longitud del nombre
        if (normalizedName.length() > 50) {
            throw new RuntimeException("Genre name must not exceed 50 characters");
        }

        // APLICAR normalización
        genre.setName(normalizedName);

        // DESCRIPCIÓN por defecto si no se proporciona
        if (genre.getDescription() == null || genre.getDescription().trim().isEmpty()) {
            genre.setDescription("Genre created via API");
        }

        // GUARDAR: Entity en BD
        Genre savedGenre = genreRepository.save(genre);

        // CONVERSIÓN: Entity → DTO para respuesta
        return genreMapper.toDTO(savedGenre);
    }

    /**
     * ACTUALIZAR GÉNERO (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.updateAuthor()
     *
     * @param id ID del género a actualizar
     * @param updateDTO Datos actualizados
     * @return GenreDTO actualizado
     */
    public GenreDTO updateGenre(Long id, GenreCreateDTO updateDTO) {
        // BUSCAR: Género existente
        Genre existingGenre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));

        // ACTUALIZAR: Usando mapper + lógica existente
        Genre updatedGenre = genreMapper.updateEntityFromDTO(existingGenre, updateDTO);

        // APLICAR: Validaciones adicionales si cambió el nombre
        if (updateDTO.getName() != null && !updateDTO.getName().trim().isEmpty()) {
            String normalizedName = normalizeGenreName(updateDTO.getName());

            // Verificar que el nuevo nombre no existe en otro género
            Optional<Genre> duplicateGenre = findGenreByNameIgnoreCase(normalizedName);
            if (duplicateGenre.isPresent() && !duplicateGenre.get().getId().equals(id)) {
                throw new RuntimeException("Genre '" + normalizedName + "' already exists");
            }

            updatedGenre.setName(normalizedName);
        }

        // GUARDAR: Cambios en BD
        Genre savedGenre = genreRepository.save(updatedGenre);

        // CONVERSIÓN: Entity → DTO para respuesta
        return genreMapper.toDTO(savedGenre);
    }

    /**
     * ELIMINAR GÉNERO (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.deleteAuthor()
     *
     * @param id ID del género a eliminar
     */
    public void deleteGenre(Long id) {
        // VERIFICAR: Que existe (lanza excepción si no)
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));

        // VALIDACIÓN BÁSICA: Verificar si tiene libros asociados
        if (genre.getBooks() != null && !genre.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete genre with " + genre.getBooks().size() +
                    " associated books. Reassign books first.");
        }

        // ELIMINAR: Del repositorio
        genreRepository.delete(genre);
    }

    /**
     * BUSCAR GÉNEROS (DTO)
     *
     * SIGUIENDO PATRÓN AuthorService.searchAuthors()
     *
     * @param query Término de búsqueda
     * @return Lista de GenreDTO que coinciden
     */
    @Transactional(readOnly = true)
    public List<GenreDTO> searchGenres(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // USAR: Método existente para búsqueda
        List<Genre> genres = searchGenreEntities(query.trim());

        // CONVERSIÓN: List<Genre> → List<GenreDTO>
        return genres.stream()
                .map(genreMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * OBTENER GÉNEROS PARA AUTOCOMPLETADO (DTO)
     *
     * NUEVO: Método especializado para UI
     *
     * @param searchTerm Término parcial
     * @param pageable Configuración (máximo resultados)
     * @return Page<GenreDTO> para autocompletado
     */
    @Transactional(readOnly = true)
    public Page<GenreDTO> getGenresForAutocomplete(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Devolver géneros más populares (con más libros)
            return genreRepository.findGenresByPopularity(pageable)
                    .map(genreMapper::toAutocompleteDTO);
        }
        return genreRepository.findForAutocomplete(searchTerm.trim(), pageable)
                .map(genreMapper::toAutocompleteDTO);
    }

    /**
     * OBTENER GÉNEROS ORDENADOS ALFABÉTICAMENTE (DTO)
     *
     * NUEVO: Para dropdowns y listas ordenadas
     *
     * @return Lista de GenreDTO ordenada por nombre
     */
    @Transactional(readOnly = true)
    public List<GenreDTO> getGenresOrderedByName() {
        List<Genre> genres = genreRepository.findAllOrderedByName();
        return genres.stream()
                .map(genreMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // MÉTODOS ENTITY EXISTENTES (MANTENER TODOS)
    // ========================================

    /**
     * OBTENER TODOS LOS GÉNEROS (ENTITY - MANTENER)
     *
     * @return Lista de todos los géneros
     */
    @Transactional(readOnly = true)
    public List<Genre> getAllGenreEntities() {
        return genreRepository.findAll();
    }

    /**
     * OBTENER GÉNEROS CON PAGINACIÓN (ENTITY - MANTENER)
     *
     * @param pageable Configuración de paginación
     * @return Página de géneros
     */
    @Transactional(readOnly = true)
    public Page<Genre> getAllGenreEntities(Pageable pageable) {
        return genreRepository.findAll(pageable);
    }

    /**
     * OBTENER GÉNERO POR ID (ENTITY - MANTENER)
     *
     * @param id ID del género
     * @return Genre encontrado
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public Genre getGenreEntityById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
    }

    /**
     * BUSCAR O CREAR GÉNERO (ENTITY - MANTENER)
     *
     * Función más importante para evitar duplicados:
     * 1. Normaliza el nombre del género
     * 2. Busca si ya existe (case-insensitive)
     * 3. Si existe, lo devuelve
     * 4. Si no existe, lo crea automáticamente
     *
     * @param name Nombre del género a buscar/crear
     * @return Género existente o recién creado
     */
    public Genre findOrCreateGenre(String name) {
        // VALIDACIÓN: Nombre no puede estar vacío
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Genre name cannot be empty");
        }

        // NORMALIZACIÓN: Limpiar y capitalizar correctamente
        String normalizedName = normalizeGenreName(name);

        // BÚSQUEDA MANUAL: Buscar en todos los géneros (case-insensitive)
        Optional<Genre> existingGenre = findGenreByNameIgnoreCase(normalizedName);
        if (existingGenre.isPresent()) {
            return existingGenre.get();
        }

        // CREACIÓN: Si no existe, crear nuevo género
        Genre newGenre = new Genre();
        newGenre.setName(normalizedName);
        newGenre.setDescription("Auto-created genre");

        return genreRepository.save(newGenre);
    }

    /**
     * CREAR NUEVO GÉNERO (ENTITY - MANTENER)
     *
     * @param genre Género a crear
     * @return Género creado
     */
    public Genre createGenreEntity(Genre genre) {
        // VALIDACIÓN 1: Nombre obligatorio
        if (genre.getName() == null || genre.getName().trim().isEmpty()) {
            throw new RuntimeException("Genre name is required");
        }

        // NORMALIZACIÓN: Limpiar y capitalizar nombre
        String normalizedName = normalizeGenreName(genre.getName());

        // VALIDACIÓN 2: Verificar duplicado
        Optional<Genre> existingGenre = findGenreByNameIgnoreCase(normalizedName);
        if (existingGenre.isPresent()) {
            throw new RuntimeException("Genre '" + normalizedName + "' already exists");
        }

        // VALIDACIÓN 3: Longitud del nombre
        if (normalizedName.length() > 50) {
            throw new RuntimeException("Genre name must not exceed 50 characters");
        }

        // APLICAR normalización
        genre.setName(normalizedName);

        // DESCRIPCIÓN por defecto si no se proporciona
        if (genre.getDescription() == null || genre.getDescription().trim().isEmpty()) {
            genre.setDescription("Genre created manually");
        }

        return genreRepository.save(genre);
    }

    /**
     * ACTUALIZAR GÉNERO (ENTITY - MANTENER)
     *
     * @param id ID del género a actualizar
     * @param updatedGenre Datos actualizados
     * @return Género actualizado
     */
    public Genre updateGenreEntity(Long id, Genre updatedGenre) {
        Genre existingGenre = getGenreEntityById(id);

        // Actualizar solo campos no nulos
        if (updatedGenre.getName() != null && !updatedGenre.getName().trim().isEmpty()) {
            String normalizedName = normalizeGenreName(updatedGenre.getName());

            // Verificar que el nuevo nombre no existe en otro género
            Optional<Genre> duplicateGenre = findGenreByNameIgnoreCase(normalizedName);
            if (duplicateGenre.isPresent() && !duplicateGenre.get().getId().equals(id)) {
                throw new RuntimeException("Genre '" + normalizedName + "' already exists");
            }

            existingGenre.setName(normalizedName);
        }

        if (updatedGenre.getDescription() != null) {
            existingGenre.setDescription(updatedGenre.getDescription());
        }

        return genreRepository.save(existingGenre);
    }

    /**
     * ELIMINAR GÉNERO (ENTITY - MANTENER)
     *
     * Solo permite eliminar si no tiene libros asociados
     *
     * @param genreId ID del género a eliminar
     */
    public void deleteGenreEntity(Long genreId) {
        Genre genre = getGenreEntityById(genreId);

        // VALIDACIÓN BÁSICA: Verificar si tiene libros asociados
        if (genre.getBooks() != null && !genre.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete genre with " + genre.getBooks().size() +
                    " associated books. Reassign books first.");
        }

        genreRepository.delete(genre);
    }

    // ========================================
    // OPERACIONES DE BÚSQUEDA BÁSICA (MANTENER)
    // ========================================

    /**
     * BÚSQUEDA SIMPLE DE GÉNEROS (ENTITY - MANTENER)
     *
     * Busca en nombre usando filtrado manual
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de géneros que coinciden
     */
    @Transactional(readOnly = true)
    public List<Genre> searchGenreEntities(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        // Búsqueda manual en memoria
        return genreRepository.findAll().stream()
                .filter(genre -> genre.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        (genre.getDescription() != null &&
                                genre.getDescription().toLowerCase().contains(searchTerm.toLowerCase())))
                .toList();
    }

    /**
     * OBTENER GÉNEROS ORDENADOS ALFABÉTICAMENTE (ENTITY - MANTENER)
     *
     * @return Lista ordenada por nombre
     */
    @Transactional(readOnly = true)
    public List<Genre> getGenresOrderedByNameEntity() {
        return genreRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    // ========================================
    // ESTADÍSTICAS BÁSICAS (MANTENER)
    // ========================================

    /**
     * CONTAR GÉNEROS TOTAL (MANTENER)
     *
     * @return Número total de géneros
     */
    @Transactional(readOnly = true)
    public Long getTotalGenresCount() {
        return genreRepository.count();
    }

    /**
     * OBTENER GÉNEROS CON LIBROS (MANTENER)
     *
     * @return Lista de géneros que tienen al menos un libro
     */
    @Transactional(readOnly = true)
    public List<Genre> getGenresWithBooks() {
        return genreRepository.findAll().stream()
                .filter(genre -> genre.getBooks() != null && !genre.getBooks().isEmpty())
                .toList();
    }

    /**
     * OBTENER GÉNEROS SIN LIBROS (HUÉRFANOS) (MANTENER)
     *
     * @return Lista de géneros sin libros asociados
     */
    @Transactional(readOnly = true)
    public List<Genre> getOrphanGenres() {
        return genreRepository.findAll().stream()
                .filter(genre -> genre.getBooks() == null || genre.getBooks().isEmpty())
                .toList();
    }

    // ========================================
    // OPERACIONES AVANZADAS BÁSICAS (MANTENER)
    // ========================================

    /**
     * LIMPIAR GÉNEROS HUÉRFANOS (MANTENER)
     *
     * Elimina géneros que no tienen libros asociados
     *
     * @return Número de géneros eliminados
     */
    public int cleanOrphanGenres() {
        List<Genre> orphanGenres = getOrphanGenres();

        // Solo eliminar géneros auto-creados sin libros
        List<Genre> toDelete = orphanGenres.stream()
                .filter(genre -> genre.getDescription() != null &&
                        genre.getDescription().contains("Auto-created"))
                .toList();

        genreRepository.deleteAll(toDelete);
        return toDelete.size();
    }

    /**
     * VERIFICAR SI EXISTE GÉNERO POR NOMBRE (MANTENER)
     *
     * @param name Nombre del género
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return findGenreByNameIgnoreCase(normalizeGenreName(name)).isPresent();
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PRIVADOS (MANTENER)
    // ========================================

    /**
     * BUSCAR GÉNERO POR NOMBRE (CASE-INSENSITIVE) (MANTENER)
     *
     * Implementación manual ya que no tenemos el método en repository
     *
     * @param name Nombre a buscar
     * @return Optional con el género si existe
     */
    private Optional<Genre> findGenreByNameIgnoreCase(String name) {
        return genreRepository.findAll().stream()
                .filter(genre -> genre.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * NORMALIZAR NOMBRE DE GÉNERO (MANTENER)
     *
     * Aplica reglas básicas de normalización:
     * - Capitaliza primera letra de cada palabra
     * - Limpia espacios extra
     * - Maneja casos especiales
     *
     * @param name Nombre a normalizar
     * @return Nombre normalizado
     */
    private String normalizeGenreName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        // Limpiar espacios extra y convertir a lowercase
        String cleaned = name.trim().replaceAll("\\s+", " ").toLowerCase();

        // Casos especiales para géneros comunes
        cleaned = handleSpecialGenreCases(cleaned);

        // Capitalizar primera letra de cada palabra
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");

            String word = words[i];
            if (!word.isEmpty()) {
                // No capitalizar preposiciones pequeñas (excepto si es la primera palabra)
                if (i > 0 && isSmallPreposition(word)) {
                    result.append(word);
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        result.append(word.substring(1));
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * MANEJAR CASOS ESPECIALES DE GÉNEROS (MANTENER)
     *
     * @param name Nombre en lowercase
     * @return Nombre con casos especiales aplicados
     */
    private String handleSpecialGenreCases(String name) {
        // Casos especiales comunes en géneros literarios
        switch (name) {
            case "sci-fi":
            case "scifi":
                return "science fiction";
            case "non-fiction":
            case "nonfiction":
                return "non-fiction";
            case "ya":
                return "young adult";
            case "rom com":
            case "romcom":
                return "romantic comedy";
            default:
                return name;
        }
    }

    /**
     * VERIFICAR SI ES PREPOSICIÓN PEQUEÑA (MANTENER)
     *
     * @param word Palabra a verificar
     * @return true si es preposición que no se capitaliza
     */
    private boolean isSmallPreposition(String word) {
        String[] smallWords = {"of", "and", "the", "in", "on", "at", "to", "for", "with"};
        return Arrays.asList(smallWords).contains(word.toLowerCase());
    }

    /*
     * RESUMEN DE ACTUALIZACIÓN:
     *
     * ✅ AGREGADO:
     *    - GenreMapper injection
     *    - 6 métodos DTO para API REST:
     *      * getAllGenres(Pageable) → Page<GenreDTO>
     *      * getGenreById(Long) → GenreDTO
     *      * createGenre(GenreCreateDTO) → GenreDTO
     *      * updateGenre(Long, GenreCreateDTO) → GenreDTO
     *      * deleteGenre(Long) → void
     *      * searchGenres(String) → List<GenreDTO>
     *      * getGenresForAutocomplete() → Page<GenreDTO>
     *      * getGenresOrderedByName() → List<GenreDTO>
     *
     * ✅ MANTENIDO:
     *    - TODOS los métodos existentes (Entity-based)
     *    - TODA la lógica de negocio existente
     *    - TODAS las validaciones existentes
     *    - TODOS los métodos de utilidad
     *    - findOrCreateGenre() para BookService
     *
     * ✅ PATRÓN:
     *    - Sigue EXACTAMENTE el patrón de AuthorService
     *    - Mismos nombres de métodos
     *    - Misma estructura de validaciones
     *    - Misma gestión de errores
     *
     * ✅ COMPATIBILIDAD:
     *    - BookService puede seguir usando findOrCreateGenre()
     *    - Métodos internos siguen funcionando igual
     *    - Solo se agregaron métodos DTO para API REST
     */
}