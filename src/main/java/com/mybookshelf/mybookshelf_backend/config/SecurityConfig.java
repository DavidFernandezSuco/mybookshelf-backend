package com.mybookshelf.mybookshelf_backend.config;

// IMPORTS: Librerías necesarias para configurar Spring Security
import org.springframework.context.annotation.Bean;           // Para crear beans de Spring
import org.springframework.context.annotation.Configuration;  // Marca esta clase como configuración
import org.springframework.http.HttpMethod;                   // Para especificar métodos HTTP
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Para configurar seguridad HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Habilita Spring Security
import org.springframework.security.web.SecurityFilterChain; // Cadena de filtros de seguridad

/**
 * CLASE SecurityConfig - Configuración de Seguridad ACTUALIZADA
 *
 * ¿Qué es Spring Security?
 * - Framework de seguridad para aplicaciones Spring
 * - Maneja autenticación (quién eres) y autorización (qué puedes hacer)
 * - Por defecto, protege TODAS las URLs de la aplicación
 *
 * ¿Por qué necesitamos esta configuración?
 * - H2 Console necesita acceso sin restricciones durante desarrollo
 * - APIs REST necesitan acceso libre para testing y desarrollo
 * - Swagger UI necesita acceso para documentación interactiva ← NUEVO
 * - Exception handling necesita funcionar sin autenticación
 *
 * CONFIGURACIÓN ACTUALIZADA: Development + Portfolio friendly
 * - APIs completamente abiertas para facilitar testing
 * - H2 Console accesible para debugging
 * - Swagger UI accesible para documentación profesional ← NUEVO
 * - Preparado para demos y presentaciones de portfolio
 */

@Configuration  // Le dice a Spring que esta clase contiene configuración
@EnableWebSecurity  // Habilita las funcionalidades de Spring Security en la aplicación
public class SecurityConfig {

    /**
     * MÉTODO filterChain - Configura la cadena de filtros de seguridad
     *
     * ACTUALIZADO: Incluye soporte completo para Swagger/OpenAPI
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
                 * - Swagger UI necesita hacer peticiones sin tokens CSRF ← NUEVO
                 * - Facilita testing con Postman, curl, etc.
                 *
                 * .csrf(csrf -> csrf.disable()) - Sintaxis moderna de Spring Security 6+
                 */
                .csrf(csrf -> csrf.disable())

                // ========================================
                // CONFIGURACIÓN DE AUTORIZACIÓN ACTUALIZADA
                // ========================================

