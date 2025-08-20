package com.mybookshelf.mybookshelf_backend.model;

/**
 * ENUM ReadingMood - Estados emocionales durante la lectura
 *
 * Representa cómo te sientes mientras lees un libro en una sesión específica.
 *
 * ¿Para qué sirve?
 * - Analizar qué afecta tu experiencia de lectura
 * - Correlacionar estado de ánimo con productividad (páginas leídas)
 * - Recordar experiencias pasadas con libros específicos
 * - Identificar patrones: ¿leo mejor cuando estoy relajado?
 *
 * CASOS DE USO:
 * - "Me siento EXCITED leyendo este thriller"
 * - "Estoy TIRED, solo pude leer 5 páginas"
 * - "FOCUSED durante toda la sesión de estudio"
 * - "DISTRACTED por el ruido del café"
 */
public enum ReadingMood {

    /**
     * EXCITED - Emocionado, entusiasmado
     *
     * Cuándo usar:
     * - El libro es muy interesante y no puedes parar
     * - Plot twists, momentos emocionantes
     * - Anticipación por saber qué pasa después
     *
     * Ejemplo: "No podía parar de leer, la historia me tiene enganchado"
     */
    EXCITED,

    /**
     * RELAXED - Relajado, tranquilo
     *
     * Cuándo usar:
     * - Lectura calmada y placentera
     * - Ambiente cómodo y sin prisa
     * - Disfrutando el proceso sin estrés
     *
     * Ejemplo: "Leyendo en el sofá con una taza de té, muy relajado"
     */
    RELAXED,

    /**
     * FOCUSED - Concentrado, enfocado
     *
     * Cuándo usar:
     * - Lectura de estudio o técnica que requiere atención
     * - Ambiente silencioso y sin distracciones
     * - Máxima concentración en el contenido
     *
     * Ejemplo: "Estudiando este manual técnico con total concentración"
     */
    FOCUSED,

    /**
     * TIRED - Cansado, fatigado
     *
     * Cuándo usar:
     * - Al final del día, con poca energía
     * - Te cuesta mantener la atención
     * - Lees más lento de lo normal
     *
     * Ejemplo: "Muy cansado después del trabajo, solo pude leer 10 páginas"
     */
    TIRED,

    /**
     * DISTRACTED - Distraído, sin concentración
     *
     * Cuándo usar:
     * - Muchas interrupciones externas
     * - Mente ocupada en otros temas
     * - Ambiente ruidoso o inadecuado
     *
     * Ejemplo: "Leyendo en el metro, mucho ruido y gente, difícil concentrarme"
     */
    DISTRACTED;

    /**
     * Método de utilidad: Obtiene descripción legible del mood
     *
     * @return String descripción en español del estado
     *
     * Útil para mostrar en interfaces de usuario
     */
    public String getDisplayName() {
        return switch(this) {
            case EXCITED -> "Emocionado";
            case RELAXED -> "Relajado";
            case FOCUSED -> "Concentrado";
            case TIRED -> "Cansado";
            case DISTRACTED -> "Distraído";
        };
    }

    /**
     * Método de utilidad: Obtiene emoji representativo
     *
     * @return String emoji que representa el mood
     *
     * Útil para interfaces visuales
     */
    public String getEmoji() {
        return switch(this) {
            case EXCITED -> "🤩";
            case RELAXED -> "😌";
            case FOCUSED -> "🧠";
            case TIRED -> "😴";
            case DISTRACTED -> "😵‍💫";
        };
    }

    /**
     * Método de utilidad: Verifica si es un mood positivo
     *
     * @return true si el mood es generalmente positivo para la lectura
     */
    public boolean isPositive() {
        return this == EXCITED || this == RELAXED || this == FOCUSED;
    }

    /**
     * Método de utilidad: Verifica si es un mood que afecta negativamente la lectura
     *
     * @return true si el mood puede reducir la productividad de lectura
     */
    public boolean isNegative() {
        return this == TIRED || this == DISTRACTED;
    }
}