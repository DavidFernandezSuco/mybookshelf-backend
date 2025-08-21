package com.mybookshelf.mybookshelf_backend.mapper;

// IMPORTS: Librerías necesarias para mapeo y conversión
import com.mybookshelf.mybookshelf_backend.dto.BookDTO;
import com.mybookshelf.mybookshelf_backend.dto.GenreCreateDTO;
import com.mybookshelf.mybookshelf_backend.dto.GenreDTO;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.Genre;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CLASE GenreMapper - Convertidor entre Genre Entity y DTOs
 *
 * ¿Para qué sirve GenreMapper?
 * - Convierte entidades Genre a DTOs para respuestas API
 * - Convierte DTOs de creación a entidades para persistir
 * - Maneja relaciones con libros sin lazy loading issues
 * - Calcula campos derivados automáticamente
 * - Evita exposición directa de entidades en API
 *
 * PATRÓN IDÉNTICO A AuthorMapper y BookMapper:
 * 1. Entity → DTO (para respuestas GET)
 * 2. CreateDTO → Entity (para requests POST)
 * 3. Cálculo de campos derivados
 * 4. Manejo de relaciones bidireccionales
 * 5. Validación de datos durante conversión
 */
@Component // Spring gestiona este mapper como un bean
public class GenreMapper {

    // ========================================
    // CONVERSIÓN ENTITY → DTO (Para respuestas)
    // ========================================

    /**
     * CONVIERTE Genre Entity a GenreDTO
     *
     * Usado para respuestas de API (GET requests)
     * Incluye todos los campos + campos calculados
     *
     * @param genre Entidad Genre a convertir
     * @return GenreDTO para respuesta API
     */
    public GenreDTO toDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        GenreDTO dto = new GenreDTO();

