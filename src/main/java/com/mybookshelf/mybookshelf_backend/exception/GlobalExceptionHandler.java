package com.mybookshelf.mybookshelf_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * CLASE GlobalExceptionHandler - "Capturador de Errores Profesional"
 *
 * ¿Para qué sirve?
 * - Intercepta TODAS las excepciones de tu API
 * - Convierte errores feos en respuestas JSON limpias
 * - Proporciona códigos HTTP apropiados
 * - Mejora la experiencia de developers que usan tu API
 *
 * @RestControllerAdvice - Intercepta excepciones de todos los controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * MANEJO DE LIBRO NO ENCONTRADO
     *
     * Cuando BookService lanza BookNotFoundException,
     * este método la captura y devuelve una respuesta limpia
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(
            BookNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse("BOOK_NOT_FOUND", ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * MANEJO DE ERRORES DE VALIDACIÓN
     *
     * Cuando @Valid falla (ej: title vacío), captura el error
     * y devuelve una lista de validaciones que fallaron
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        // Extraer errores campo por campo
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Validation failed");
        response.put("fieldErrors", fieldErrors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * MANEJO DE ERRORES GENERALES
     *
     * Cualquier RuntimeException no específica
     * se convierte en una respuesta 500 limpia
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR",
                "An unexpected error occurred: " + ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * MANEJO DE CUALQUIER OTRA EXCEPCIÓN
     *
     * Safety net para cualquier error no contemplado
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse("UNKNOWN_ERROR",
                "An unknown error occurred");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}