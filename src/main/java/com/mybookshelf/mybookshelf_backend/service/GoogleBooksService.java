package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.client.GoogleBooksClient;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import com.mybookshelf.mybookshelf_backend.mapper.GoogleBooksMapper;
import com.mybookshelf.mybookshelf_backend.model.Author;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.mybookshelf.mybookshelf_backend.model.Genre;
import com.mybookshelf.mybookshelf_backend.exception.BookNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * CLASE GoogleBooksService - Lógica de Negocio para Integración con Google Books
 *
 * SIGUIENDO PATRÓN DE TUS SERVICES EXISTENTES:
 * - @Service component con inyección de dependencias
 * - Métodos claros con responsabilidades específicas
 * - Validaciones de entrada y manejo de errores
 * - Integración limpia con servicios existentes
 * - Logs informativos para debugging
 *
 * RESPONSABILIDADES:
 * 1. Orquestar búsquedas externas (usa GoogleBooksClient)
 * 2. Convertir datos externos a entidades internas (usa GoogleBooksMapper)
 * 3. Integrar con BookService, AuthorService y GenreService existentes
 * 4. Gestionar duplicados y conflictos de datos
 * 5. Proporcionar métodos de alto nivel para los controllers
 *
 * FASE 3 COMPLETADA: Service Layer con lógica de negocio
 */
