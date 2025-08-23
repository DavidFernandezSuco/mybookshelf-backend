package com.mybookshelf.mybookshelf_backend.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * CLASE GoogleBookVolumeInfoDTO - Información detallada del libro
 *
 * SIGUIENDO PATRÓN DE TUS DTOs:
 * - Solo campos que realmente necesitamos
 * - Nombres simples y claros
 * - Sin complejidades innecesarias
 *
 * MAPEA EL JSON "volumeInfo" de Google Books
 */
public class GoogleBookVolumeInfoDTO {

    // ========================================
    // INFORMACIÓN BÁSICA DEL LIBRO
    // ========================================

    /**
     * TITLE - Título del libro
     */
    private String title;

    /**
     * AUTHORS - Lista de autores
     * Viene como array en JSON
     */
    private List<String> authors;

    /**
     * DESCRIPTION - Descripción del libro
     */
    private String description;

    /**
     * PAGE_COUNT - Número de páginas
     * En JSON viene como "pageCount"
     */
    @JsonProperty("pageCount")
    private Integer pageCount;

    /**
     * PUBLISHED_DATE - Fecha de publicación
     * Viene como String en diversos formatos
     */
    @JsonProperty("publishedDate")
    private String publishedDate;

    /**
     * PUBLISHER - Editorial
     */
    private String publisher;

    /**
     * CATEGORIES - Géneros/categorías
     * Lista de categorías del libro
     */
    private List<String> categories;

    /**
     * LANGUAGE - Idioma del libro
     */
    private String language;

    // ========================================
    // INFORMACIÓN TÉCNICA
    // ========================================

    /**
     * INDUSTRY_IDENTIFIERS - ISBNs y otros identificadores
     * Array de objetos con type y identifier
     */
    @JsonProperty("industryIdentifiers")
    private List<IndustryIdentifierDTO> industryIdentifiers;

    /**
     * IMAGE_LINKS - URLs de imágenes de portada
     * Objeto con thumbnail, small, medium, large
     */
    @JsonProperty("imageLinks")
    private ImageLinksDTO imageLinks;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    public GoogleBookVolumeInfoDTO() {}

    // ========================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================

    /**
     * Obtener primer ISBN disponible
     */
    public String getFirstIsbn() {
        if (industryIdentifiers != null) {
            return industryIdentifiers.stream()
                    .filter(id -> "ISBN_13".equals(id.getType()) || "ISBN_10".equals(id.getType()))
                    .map(IndustryIdentifierDTO::getIdentifier)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Obtener URL de imagen thumbnail
     */
    public String getThumbnailUrl() {
        if (imageLinks != null) {
            return imageLinks.getThumbnail();
        }
        return null;
    }

    /**
     * Obtener primer género/categoría
     */
    public String getFirstCategory() {
        if (categories != null && !categories.isEmpty()) {
            return categories.get(0);
        }
        return null;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<IndustryIdentifierDTO> getIndustryIdentifiers() {
        return industryIdentifiers;
    }

    public void setIndustryIdentifiers(List<IndustryIdentifierDTO> industryIdentifiers) {
        this.industryIdentifiers = industryIdentifiers;
    }

    public ImageLinksDTO getImageLinks() {
        return imageLinks;
    }

    public void setImageLinks(ImageLinksDTO imageLinks) {
        this.imageLinks = imageLinks;
    }

    // ========================================
    // CLASES INTERNAS SIMPLES
    // ========================================

    /**
     * Para mapear industryIdentifiers (ISBN, etc.)
     */
    public static class IndustryIdentifierDTO {
        private String type;      // "ISBN_13", "ISBN_10", etc.
        private String identifier; // El código actual

        public IndustryIdentifierDTO() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }

    /**
     * Para mapear imageLinks (portadas)
     */
    public static class ImageLinksDTO {
        private String thumbnail;
        private String small;
        private String medium;
        private String large;

        public ImageLinksDTO() {}

        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
        public String getSmall() { return small; }
        public void setSmall(String small) { this.small = small; }
        public String getMedium() { return medium; }
        public void setMedium(String medium) { this.medium = medium; }
        public String getLarge() { return large; }
        public void setLarge(String large) { this.large = large; }
    }
}