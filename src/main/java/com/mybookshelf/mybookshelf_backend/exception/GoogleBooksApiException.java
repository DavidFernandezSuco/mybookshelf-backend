package com.mybookshelf.mybookshelf_backend.exception;

/**
 * EXCEPCIÓN PARA ERRORES DE GOOGLE BOOKS API
 *
 * Siguiendo patrón simple - RuntimeException básica
 * Para manejar errores específicos de la integración con Google Books
 *
 * NIVEL JUNIOR - Sin complejidades innecesarias
 * Mantiene consistencia con el resto de tu proyecto
 */
public class GoogleBooksApiException extends RuntimeException {

    /**
     * Constructor con mensaje
     */
    public GoogleBooksApiException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa
     */
    public GoogleBooksApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor solo con causa
     */
    public GoogleBooksApiException(Throwable cause) {
        super(cause);
    }
}