@Service
public class GoogleBooksService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    @Autowired
    private GoogleBooksClient googleBooksClient;

    @Autowired
    private GoogleBooksMapper googleBooksMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    // ========================================
    // MÉTODOS PRINCIPALES DE BÚSQUEDA
    // ========================================

    /**
     * BUSCAR LIBROS EN GOOGLE BOOKS
     *
     * Método principal para buscar libros externamente.
     * Valida entrada y delega en GoogleBooksClient.
     *
     * @param query Término de búsqueda
     * @return Lista de libros encontrados en Google Books
     */
    public List<GoogleBookDTO> searchBooks(String query) {
        // Validación de entrada
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        System.out.println("🔍 GoogleBooksService: Buscando '" + query + "'");

        try {
            List<GoogleBookDTO> results = googleBooksClient.searchBooks(query);

            System.out.println("📚 GoogleBooksService: Encontrados " + results.size() + " resultados");

            return results;

        } catch (Exception e) {
            System.err.println("❌ Error en búsqueda de Google Books: " + e.getMessage());
            throw new RuntimeException("Failed to search Google Books: " + e.getMessage(), e);
        }
    }

    /**
     * OBTENER DETALLES DE UN LIBRO POR ID
     *
     * Obtiene información completa de un libro específico de Google Books
     *
     * @param googleBooksId ID del libro en Google Books
     * @return Información detallada del libro
     */
    public GoogleBookDTO getBookDetails(String googleBooksId) {
        if (googleBooksId == null || googleBooksId.trim().isEmpty()) {
            throw new IllegalArgumentException("Google Books ID cannot be empty");
        }

        System.out.println("📖 GoogleBooksService: Obteniendo detalles para ID " + googleBooksId);

        try {
            GoogleBookDTO book = googleBooksClient.getBookById(googleBooksId);

            if (book == null) {
                throw new RuntimeException("Book not found in Google Books with ID: " + googleBooksId);
            }

            System.out.println("✅ GoogleBooksService: Libro obtenido - " + book.getTitle());

            return book;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo libro de Google Books: " + e.getMessage());
            throw new RuntimeException("Failed to get book details: " + e.getMessage(), e);
        }
    }

    // ========================================
    // MÉTODOS DE IMPORTACIÓN Y CONVERSIÓN
    // ========================================

    /**
     * IMPORTAR LIBRO DESDE GOOGLE BOOKS
     *
     * Método principal para importar un libro de Google Books a tu sistema.
     * Convierte GoogleBookDTO a Book entity y lo guarda con autores y géneros.
     * ADAPTADO a tu BookService.createBook() existente.
     *
     * @param googleBooksId ID del libro en Google Books
     * @param status Estado inicial para el libro (WISHLIST, READING, etc.)
     * @return Book entity guardado en tu sistema
     */
    public Book importFromGoogle(String googleBooksId, BookStatus status) {
        System.out.println("⬇️ GoogleBooksService: Importando libro ID " + googleBooksId);

        // 1. Obtener datos de Google Books
        GoogleBookDTO googleBook = getBookDetails(googleBooksId);

        // 2. Verificar si ya existe (evitar duplicados)
        if (isDuplicate(googleBook)) {
            throw new RuntimeException("Book already exists in your library: " + googleBook.getTitle());
        }

        // 3. Convertir a Book entity usando el mapper
        Book book = googleBooksMapper.toBookEntity(googleBook, status);

        if (book == null) {
            throw new RuntimeException("Failed to convert Google Book to Book entity");
        }

        // 4. Procesar autores y obtener IDs
        Set<Long> authorIds = processAuthorsAndGetIds(googleBook);

        // 5. Procesar géneros y obtener IDs
        Set<Long> genreIds = processGenresAndGetIds(googleBook);

        // 6. Guardar libro usando tu método existente
        Book savedBook = bookService.createBook(book, authorIds, genreIds);

        System.out.println("✅ GoogleBooksService: Libro importado exitosamente - " + savedBook.getTitle());
        System.out.println("📊 GoogleBooksService: " + authorIds.size() + " autores, " + genreIds.size() + " géneros");

        return savedBook;
    }

    /**
     * MAPEAR GoogleBookDTO A Book ENTITY
     *
     * Método de conveniencia que usa el mapper directamente
     *
     * @param googleBook Libro de Google Books
     * @param status Estado inicial
     * @return Book entity (sin guardar)
     */
    public Book mapToBookEntity(GoogleBookDTO googleBook, BookStatus status) {
        if (googleBook == null) {
            throw new IllegalArgumentException("GoogleBookDTO cannot be null");
        }

        if (!googleBooksMapper.isValidGoogleBook(googleBook)) {
            throw new IllegalArgumentException("Invalid Google Book data: " + googleBooksMapper.getBookInfo(googleBook));
        }

        System.out.println("🔄 GoogleBooksService: Mapeando " + googleBooksMapper.getBookInfo(googleBook));

        return googleBooksMapper.toBookEntity(googleBook, status);
    }

    // ========================================
    // GESTIÓN DE DUPLICADOS
    // ========================================

    /**
     * VERIFICAR SI UN LIBRO YA EXISTE (DUPLICADO)
     *
     * Verifica por ISBN y título si el libro ya está en tu biblioteca.
     * ADAPTADO a los métodos que SÍ existen en BookService.
     *
     * @param googleBook Libro a verificar
     * @return true si ya existe, false si es nuevo
     */
    public boolean isDuplicate(GoogleBookDTO googleBook) {
        if (googleBook == null || googleBook.getVolumeInfo() == null) {
            return false;
        }

        String title = googleBook.getTitle();
        String isbn = googleBook.getVolumeInfo().getFirstIsbn();

        System.out.println("🔍 GoogleBooksService: Verificando duplicados para '" + title + "'");

        // 1. Verificar por ISBN usando BookService.existsByIsbn()
        if (isbn != null && !isbn.trim().isEmpty()) {
            if (bookService.existsByIsbn(isbn)) {
                System.out.println("⚠️ GoogleBooksService: Libro ya existe con ISBN " + isbn);
                return true;
            }
        }

        // 2. Verificar por título usando BookService.searchBooks()
        if (title != null && !title.trim().isEmpty()) {
            List<Book> booksByTitle = bookService.searchBooks(title);

            // Verificar coincidencia exacta o muy similar
            boolean titleMatch = booksByTitle.stream()
                    .anyMatch(book -> titlesSimilar(book.getTitle(), title));

            if (titleMatch) {
                System.out.println("⚠️ GoogleBooksService: Libro ya existe con título similar a '" + title + "'");
                return true;
            }
        }

        System.out.println("✅ GoogleBooksService: Libro es único, no hay duplicados");
        return false;
    }

    // ========================================
    // ENRIQUECIMIENTO DE DATOS
    // ========================================

    /**
     * ENRIQUECER LIBRO EXISTENTE CON DATOS DE GOOGLE BOOKS
     *
     * Actualiza un libro existente en tu sistema con datos más completos
     * obtenidos de Google Books (descripción, portada, etc.)
     * ADAPTADO a usar getBookById() + método interno de actualización.
     *
     * @param bookId ID del libro en tu sistema
     * @param googleBooksId ID del libro en Google Books
     * @return Libro actualizado
     */
    public Book enrichExistingBook(Long bookId, String googleBooksId) {
        System.out.println("🔄 GoogleBooksService: Enriqueciendo libro ID " + bookId + " con datos de Google Books");

        // 1. Obtener libro existente usando tu método
        Book existingBook = bookService.getBookById(bookId);
        if (existingBook == null) {
            throw new BookNotFoundException("Book not found with id: " + bookId);
        }

        // 2. Obtener datos de Google Books
        GoogleBookDTO googleBook = getBookDetails(googleBooksId);

        // 3. Enriquecer campos faltantes (sin sobrescribir datos existentes importantes)
        boolean updated = false;

        // Descripción (si no tiene)
        if ((existingBook.getDescription() == null || existingBook.getDescription().trim().isEmpty())
                && googleBook.getVolumeInfo().getDescription() != null) {
            existingBook.setDescription(googleBook.getVolumeInfo().getDescription());
            updated = true;
        }

        // ISBN (si no tiene)
        if ((existingBook.getIsbn() == null || existingBook.getIsbn().trim().isEmpty())
                && googleBook.getVolumeInfo().getFirstIsbn() != null) {
            existingBook.setIsbn(googleBook.getVolumeInfo().getFirstIsbn());
            updated = true;
        }

        // Páginas totales (si no tiene o es 0)
        if ((existingBook.getTotalPages() == null || existingBook.getTotalPages() == 0)
                && googleBook.getVolumeInfo().getPageCount() != null && googleBook.getVolumeInfo().getPageCount() > 0) {
            existingBook.setTotalPages(googleBook.getVolumeInfo().getPageCount());
            updated = true;
        }

        // Publisher (si no tiene)
        if ((existingBook.getPublisher() == null || existingBook.getPublisher().trim().isEmpty())
                && googleBook.getVolumeInfo().getPublisher() != null) {
            existingBook.setPublisher(googleBook.getVolumeInfo().getPublisher());
            updated = true;
        }

        // 4. Guardar cambios si hubo actualizaciones usando el nuevo método
        if (updated) {
            Book updatedBook = bookService.updateBook(existingBook.getId(), existingBook);
            System.out.println("✅ GoogleBooksService: Libro enriquecido exitosamente");
            return updatedBook;
        } else {
            System.out.println("ℹ️ GoogleBooksService: No se encontraron datos adicionales para enriquecer");
            return existingBook;
        }
    }

    // ========================================
    // MÉTODOS DE BÚSQUEDA ESPECÍFICOS
    // ========================================

    /**
     * BUSCAR POR ISBN EN GOOGLE BOOKS
     */
    public GoogleBookDTO searchByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be empty");
        }

        System.out.println("🔍 GoogleBooksService: Buscando por ISBN " + isbn);
        return googleBooksClient.searchByIsbn(isbn);
    }

    /**
     * BUSCAR POR TÍTULO EN GOOGLE BOOKS
     */
    public List<GoogleBookDTO> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        System.out.println("🔍 GoogleBooksService: Buscando por título '" + title + "'");
        return googleBooksClient.searchByTitle(title);
    }

    /**
     * BUSCAR POR AUTOR EN GOOGLE BOOKS
     */
    public List<GoogleBookDTO> searchByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }

        System.out.println("🔍 GoogleBooksService: Buscando por autor '" + author + "'");
        return googleBooksClient.searchByAuthor(author);
    }

    // ========================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================

    /**
     * PROCESAR AUTORES DEL LIBRO DE GOOGLE BOOKS Y OBTENER IDs
     *
     * Convierte los nombres de autores en entidades Author usando tu
     * AuthorService.findOrCreateAuthor() existente, y devuelve sus IDs
     * para usar con tu BookService.createBook(book, authorIds, genreIds)
     */
    private Set<Long> processAuthorsAndGetIds(GoogleBookDTO googleBook) {
        Set<Long> authorIds = new HashSet<>();
        List<String> authorNames = googleBooksMapper.extractAuthorNames(googleBook);

        for (String fullName : authorNames) {
            try {
                // Dividir nombre completo en firstName y lastName
                String[] nameParts = splitFullName(fullName);
                String firstName = nameParts[0];
                String lastName = nameParts[1];

                // Usar tu método existente con 2 parámetros
                Author author = authorService.findOrCreateAuthor(firstName, lastName);
                authorIds.add(author.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Error procesando autor '" + fullName + "': " + e.getMessage());
                // Continúar con otros autores
            }
        }

        // Si no se pudieron procesar autores, crear "Autor desconocido"
        if (authorIds.isEmpty()) {
            try {
                Author unknownAuthor = authorService.findOrCreateAuthor("Autor", "desconocido");
                authorIds.add(unknownAuthor.getId());
            } catch (Exception e) {
                System.err.println("❌ Error creando autor desconocido: " + e.getMessage());
            }
        }

        return authorIds;
    }

    /**
     * PROCESAR GÉNEROS DEL LIBRO DE GOOGLE BOOKS Y OBTENER IDs
     *
     * Convierte las categorías en entidades Genre usando tu
     * GenreService.findOrCreateGenre() existente, y devuelve sus IDs
     */
    private Set<Long> processGenresAndGetIds(GoogleBookDTO googleBook) {
        Set<Long> genreIds = new HashSet<>();
        List<String> genreNames = googleBooksMapper.extractGenreNames(googleBook);

        for (String genreName : genreNames) {
            try {
                // Usar tu método existente con 1 parámetro
                Genre genre = genreService.findOrCreateGenre(genreName);
                genreIds.add(genre.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Error procesando género '" + genreName + "': " + e.getMessage());
                // Continúar con otros géneros
            }
        }

        // Si no se pudieron procesar géneros, crear "General"
        if (genreIds.isEmpty()) {
            try {
                Genre generalGenre = genreService.findOrCreateGenre("General");
                genreIds.add(generalGenre.getId());
            } catch (Exception e) {
                System.err.println("❌ Error creando género general: " + e.getMessage());
            }
        }

        return genreIds;
    }

    /**
     * DIVIDIR NOMBRE COMPLETO EN FIRSTNAME Y LASTNAME
     *
     * Método utilitario para manejar la conversión de nombres de Google Books
     * al formato que espera tu AuthorService.findOrCreateAuthor(firstName, lastName)
     */
    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Autor", "desconocido"};
        }

        String trimmed = fullName.trim();

        // Si no tiene espacios, usar todo como lastName
        if (!trimmed.contains(" ")) {
            return new String[]{"", trimmed};
        }

        // Dividir en primera palabra (firstName) y resto (lastName)
        int firstSpace = trimmed.indexOf(" ");
        String firstName = trimmed.substring(0, firstSpace).trim();
        String lastName = trimmed.substring(firstSpace + 1).trim();

        // Validar que no estén vacíos
        if (firstName.isEmpty()) firstName = "Autor";
        if (lastName.isEmpty()) lastName = "desconocido";

        return new String[]{firstName, lastName};
    }

    /**
     * VERIFICAR SI DOS TÍTULOS SON SIMILARES
     *
     * Compara títulos considerando variaciones menores
     */
    private boolean titlesSimilar(String title1, String title2) {
        if (title1 == null || title2 == null) {
            return false;
        }

        // Normalizar títulos (minúsculas, sin espacios extra, sin puntuación)
        String normalized1 = title1.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", " ").trim();
        String normalized2 = title2.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", " ").trim();

        // Coincidencia exacta después de normalización
        if (normalized1.equals(normalized2)) {
            return true;
        }

        // Coincidencia si uno contiene al otro (para casos como "Clean Code" vs "Clean Code: A Handbook")
        return normalized1.contains(normalized2) || normalized2.contains(normalized1);
    }

    // ========================================
    // MÉTODOS DE INFORMACIÓN Y DEBUGGING
    // ========================================

    /**
     * OBTENER ESTADÍSTICAS DE LA INTEGRACIÓN CON GOOGLE BOOKS
     *
     * Método útil para debugging y monitoreo
     */
    public void showIntegrationStats() {
        System.out.println("📊 GoogleBooksService - Estadísticas de Integración:");
        System.out.println("   ✅ GoogleBooksClient: " + (googleBooksClient != null ? "Configurado" : "No disponible"));
        System.out.println("   ✅ GoogleBooksMapper: " + (googleBooksMapper != null ? "Configurado" : "No disponible"));
        System.out.println("   ✅ BookService: " + (bookService != null ? "Configurado" : "No disponible"));
        System.out.println("   ✅ AuthorService: " + (authorService != null ? "Configurado" : "No disponible"));
        System.out.println("   ✅ GenreService: " + (genreService != null ? "Configurado" : "No disponible"));
    }

    /**
     * VERIFICAR QUE TODAS LAS DEPENDENCIAS ESTÁN CONFIGURADAS
     */
    public boolean isFullyConfigured() {
        boolean configured = googleBooksClient != null &&
                googleBooksMapper != null &&
                bookService != null &&
                authorService != null &&
                genreService != null;

        if (configured) {
            System.out.println("✅ GoogleBooksService completamente configurado");
        } else {
            System.err.println("❌ GoogleBooksService NO completamente configurado");
        }

        return configured;
    }
}