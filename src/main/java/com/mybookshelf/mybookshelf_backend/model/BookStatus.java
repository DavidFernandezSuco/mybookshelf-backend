package com.mybookshelf.mybookshelf_backend.model;

/**
 * ENUM BookStatus
 *
 * Un enum es un tipo especial de clase que representa un conjunto fijo de constantes.
 * En este caso, representa los posibles estados de un libro en nuestra biblioteca.
 *
 * ¿Por qué usar enum en lugar de String?
 * - Seguridad de tipos: Solo permite valores válidos
 * - IntelliSense: El IDE sugiere opciones disponibles
 * - Performance: Más eficiente que Strings
 * - Validación automática: JPA valida que el valor sea correcto
 */
public enum BookStatus {

    /**
     * WISHLIST - Lista de deseos
     *
     * Estado inicial de un libro cuando lo añadimos al sistema
     * pero aún no hemos empezado a leerlo.
     *
     * Ejemplo: "Quiero leer este libro algún día"
     */
    WISHLIST,

    /**
     * READING - Leyendo actualmente
     *
     * El libro está siendo leído en este momento.
     * Típicamente solo deberíamos tener 1-3 libros en este estado.
     *
     * Ejemplo: "Estoy en la página 150 de 300"
     */
    READING,

    /**
     * FINISHED - Libro terminado
     *
     * El libro se ha leído completamente.
     * Es el estado final exitoso de un libro.
     *
     * Ejemplo: "Terminé de leer este libro el 15/08/2025"
     */
    FINISHED,

    /**
     * ABANDONED - Libro abandonado
     *
     * Se empezó a leer pero se decidió no continuar.
     * Diferente de ON_HOLD porque es una decisión definitiva.
     *
     * Ejemplo: "Leí 50 páginas pero no me gustó"
     */
    ABANDONED,

    /**
     * ON_HOLD - En pausa temporal
     *
     * Se empezó a leer pero está pausado temporalmente.
     * La intención es retomarlo más adelante.
     *
     * Ejemplo: "Pausé para leer otro libro más urgente"
     */
    ON_HOLD

    // Nota: Los enums no necesitan punto y coma al final,
    // pero podrías añadir métodos adicionales si quisieras:

    /*
    // Ejemplo de método adicional (opcional):
    public String getDisplayName() {
        return switch(this) {
            case WISHLIST -> "Lista de Deseos";
            case READING -> "Leyendo";
            case FINISHED -> "Terminado";
            case ABANDONED -> "Abandonado";
            case ON_HOLD -> "En Pausa";
        };
    }
    */
}