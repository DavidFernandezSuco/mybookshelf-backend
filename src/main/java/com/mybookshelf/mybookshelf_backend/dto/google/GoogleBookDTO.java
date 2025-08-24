package com.mybookshelf.mybookshelf_backend.dto.google;

import java.util.List;

/**
 * CLASE GoogleBookDTO - DTO para libros de Google Books API
 *
 * SIGUIENDO PATRÓN DE BookDTO EXACTAMENTE:
 * - Campos simples y básicos
 * - Sin validaciones complicadas
 * - Solo para recibir datos de Google Books
 * - Se mapea luego a nuestro BookDTO/Book entity
 */
public class GoogleBookDTO {

    // ========================================
    // INFORMACIÓN BÁSICA DE GOOGLE BOOKS
    // ========================================

    /**
     * ID - Identificador único de Google Books
     * Ejemplo: "zyTCAlFPjgYC"
     */
    private String id;

    /**
     * VOLUME_INFO - Información detallada del libro
     * Viene en el JSON como "volumeInfo"
     */
    private GoogleBookVolumeInfoDTO volumeInfo;

    /**
     * SELF_LINK - URL del libro en Google Books
     * Para obtener más detalles si necesitamos
     */
    private String selfLink;

    // ========================================
    // CONSTRUCTORES (SIGUIENDO PATRÓN DTO)
    // ========================================

    /**
     * Constructor vacío - OBLIGATORIO para JSON
     */
    public GoogleBookDTO() {}

    /**
     * Constructor básico - Para tests
     */
    public GoogleBookDTO(String id, GoogleBookVolumeInfoDTO volumeInfo) {
        this.id = id;
        this.volumeInfo = volumeInfo;
    }

    // ========================================
    // MÉTODOS ÚTILES (COMO EN TUS DTOs)
    // ========================================

    /**
     * Obtener título del libro (método de conveniencia)
     */
    public String getTitle() {
        if (volumeInfo != null) {
            return volumeInfo.getTitle();
        }
        return null;
    }

    /**
     * Obtener autores como String (método de conveniencia)
     */
    public String getAuthorsAsString() {
        if (volumeInfo != null && volumeInfo.getAuthors() != null) {
            return String.join(", ", volumeInfo.getAuthors());
        }
        return "Unknown Author";
    }

    /**
     * Verificar si tiene ISBN
     */
    public boolean hasIsbn() {
        return volumeInfo != null &&
                volumeInfo.getIndustryIdentifiers() != null &&
                !volumeInfo.getIndustryIdentifiers().isEmpty();
    }

    // ========================================
    // GETTERS Y SETTERS ESTÁNDAR
    // ========================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GoogleBookVolumeInfoDTO getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(GoogleBookVolumeInfoDTO volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    // ========================================
    // MÉTODOS ESTÁNDAR (COMO TUS DTOs)
    // ========================================

    @Override
    public String toString() {
        return "GoogleBookDTO{" +
                "id='" + id + '\'' +
                ", title='" + getTitle() + '\'' +
                ", authors='" + getAuthorsAsString() + '\'' +
                '}';
    }
}
