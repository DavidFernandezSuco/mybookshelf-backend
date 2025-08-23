package com.mybookshelf.mybookshelf_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST DE CONEXIÓN BÁSICA CON GOOGLE BOOKS API
 *
 * Este test verifica que:
 * - La configuración está correcta
 * - Podemos hacer llamadas a Google Books API
 * - Recibimos respuestas válidas
 */
@SpringBootTest
public class GoogleBooksConnectionTest {

    @Autowired
    private RestTemplate googleBooksRestTemplate;

    @Value("${google.books.api.key}")
    private String apiKey;

    @Value("${google.books.api.url}")
    private String apiUrl;

    /**
     * TEST 1: Verificar configuración básica
     */
    @Test
    public void testConfigurationLoaded() {
        // Verificar que los valores se inyectan correctamente
        assertNotNull(apiKey, "API Key debe estar configurada");
        assertNotNull(apiUrl, "API URL debe estar configurada");
        assertNotNull(googleBooksRestTemplate, "RestTemplate debe estar configurado");

        // Verificar que no están vacías
        assertFalse(apiKey.isEmpty(), "API Key no debe estar vacía");
        assertFalse(apiUrl.isEmpty(), "API URL no debe estar vacía");

        System.out.println("✅ Configuración cargada correctamente");
        System.out.println("📡 API URL: " + apiUrl);
        System.out.println("🔑 API Key configurada: " + (apiKey.length() > 10 ? "Sí" : "No"));
    }

    /**
     * TEST 2: Conexión básica a Google Books API
     */
    @Test
    public void testGoogleBooksConnection() {
        // URL de prueba simple: buscar "java"
        String testUrl = apiUrl + "?q=java&key=" + apiKey + "&maxResults=1";

        try {
            // Hacer llamada básica
            String response = googleBooksRestTemplate.getForObject(testUrl, String.class);

            // Verificaciones básicas
            assertNotNull(response, "Response no debe ser null");
            assertTrue(response.contains("totalItems"), "Response debe contener 'totalItems'");
            assertTrue(response.contains("items") || response.contains("totalItems"),
                    "Response debe ser JSON válido de Google Books");

            System.out.println("✅ Conexión exitosa con Google Books API");
            System.out.println("📝 Response preview: " + response.substring(0, Math.min(200, response.length())) + "...");

        } catch (Exception e) {
            fail("Error conectando con Google Books API: " + e.getMessage());
        }
    }

    /**
     * TEST 3: Verificar formato de respuesta
     */
    @Test
    public void testResponseFormat() {
        String testUrl = apiUrl + "?q=clean+code&key=" + apiKey + "&maxResults=1";

        try {
            String response = googleBooksRestTemplate.getForObject(testUrl, String.class);

            // Verificar que es JSON válido con estructura esperada
            assertTrue(response.startsWith("{"), "Response debe ser JSON válido");
            assertTrue(response.contains("\"totalItems\""), "Debe contener totalItems");

            System.out.println("✅ Formato de respuesta correcto");

        } catch (Exception e) {
            fail("Error verificando formato: " + e.getMessage());
        }
    }
}