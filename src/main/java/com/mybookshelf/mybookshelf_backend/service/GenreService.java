package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias para la lógica de negocio
import com.mybookshelf.mybookshelf_backend.model.Genre;      // Entidad Genre
import com.mybookshelf.mybookshelf_backend.repository.GenreRepository; // Repository de géneros
import org.springframework.data.domain.Page;                 // Para paginación
import org.springframework.data.domain.Pageable;             // Configuración de paginación
import org.springframework.stereotype.Service;               // Anotación de Spring
import org.springframework.transaction.annotation.Transactional; // Para transacciones

import java.util.List;
import java.util.Optional;

/**
 * CLASE GenreService - "Gestor Básico de Géneros Literarios"
 *
 * ¿Para qué sirve GenreService?
 * - Gestionar géneros con normalización básica
 * - Evitar duplicados simples
 * - Operaciones CRUD fundamentales
 * - Base para expandir funcionalidad
 *
 * CASOS DE USO PRINCIPALES:
 * - "Buscar o crear género" - Evita duplicados al añadir libros
 * - "Obtener todos los géneros" - Para formularios y listas
 * - "Gestión básica" - CRUD esencial
 *
 * VERSIÓN ULTRA SIMPLIFICADA:
 * - Solo usa métodos básicos de JpaRepository
 * - findAll(), findById(), save(), delete(), etc.
 * - Evita cualquier query personalizada
 * - Funcional pero expandible
 */
@Service // Marca esta clase como componente de lógica de negocio
@Transactional // Todas las operaciones serán transaccionales por defecto
public class GenreService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    /**
     * REPOSITORY: Acceso a datos de géneros
     *
     * Solo usamos métodos básicos de JpaRepository
     */
    private final GenreRepository genreRepository;

    /**
     * CONSTRUCTOR: Inyección de dependencias
     */
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    // ========================================
    // OPERACIONES CRUD BÁSICAS
    // ========================================

    /**
     * OBTENER TODOS LOS GÉNEROS
     *
     * @return Lista de todos los géneros
     */
    @Transactional(readOnly = true)
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    /**
     * OBTENER GÉNEROS CON PAGINACIÓN
     *
     * @param pageable Configuración de paginación
     * @return Página de géneros
     */
    @Transactional(readOnly = true)
    public Page<Genre> getAllGenres(Pageable pageable) {
        return genreRepository.findAll(pageable);
    }

    /**
     * OBTENER GÉNERO POR ID
     *
     * @param id ID del género
     * @return Genre encontrado
     * @throws RuntimeException si no existe
     */
    @Transactional(readOnly = true)
    public Genre getGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
    }

    /**
     * BUSCAR O CREAR GÉNERO (OPERACIÓN CLAVE)
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
     * CREAR NUEVO GÉNERO (CON VALIDACIONES)
     *
     * @param genre Género a crear
     * @return Género creado
     */
    public Genre createGenre(Genre genre) {
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
     * ACTUALIZAR GÉNERO
     *
     * @param id ID del género a actualizar
     * @param updatedGenre Datos actualizados
     * @return Género actualizado
     */
    public Genre updateGenre(Long id, Genre updatedGenre) {
        Genre existingGenre = getGenreById(id);

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
     * ELIMINAR GÉNERO (CON VALIDACIONES BÁSICAS)
     *
     * @param genreId ID del género a eliminar
     */
    public void deleteGenre(Long genreId) {
        Genre genre = getGenreById(genreId);

        // VALIDACIÓN BÁSICA: Verificar si tiene libros asociados
        if (genre.getBooks() != null && !genre.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete genre with " + genre.getBooks().size() +
                    " associated books. Reassign books first.");
        }

        genreRepository.delete(genre);
    }

    // ========================================
    // OPERACIONES DE BÚSQUEDA BÁSICA
    // ========================================

    /**
     * BÚSQUEDA SIMPLE DE GÉNEROS
     *
     * Busca en nombre usando filtrado manual
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de géneros que coinciden
     */
    @Transactional(readOnly = true)
    public List<Genre> searchGenres(String searchTerm) {
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
     * OBTENER GÉNEROS ORDENADOS ALFABÉTICAMENTE
     *
     * @return Lista ordenada por nombre
     */
    @Transactional(readOnly = true)
    public List<Genre> getGenresOrderedByName() {
        return genreRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    // ========================================
    // ESTADÍSTICAS BÁSICAS
    // ========================================

    /**
     * CONTAR GÉNEROS TOTAL
     *
     * @return Número total de géneros
     */
    @Transactional(readOnly = true)
    public Long getTotalGenresCount() {
        return genreRepository.count();
    }

    /**
     * OBTENER GÉNEROS CON LIBROS
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
     * OBTENER GÉNEROS SIN LIBROS (HUÉRFANOS)
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
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================================

    /**
     * BUSCAR GÉNERO POR NOMBRE (CASE-INSENSITIVE)
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
     * NORMALIZAR NOMBRE DE GÉNERO
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
     * MANEJAR CASOS ESPECIALES DE GÉNEROS
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
     * VERIFICAR SI ES PREPOSICIÓN PEQUEÑA
     *
     * @param word Palabra a verificar
     * @return true si es preposición que no se capitaliza
     */
    private boolean isSmallPreposition(String word) {
        String[] smallWords = {"of", "and", "the", "in", "on", "at", "to", "for", "with"};
        return List.of(smallWords).contains(word.toLowerCase());
    }

    // ========================================
    // OPERACIONES AVANZADAS BÁSICAS
    // ========================================

    /**
     * LIMPIAR GÉNEROS HUÉRFANOS
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
     * VERIFICAR SI EXISTE GÉNERO POR NOMBRE
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

    /*
     * NOTAS IMPORTANTES SOBRE GenreService SIMPLIFICADO:
     *
     * 1. SOLO MÉTODOS BÁSICOS:
     *    - Usa únicamente findAll(), findById(), save(), delete()
     *    - Evita cualquier query personalizada
     *    - Filtrado manual con streams cuando es necesario
     *
     * 2. FUNCIONALIDAD ESENCIAL:
     *    - findOrCreateGenre() para evitar duplicados
     *    - Normalización inteligente de nombres
     *    - Validaciones básicas pero efectivas
     *    - Operaciones CRUD completas
     *
     * 3. BÚSQUEDAS MANUALES:
     *    - findGenreByNameIgnoreCase() implementado con streams
     *    - Búsquedas de texto con filtrado en memoria
     *    - Ordenamiento manual por nombre
     *
     * 4. PERFORMANCE:
     *    - Adecuado para volúmenes pequeños/medianos de géneros
     *    - Fácil de optimizar después con queries personalizadas
     *    - Transacciones read-only donde corresponde
     *
     * 5. ESCALABILIDAD:
     *    - Base sólida para añadir queries optimizadas
     *    - Estructura preparada para expansión
     *    - Métodos privados reutilizables
     *
     * Esta versión compila sin errores y proporciona toda la funcionalidad
     * esencial para gestionar géneros en la aplicación.
     */
}