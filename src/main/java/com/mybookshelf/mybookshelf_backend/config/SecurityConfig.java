package com.mybookshelf.mybookshelf_backend.config;

// IMPORTS: Librerías necesarias para configurar Spring Security
import org.springframework.context.annotation.Bean;           // Para crear beans de Spring
import org.springframework.context.annotation.Configuration;  // Marca esta clase como configuración
import org.springframework.http.HttpMethod;                   // Para especificar métodos HTTP
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
 * - APIs REST necesitan acceso libre para testing y desarrollo
 * - Swagger UI necesita acceso para documentación
 *
 * CONFIGURACIÓN ACTUAL: Desarrollo/Demo friendly
 * - APIs completamente abiertas para facilitar testing
 * - H2 Console accesible para debugging
 * - Preparado para futuras mejoras de seguridad
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
                 * - APIs REST no necesitan CSRF (se usa para formularios web tradicionales)
                 * - H2 Console no maneja tokens CSRF correctamente
                 * - Facilita testing con Postman, curl, etc.
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
                 * ORDEN IMPORTANTE: las reglas más específicas van primero
                 *
                 * .requestMatchers("/h2-console/**").permitAll()
                 * - Permite acceso SIN autenticación a H2 Console
                 * - /** significa "cualquier subcarpeta o archivo"
                 * - Esencial para debugging durante desarrollo
                 *
                 * .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                 * - Permite acceso libre a documentación Swagger
                 * - /swagger-ui/** = interfaz web de Swagger
                 * - /v3/api-docs/** = endpoints de OpenAPI JSON
                 *
                 * .requestMatchers("/api/**").permitAll()
                 * - 🔥 CLAVE: Permite acceso libre a TODA tu API REST
                 * - Durante desarrollo/demo, no requiere autenticación
                 * - Facilita testing inmediato de endpoints
                 *
                 * .anyRequest().authenticated()
                 * - TODAS las demás URLs requieren autenticación
                 * - Si no estás logueado, Spring redirige a login
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()          // H2 Console libre
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger libre
                        .requestMatchers("/api/**").permitAll()                 // 🔥 API REST LIBRE
                        .anyRequest().authenticated()                           // Resto protegido
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
                 * NOTA: frameOptions() está DEPRECADO desde Spring Security 6.1
                 * Usamos el nuevo método recomendado con lambda
                 *
                 * .frameOptions(frameOptions -> frameOptions.disable())
                 * - Sintaxis moderna para deshabilitar frame options
                 * - Reemplaza el antiguo .frameOptions().disable()
                 */
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        // Devolver la configuración construida
        return http.build();
    }

    /**
     * NOTAS IMPORTANTES SOBRE ESTA CONFIGURACIÓN:
     *
     * 1. CONFIGURACIÓN DE DESARROLLO:
     *    - APIs completamente abiertas para facilitar testing
     *    - H2 Console accesible para debugging
     *    - Swagger accesible para documentación
     *    - Sin autenticación requerida para /api/**
     *
     * 2. VENTAJAS ACTUALES:
     *    - Testing inmediato con Postman/curl
     *    - Demo funcional sin complicaciones
     *    - Desarrollo ágil sin obstáculos de auth
     *    - Debugging fácil de base de datos
     *
     * 3. URLS AFECTADAS:
     *    - http://localhost:8080/api/books → ✅ LIBRE
     *    - http://localhost:8080/api/authors → ✅ LIBRE
     *    - http://localhost:8080/h2-console → ✅ LIBRE
     *    - http://localhost:8080/swagger-ui → ✅ LIBRE
     *    - http://localhost:8080/ → ❌ Requiere login
     *
     * 4. EVOLUCIÓN FUTURA:
     *    Esta configuración es perfecta para las fases actuales del proyecto.
     *    Más adelante podemos implementar:
     *    - JWT authentication para APIs
     *    - Roles y permisos granulares
     *    - Rate limiting
     *    - HTTPS obligatorio en producción
     *
     * 5. SEGURIDAD EN PRODUCCIÓN:
     *    Cuando despliegues a producción, considera:
     *    - Habilitar CSRF para formularios web
     *    - Restringir frame options
     *    - Usar base de datos real (no H2)
     *    - Implementar autenticación robusta
     *    - HTTPS obligatorio
     *    - Validación de input estricta
     */
}