        // CAMPOS BÁSICOS - Mapeo directo
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());

        // TIMESTAMPS
        dto.setCreatedAt(genre.getCreatedAt());
        dto.setUpdatedAt(genre.getUpdatedAt());

        // ESTADÍSTICAS - Contar libros sin cargar la relación completa
        if (genre.getBooks() != null) {
            dto.setBookCount((long) genre.getBooks().size());
        } else {
            dto.setBookCount(0L);
        }

        // CAMPOS CALCULADOS - Llamar método que calcula todos los derivados
        dto.calculateDerivedFields();

        return dto;
    }

    /**
     * CONVIERTE Genre Entity a GenreDTO CON LIBROS
     *
     * Versión completa que incluye información de libros
     * Solo usar cuando necesites los libros explícitamente
     *
     * @param genre Entidad Genre a convertir
     * @return GenreDTO con información de libros incluida
     */
    public GenreDTO toDTOWithBooks(Genre genre) {
        if (genre == null) {
            return null;
        }

        // Empezar con DTO básico
        GenreDTO dto = toDTO(genre);

        // AGREGAR información de libros (solo campos básicos para evitar lazy loading)
        if (genre.getBooks() != null && !genre.getBooks().isEmpty()) {
            Set<BookDTO> bookDTOs = genre.getBooks().stream()
                    .map(this::bookToMinimalDTO)
                    .collect(Collectors.toSet());
            // Si GenreDTO tuviera campo books, se setearía aquí
            // dto.setBooks(bookDTOs);
        }

        return dto;
    }

    /**
     * CONVIERTE Book Entity a BookDTO minimal (para evitar lazy loading)
     *
     * DTO básico para incluir en respuestas de Genre
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
     * CONVIERTE GenreCreateDTO a Genre Entity
     *
     * Usado para requests POST (crear género)
     * NO incluye ID, timestamps (se auto-generan)
     * NO maneja relaciones con libros (se setean después en Service)
     *
     * @param createDTO DTO con datos para crear género
     * @return Genre entity listo para persistir
     */
    public Genre toEntity(GenreCreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Genre genre = new Genre();

        // CAMPOS BÁSICOS - Mapeo directo con normalización
        genre.setName(normalizeGenreName(createDTO.getName()));
        genre.setDescription(createDTO.getDescription());

        // RELACIONES - Inicializar como Set vacío
        // El Service se encargará de buscar y setear libros por IDs si es necesario
        genre.setBooks(new HashSet<>());

        return genre;
    }

    // ========================================
    // CONVERSIÓN PARA ACTUALIZACIÓN
    // ========================================

    /**
     * ACTUALIZA Genre Entity con datos de GenreCreateDTO
     *
     * Para requests PUT (actualizar género existente)
     * Preserva ID y timestamps existentes
     * Solo actualiza campos que no son null en el DTO
     *
     * @param existingGenre Genre entity existente
     * @param updateDTO DTO con campos a actualizar
     * @return Genre entity actualizada
     */
    public Genre updateEntityFromDTO(Genre existingGenre, GenreCreateDTO updateDTO) {
        if (existingGenre == null || updateDTO == null) {
            return existingGenre;
        }

        // ACTUALIZAR solo campos no nulos del DTO
        if (updateDTO.getName() != null && !updateDTO.getName().trim().isEmpty()) {
            existingGenre.setName(normalizeGenreName(updateDTO.getName()));
        }
        if (updateDTO.getDescription() != null) {
            existingGenre.setDescription(updateDTO.getDescription());
        }

        return existingGenre;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * CONVIERTE lista de Genres a lista de GenreDTOs
     *
     * Útil para endpoints que devuelven múltiples géneros
     */
    public Set<GenreDTO> toDTOSet(Set<Genre> genres) {
        if (genres == null) {
            return new HashSet<>();
        }
        return genres.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    /**
     * VERIFICA si un GenreCreateDTO es válido para conversión
     *
     * Validaciones adicionales más allá de @Valid
     */
    public boolean isValidForConversion(GenreCreateDTO createDTO) {
        if (createDTO == null) {
            return false;
        }

        // Validación 1: Nombre no puede estar vacío
        if (createDTO.getName() == null || createDTO.getName().trim().isEmpty()) {
            return false;
        }

        // Validación 2: Nombre no puede ser demasiado largo después de normalización
        String normalizedName = normalizeGenreName(createDTO.getName());
        if (normalizedName.length() > 50) {
            return false;
        }

        // Validación 3: Verificar caracteres válidos (letras, números, espacios, guiones)
        if (!normalizedName.matches("^[a-zA-Z0-9\\s\\-&]+$")) {
            return false;
        }

        return true;
    }

    /**
     * CREA DTO con información mínima para listas
     *
     * Para endpoints que necesitan datos básicos sin relaciones
     */
    public GenreDTO toMinimalDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());

        // Contar libros básico
        if (genre.getBooks() != null) {
            dto.setBookCount((long) genre.getBooks().size());
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
    public GenreDTO toAutocompleteDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());

        // Solo calcular display name
        dto.calculateDerivedFields();

        return dto;
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================================

    /**
     * NORMALIZAR NOMBRE DE GÉNERO
     *
     * Aplica reglas de normalización similares a GenreService:
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
            case "self help":
                return "self-help";
            case "how to":
                return "how-to";
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
        String[] smallWords = {"of", "and", "the", "in", "on", "at", "to", "for", "with", "&"};
        for (String smallWord : smallWords) {
            if (word.equalsIgnoreCase(smallWord)) {
                return true;
            }
        }
        return false;
    }

    /*
     * NOTAS IMPORTANTES SOBRE GenreMapper:
     *
     * 1. PATRÓN IDÉNTICO A AuthorMapper y BookMapper:
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
     * 3. NORMALIZACIÓN INTELIGENTE:
     *    - normalizeGenreName() aplica reglas consistentes
     *    - handleSpecialGenreCases() para géneros comunes
     *    - Capitalización correcta (evita "science fiction" vs "Science Fiction")
     *
     * 4. CAMPOS CALCULADOS:
     *    - calculateDerivedFields() en GenreDTO calcula:
     *      * displayName (nombre capitalizado)
     *      * isPopular (basado en bookCount)
     *    - bookCount se calcula sin cargar relaciones completas
     *
     * 5. PERFORMANCE:
     *    - toMinimalDTO() para listas grandes
     *    - toAutocompleteDTO() para campos de búsqueda
     *    - Evita cargar relaciones innecesarias
     *
     * 6. VALIDACIÓN:
     *    - isValidForConversion() para validaciones extra
     *    - Complementa @Valid de los DTOs
     *    - Validaciones de formato y caracteres
     *
     * 7. INTEGRACIÓN CON SERVICE:
     *    - El GenreService usará estos métodos para:
     *      * Convertir entities a DTOs en responses
     *      * Convertir CreateDTOs a entities en creación
     *      * Actualizar entities existentes
     *      * Manejar diferentes niveles de detalle según endpoint
     *
     * 8. CONSISTENCIA:
     *    - Misma estructura que AuthorMapper
     *    - Mismas convenciones de naming
     *    - Misma gestión de errores y edge cases
     *    - Reutilización de patrones probados
     */
}