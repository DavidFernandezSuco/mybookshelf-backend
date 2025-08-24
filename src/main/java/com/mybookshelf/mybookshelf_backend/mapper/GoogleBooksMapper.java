package com.mybookshelf.mybookshelf_backend.mapper;

import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookDTO;
import com.mybookshelf.mybookshelf_backend.dto.google.GoogleBookVolumeInfoDTO;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * CLASE GoogleBooksMapper - Convierte GoogleBookDTO a Book entity
 *
 * SIGUIENDO PATRÓN DE TUS MAPPERS EXISTENTES:
 * - @Component para inyección de dependencias
 * - Métodos claros y simples
 * - Manejo seguro de datos opcionales
 * - Validaciones básicas
 * - Nivel junior sin complejidades
 *
 * RESPONSABILIDAD:
 * - Convertir GoogleBookDTO (de la API) a Book entity (tu sistema)
 * - Mapear campos básicos de Google Books a tus campos
 * - Manejar datos faltantes de forma segura
 * - Preparar entidad para que BookService la procese
 */
@Component
public class GoogleBooksMapper {

    // ========================================
    // CONVERSIÓN PRINCIPAL GoogleBookDTO → Book
    // ========================================

    /**
     * CONVIERTE GoogleBookDTO a Book entity
     *
     * Esta es la función principal que convierte un libro de Google Books
     * a tu entidad Book para que pueda ser guardado en tu sistema
     *
     * @param googleBook Libro de Google Books API
     * @param defaultStatus Estado inicial para el libro (ej: WISHLIST)
     * @return Book entity listo para ser procesado por BookService
     */
    public Book toBookEntity(GoogleBookDTO googleBook, BookStatus defaultStatus) {
        if (googleBook == null) {
            return null;
        }

        GoogleBookVolumeInfoDTO volumeInfo = googleBook.getVolumeInfo();
        if (volumeInfo == null) {
            return null; // No podemos crear un libro sin información básica
        }

        Book book = new Book();

        // CAMPOS BÁSICOS OBLIGATORIOS
        book.setTitle(cleanTitle(volumeInfo.getTitle()));
        book.setStatus(defaultStatus != null ? defaultStatus : BookStatus.WISHLIST);

        // CAMPOS OPCIONALES CON MANEJO SEGURO
        book.setDescription(cleanDescription(volumeInfo.getDescription()));
        book.setTotalPages(volumeInfo.getPageCount());
        book.setIsbn(volumeInfo.getFirstIsbn());
        book.setPublisher(cleanPublisher(volumeInfo.getPublisher()));
        book.setPublishedDate(parsePublishedDate(volumeInfo.getPublishedDate()));

        // CAMPOS INICIALES
        book.setCurrentPage(0); // Siempre empezamos desde 0

        return book;
    }

    /**
     * CONVERSIÓN CON ESTADO POR DEFECTO
     *
     * Versión simplificada que usa WISHLIST como estado por defecto
     */
    public Book toBookEntity(GoogleBookDTO googleBook) {
        return toBookEntity(googleBook, BookStatus.WISHLIST);
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA Y VALIDACIÓN
    // ========================================

    /**
     * LIMPIAR Y VALIDAR TÍTULO
     */
    private String cleanTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Título no disponible";
        }

        // Limpiar título
        String cleanedTitle = title.trim();

        // Limitar longitud si es muy largo
        if (cleanedTitle.length() > 500) {
            cleanedTitle = cleanedTitle.substring(0, 497) + "...";
        }

