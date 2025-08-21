package com.mybookshelf.mybookshelf_backend.controller;

// IMPORTS: Librerías necesarias para REST Controllers
import com.mybookshelf.mybookshelf_backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CLASE AnalyticsController - "Dashboard de Estadísticas REST"
 *
 * ENDPOINTS IMPLEMENTADOS:
 * - GET /api/analytics/dashboard - Estadísticas principales
 * - GET /api/analytics/quick - Métricas rápidas
 * - GET /api/analytics/yearly-progress - Progreso anual
 * - GET /api/analytics/monthly-progress - Progreso mensual año actual
 * - GET /api/analytics/productivity - Estadísticas de productividad
 */

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ========================================
    // ENDPOINTS QUE SÍ FUNCIONAN
    // ========================================

    /**
     * GET /api/analytics/dashboard - ESTADÍSTICAS PRINCIPALES
     *
     * Respuesta ejemplo:
     * {
     *   "totalBooks": 25,
     *   "booksReading": 3,
     *   "booksFinished": 18,
     *   "booksWishlist": 4,
     *   "completionRate": 72.0,
     *   "averagePages": 342.5
     * }
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            var stats = analyticsService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error calculating dashboard stats: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/quick - MÉTRICAS RÁPIDAS
     *
     * Versión simplificada para widgets o notificaciones
     */
    @GetMapping("/quick")
    public ResponseEntity<?> getQuickStats() {
        try {
            var stats = analyticsService.getQuickStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error calculating quick stats: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/yearly-progress - PROGRESO ANUAL
     *
     * Análisis histórico año por año
     */
    @GetMapping("/yearly-progress")
    public ResponseEntity<?> getYearlyProgress() {
        try {
            var yearlyProgress = analyticsService.getYearlyProgress();
            return ResponseEntity.ok(yearlyProgress);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error calculating yearly progress: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/monthly-progress - PROGRESO MENSUAL AÑO ACTUAL
     *
     * Progreso mes por mes del año en curso
     */
    @GetMapping("/monthly-progress")
    public ResponseEntity<?> getMonthlyProgress() {
        try {
            var monthlyProgress = analyticsService.getCurrentYearMonthlyProgress();
            return ResponseEntity.ok(monthlyProgress);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error calculating monthly progress: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/productivity - ESTADÍSTICAS DE PRODUCTIVIDAD
     *
     * Métricas enfocadas en rendimiento y productividad
     */
    @GetMapping("/productivity")
    public ResponseEntity<?> getProductivityStats() {
        try {
            var productivityStats = analyticsService.getProductivityStats();
            return ResponseEntity.ok(productivityStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error calculating productivity stats: " + e.getMessage()));
        }
    }

    /*
     * NOTAS SOBRE ESTA VERSIÓN CORREGIDA:
     *
     * 1. ✅ SOLO MÉTODOS QUE EXISTEN:
     *    - Cada endpoint usa un método real de AnalyticsService
     *    - No hay llamadas a métodos inexistentes
     *    - Funcionará inmediatamente sin errores
     *
     * 2. ✅ MANEJO DE ERRORES:
     *    - Try-catch en todos los endpoints
     *    - Respuestas JSON consistentes
     *    - Mensajes descriptivos para debugging
     *
     * 3. ✅ ENDPOINTS ÚTILES:
     *    - /dashboard - Para homepage principal
     *    - /quick - Para widgets pequeños
     *    - /yearly-progress - Para gráficos anuales
     *    - /monthly-progress - Para charts mensuales
     *    - /productivity - Para métricas de rendimiento
     *
     * 4. ✅ FÁCIL DE PROBAR:
     *    - Todas las URLs son simples
     *    - Solo métodos GET (no parámetros complejos)
     *    - Respuestas JSON listas para frontend
     *
     */
}