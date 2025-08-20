package com.mybookshelf.mybookshelf_backend.config;

// IMPORTS: Librerías necesarias para configurar Spring Security
import org.springframework.context.annotation.Bean;           // Para crear beans de Spring
import org.springframework.context.annotation.Configuration;  // Marca esta clase como configuración
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Para configurar seguridad HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Habilita Spring Security
import org.springframework.security.web.SecurityFilterChain; // Cadena de filtros de seguridad

/**
 * CLASE SecurityConfig - Configuración de Seguridad
 *
 * ¿Qué es Spring Security?
 * - Framework de seguridad para aplicaciones Spring
 * - Maneja autenticación (quién eres) y autorización (qué puedes hacer)
 * - Por defecto, protege TODAS las URLs de la aplicación
 *
 * ¿Por qué necesitamos esta configuración?
 * - H2 Console necesita acceso sin restricciones durante desarrollo
 * - Spring Security por defecto bloquea frames e iframes (H2 Console los usa)
 * - CSRF protection interfiere con H2 Console
 *
 * NOTA IMPORTANTE: Esta configuración es SOLO para desarrollo
 * En producción usaríamos configuración más estricta
 */

@Configuration  // Le dice a Spring que esta clase contiene configuración
@EnableWebSecurity  // Habilita las funcionalidades de Spring Security en la aplicación
public class SecurityConfig {

    /**
     * MÉTODO filterChain - Configura la cadena de filtros de seguridad
     *
     * ¿Qué es un SecurityFilterChain?
     * - Serie de filtros que procesan las peticiones HTTP
     * - Cada filtro verifica diferentes aspectos de seguridad
     * - Se ejecutan en orden antes de llegar al controlador
     *
     * @Bean - Indica que Spring debe gestionar este objeto
     * HttpSecurity - Objeto para configurar seguridad HTTP
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ========================================
                // CONFIGURACIÓN CSRF (Cross-Site Request Forgery)
                // ========================================

                /**
                 * CSRF Protection - Protección contra ataques de falsificación de peticiones
                 *
                 * ¿Qué es CSRF?
                 * - Ataque donde un sitio malicioso hace peticiones a tu aplicación
                 * - Spring Security incluye tokens CSRF para prevenir esto
                 *
                 * ¿Por qué lo deshabilitamos?
                 * - H2 Console no maneja tokens CSRF correctamente
                 * - En desarrollo es seguro deshabilitarlo
                 * - En producción con APIs REST no suele ser necesario (se usa para formularios web)
                 *
                 * .csrf(csrf -> csrf.disable()) - Sintaxis moderna de Spring Security 6+
                 */
                .csrf(csrf -> csrf.disable())

                // ========================================
                // CONFIGURACIÓN DE AUTORIZACIÓN
                // ========================================

                /**
                 * Authorization Rules - Reglas de autorización por URL
                 *
                 * authorizeHttpRequests - Configura qué URLs requieren autenticación
                 *
                 * .requestMatchers("/h2-console/**").permitAll()
                 * - Permite acceso SIN autenticación a cualquier URL que empiece con /h2-console/
                 * - /** significa "cualquier subcarpeta o archivo"
                 * - .permitAll() = acceso libre, sin login
                 *
                 * .anyRequest().authenticated()
                 * - TODAS las demás URLs requieren autenticación
                 * - Si no estás logueado, te redirige a la página de login
                 *
                 * Orden IMPORTANTE: las reglas más específicas van primero
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()  // H2 Console libre
                        .anyRequest().authenticated()                   // Todo lo demás requiere login
                )

                // ========================================
                // CONFIGURACIÓN DE HEADERS HTTP
                // ========================================

                /**
                 * Headers Configuration - Configuración de cabeceras de seguridad
                 *
                 * ¿Qué son Frame Options?
                 * - Cabecera HTTP que controla si tu página puede mostrarse en un frame/iframe
                 * - Por seguridad, Spring Security incluye "X-Frame-Options: DENY"
                 * - Esto previene ataques de clickjacking
                 *
                 * ¿Por qué deshabilitamos Frame Options?
                 * - H2 Console usa frames internamente para mostrar su interfaz
                 * - Si no lo deshabilitamos, H2 Console aparece en blanco
                 *
                 * .frameOptions().disable() - Permite que la aplicación se muestre en frames
                 *
                 * NOTA: En producción, esto sería un riesgo de seguridad
                 */
                .headers(headers -> headers.frameOptions().disable());

        // Devolver la configuración construida
        return http.build();
    }

    /**
     * NOTAS ADICIONALES SOBRE SEGURIDAD:
     *
     * 1. CONFIGURACIÓN TEMPORAL:
     *    Esta configuración es específica para desarrollo.
     *    Permite acceso fácil a H2 Console para ver/modificar datos.
     *
     * 2. PRODUCCIÓN:
     *    En producción usaríamos:
     *    - CSRF habilitado para formularios web
     *    - Frame options habilitadas
     *    - Base de datos real (PostgreSQL, MySQL)
     *    - JWT tokens para APIs REST
     *    - HTTPS obligatorio
     *
     * 3. CREDENCIALES ACTUALES:
     *    Las credenciales están en application.properties:
     *    - Username: admin
     *    - Password: admin123
     *
     * 4. URLS AFECTADAS:
     *    - http://localhost:8080/h2-console → Acceso libre
     *    - http://localhost:8080/ → Requiere login (admin/admin123)
     *    - http://localhost:8080/api/* → Requiere login (cuando creemos APIs)
     *
     * 5. PRÓXIMOS PASOS:
     *    Más adelante configuraremos JWT para APIs REST profesionales
     */
}