                /**
                 * Authorization Rules - Reglas de autorización por URL
                 *
                 * authorizeHttpRequests - Configura qué URLs requieren autenticación
                 *
                 * ORDEN IMPORTANTE: las reglas más específicas van primero
                 *
                 * 🆕 SWAGGER/OPENAPI ENDPOINTS:
                 * .requestMatchers("/swagger-ui/**").permitAll()
                 * - Permite acceso libre a la interfaz web de Swagger
                 * - URL: http://localhost:8080/swagger-ui/index.html
                 * - Esencial para documentación interactiva
                 *
                 * .requestMatchers("/v3/api-docs/**").permitAll()
                 * - Permite acceso libre a los endpoints de OpenAPI JSON
                 * - URL: http://localhost:8080/v3/api-docs
                 * - Swagger UI lee estos JSON para generar la documentación
                 *
                 * .requestMatchers("/swagger-ui.html").permitAll()
                 * - URL alternativa de Swagger (algunas versiones)
                 * - Compatibilidad con diferentes configuraciones
                 *
                 * H2 CONSOLE:
                 * .requestMatchers("/h2-console/**").permitAll()
                 * - Permite acceso SIN autenticación a H2 Console
                 * - /** significa "cualquier subcarpeta o archivo"
                 * - Esencial para debugging durante desarrollo
                 *
                 * API REST:
                 * .requestMatchers("/api/**").permitAll()
                 * - 🔥 CLAVE: Permite acceso libre a TODA tu API REST
                 * - Durante desarrollo/demo, no requiere autenticación
                 * - Facilita testing inmediato de endpoints
                 * - Permite que Swagger UI ejecute peticiones
                 *
                 * RESTO DE APLICACIÓN:
                 * .anyRequest().authenticated()
                 * - TODAS las demás URLs requieren autenticación
                 * - Si no estás logueado, Spring redirige a login
                 */
                .authorizeHttpRequests(auth -> auth
                        // 🆕 SWAGGER/OPENAPI - Documentación interactiva
                        .requestMatchers("/swagger-ui/**").permitAll()          // Swagger UI
                        .requestMatchers("/v3/api-docs/**").permitAll()         // OpenAPI JSON
                        .requestMatchers("/swagger-ui.html").permitAll()        // URL alternativa

                        // HERRAMIENTAS DE DESARROLLO
                        .requestMatchers("/h2-console/**").permitAll()          // H2 Console libre

                        // API REST - Tu aplicación principal
                        .requestMatchers("/api/**").permitAll()                 // 🔥 API REST LIBRE

                        // RESTO DE LA APLICACIÓN
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
                 * - Swagger UI también puede usar frames en algunos componentes ← NUEVO
                 * - Si no lo deshabilitamos, estas herramientas aparecen en blanco
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
     * NOTAS IMPORTANTES SOBRE ESTA CONFIGURACIÓN ACTUALIZADA:
     *
     * 1. 🆕 SWAGGER INTEGRATION:
     *    - Swagger UI completamente accesible sin autenticación
     *    - OpenAPI endpoints liberados para documentación automática
     *    - URLs de documentación: /swagger-ui/index.html, /v3/api-docs
     *    - Perfecto para demos de portfolio y presentaciones
     *
     * 2. CONFIGURACIÓN DE DESARROLLO:
     *    - APIs completamente abiertas para facilitar testing
     *    - H2 Console accesible para debugging
     *    - Swagger accesible para documentación interactiva
     *    - Sin autenticación requerida para herramientas de desarrollo
     *
     * 3. VENTAJAS ACTUALES:
     *    - Testing inmediato con Postman/curl/Swagger UI
     *    - Demo funcional sin complicaciones de autenticación
     *    - Desarrollo ágil sin obstáculos de auth
     *    - Documentación profesional con Swagger UI
     *    - Debugging fácil de base de datos con H2 Console
     *
     * 4. URLS LIBERADAS:
     *    - http://localhost:8080/api/** → ✅ API REST LIBRE
     *    - http://localhost:8080/swagger-ui/index.html → ✅ SWAGGER UI LIBRE
     *    - http://localhost:8080/v3/api-docs → ✅ OPENAPI JSON LIBRE
     *    - http://localhost:8080/h2-console → ✅ H2 CONSOLE LIBRE
     *    - http://localhost:8080/ → ❌ Requiere login (otras páginas)
     *
     * 5. PORTFOLIO VALUE:
     *    - Swagger UI proporciona documentación profesional automática
     *    - Fácil demostración de capacidades API a recruiters
     *    - Testing interactivo sin herramientas externas
     *    - Aspecto profesional comparable a APIs comerciales
     *
     * 6. EVOLUCIÓN FUTURA (Post-Portfolio):
     *    Esta configuración es perfecta para desarrollo y demo.
     *    Más adelante se puede implementar:
     *    - JWT authentication para APIs protegidas
     *    - Roles y permisos granulares por endpoint
     *    - Rate limiting para prevenir abuso
     *    - API keys para consumidores externos
     *    - HTTPS obligatorio en producción
     *
     * 7. SEGURIDAD EN PRODUCCIÓN:
     *    Cuando despliegues a producción, considera:
     *    - Restringir acceso a Swagger UI (solo desarrollo)
     *    - Implementar autenticación robusta para APIs
     *    - Habilitar CSRF para formularios web
     *    - Usar base de datos real con credenciales seguras
     *    - HTTPS obligatorio y certificados SSL
     *    - Validación de input estricta y rate limiting
     *
     * 8. TESTING & DEMO READY:
     *    - ✅ Swagger UI funcionará inmediatamente
     *    - ✅ Todos los endpoints documentados automáticamente
     *    - ✅ Testing directo desde navegador
     *    - ✅ Professional API explorer para presentaciones
     */
}