        return cleanedTitle;
    }

    /**
     * LIMPIAR DESCRIPCIÓN
     */
    private String cleanDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null; // Descripción es opcional
        }

        String cleaned = description.trim();

        // Remover HTML tags si existen
        cleaned = cleaned.replaceAll("<[^>]*>", "");

        // Limitar longitud (tu entity permite hasta 2000 caracteres)
        if (cleaned.length() > 2000) {
            cleaned = cleaned.substring(0, 1997) + "...";
        }

        return cleaned;
    }

    /**
     * LIMPIAR PUBLISHER
     */
    private String cleanPublisher(String publisher) {
        if (publisher == null || publisher.trim().isEmpty()) {
            return null;
        }

        String cleaned = publisher.trim();

        // Limitar longitud (tu entity permite hasta 200 caracteres)
        if (cleaned.length() > 200) {
            cleaned = cleaned.substring(0, 197) + "...";
        }

        return cleaned;
    }

    /**
     * PARSEAR FECHA DE PUBLICACIÓN
     *
     * Google Books devuelve fechas en varios formatos:
     * - "2008" (solo año)
     * - "2008-07" (año y mes)
     * - "2008-07-15" (fecha completa)
     */
    private LocalDate parsePublishedDate(String publishedDate) {
        if (publishedDate == null || publishedDate.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = publishedDate.trim();

            // Caso 1: Solo año (ej: "2008")
            if (cleaned.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(cleaned), 1, 1);
            }

            // Caso 2: Año y mes (ej: "2008-07")
            if (cleaned.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(cleaned + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            // Caso 3: Fecha completa (ej: "2008-07-15")
            if (cleaned.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            // Si no coincide con ningún patrón, retornar null
            return null;

        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("⚠️ No se pudo parsear fecha: " + publishedDate);
            return null;
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * OBTENER LISTA DE AUTORES COMO STRING
     *
     * Convierte la lista de autores de Google Books en un String
     * para usar con tu AuthorService.findOrCreateAuthor()
     */
    public List<String> extractAuthorNames(GoogleBookDTO googleBook) {
        if (googleBook == null || googleBook.getVolumeInfo() == null) {
            return List.of();
        }

        List<String> authors = googleBook.getVolumeInfo().getAuthors();
        if (authors == null || authors.isEmpty()) {
            return List.of("Autor desconocido");
        }

        return authors;
    }

    /**
     * OBTENER LISTA DE GÉNEROS COMO STRING
     *
     * Convierte las categorías de Google Books en géneros
     * para usar con tu GenreService.findOrCreateGenre()
     */
    public List<String> extractGenreNames(GoogleBookDTO googleBook) {
        if (googleBook == null || googleBook.getVolumeInfo() == null) {
            return List.of();
        }

        List<String> categories = googleBook.getVolumeInfo().getCategories();
        if (categories == null || categories.isEmpty()) {
            return List.of("General");
        }

        // Limpiar y normalizar categorías
        return categories.stream()
                .map(this::cleanGenreName)
                .distinct()
                .limit(3) // Máximo 3 géneros por libro
                .toList();
    }

    /**
     * LIMPIAR NOMBRE DE GÉNERO
     */
    private String cleanGenreName(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "General";
        }

        String cleaned = category.trim().toLowerCase();

        // Normalizar categorías comunes de Google Books
        switch (cleaned) {
            case "computers":
            case "technology & engineering":
                return "Technology";
            case "juvenile fiction":
            case "young adult fiction":
                return "Young Adult";
            case "business & economics":
                return "Business";
            case "self-help":
                return "Self Help";
            case "biography & autobiography":
                return "Biography";
            default:
                // Capitalizar primera letra
                return category.substring(0, 1).toUpperCase() +
                        category.substring(1).toLowerCase();
        }
    }

    /**
     * VERIFICAR SI UN GoogleBookDTO ES VÁLIDO PARA CONVERSIÓN
     */
    public boolean isValidGoogleBook(GoogleBookDTO googleBook) {
        if (googleBook == null) {
            return false;
        }

        GoogleBookVolumeInfoDTO volumeInfo = googleBook.getVolumeInfo();
        if (volumeInfo == null) {
            return false;
        }

        // Verificar que tenga al menos título
        String title = volumeInfo.getTitle();
        return title != null && !title.trim().isEmpty();
    }

    /**
     * OBTENER INFORMACIÓN PARA DEBUG/LOG
     */
    public String getBookInfo(GoogleBookDTO googleBook) {
        if (googleBook == null) {
            return "GoogleBook: null";
        }

        GoogleBookVolumeInfoDTO volumeInfo = googleBook.getVolumeInfo();
        if (volumeInfo == null) {
            return "GoogleBook: " + googleBook.getId() + " (sin volume info)";
        }

        return String.format("GoogleBook: '%s' por %s (ID: %s)",
                volumeInfo.getTitle(),
                googleBook.getAuthorsAsString(),
                googleBook.getId());
    }
}
