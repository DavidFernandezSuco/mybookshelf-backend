package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.mybookshelf.mybookshelf_backend.repository.BookRepository;
import com.mybookshelf.mybookshelf_backend.repository.ReadingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS para AnalyticsService
 *
 * Portfolio Demo: Testing de cálculos y business intelligence
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReadingSessionRepository sessionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    /**
     * TEST 1: Dashboard Stats - Funcionalidad Básica
     *
     * Verifica que el método funciona sin errores y retorna datos válidos
     */
    @Test
    void shouldReturnDashboardStatsWithoutErrors() {
        // Given - Mock responses básicas
        when(bookRepository.count()).thenReturn(10L);
        when(bookRepository.countByStatus(any(BookStatus.class))).thenReturn(2L);
        when(bookRepository.countBooksFinishedInYear(anyInt())).thenReturn(5L);

        // When - Ejecutar método
        AnalyticsService.DashboardStatsDTO result = analyticsService.getDashboardStats();

        // Then - Verificar que funciona sin lanzar excepciones
        assertNotNull(result, "Dashboard stats should not be null");
        assertTrue(result.getTotalBooks() >= 0, "Total books should be non-negative");
        assertTrue(result.getBooksReading() >= 0, "Books reading should be non-negative");
        assertTrue(result.getBooksFinished() >= 0, "Books finished should be non-negative");
        assertTrue(result.getBooksWishlist() >= 0, "Books wishlist should be non-negative");

        // Verificar que se llamaron los métodos principales
        verify(bookRepository, atLeast(1)).count();
        verify(bookRepository, atLeast(1)).countByStatus(any(BookStatus.class));
        verify(bookRepository, atLeast(1)).countBooksFinishedInYear(anyInt());
    }

    /**
     * TEST 2: Quick Stats - Cálculo de Porcentajes
     *
     * Verifica el cálculo correcto de porcentajes con datos específicos
     */
    @Test
    void shouldCalculateQuickStatsPercentageCorrectly() {
        // Given - Datos específicos para cálculo de porcentaje
        when(bookRepository.countByStatus(BookStatus.READING)).thenReturn(2L);
        when(bookRepository.countByStatus(BookStatus.FINISHED)).thenReturn(3L);
        when(bookRepository.count()).thenReturn(10L);

        // When - Ejecutar método
        AnalyticsService.QuickStatsDTO result = analyticsService.getQuickStats();

        // Then - Verificar cálculos específicos
        assertNotNull(result, "Quick stats should not be null");
        assertEquals(2L, result.getBooksReading(), "Reading books count should match");
        assertEquals(3L, result.getBooksFinished(), "Finished books count should match");
        assertEquals(10L, result.getTotalBooks(), "Total books count should match");
        assertEquals(30.0, result.getProgressPercentage(), 0.01, "Progress percentage should be 30%");

        // Verificar llamadas a repository
        verify(bookRepository).countByStatus(BookStatus.READING);
        verify(bookRepository).countByStatus(BookStatus.FINISHED);
        verify(bookRepository).count();
    }

    /**
     * TEST 3: Quick Stats con Biblioteca Vacía
     *
     * Verifica manejo correcto de casos edge (división por cero)
     */
    @Test
    void shouldHandleEmptyLibraryInQuickStats() {
        // Given - Biblioteca vacía
        when(bookRepository.countByStatus(BookStatus.READING)).thenReturn(0L);
        when(bookRepository.countByStatus(BookStatus.FINISHED)).thenReturn(0L);
        when(bookRepository.count()).thenReturn(0L);

        // When - Ejecutar método
        AnalyticsService.QuickStatsDTO result = analyticsService.getQuickStats();

        // Then - Verificar manejo de casos edge
        assertNotNull(result, "Quick stats should not be null even with empty library");
        assertEquals(0L, result.getBooksReading(), "Reading books should be 0");
        assertEquals(0L, result.getBooksFinished(), "Finished books should be 0");
        assertEquals(0L, result.getTotalBooks(), "Total books should be 0");
        assertEquals(0.0, result.getProgressPercentage(), "Progress percentage should be 0% with empty library");

        // Verificar que no hay errores de división por cero
        verify(bookRepository, times(2)).countByStatus(any(BookStatus.class));
        verify(bookRepository).count();
    }

    /**
     * TEST 4: Yearly Progress - Funcionalidad Básica
     *
     * Verifica que el análisis anual funciona correctamente
     */
    @Test
    void shouldReturnYearlyProgressWithoutErrors() {
        // Given - Mock para análisis anual
        when(bookRepository.countBooksFinishedInYear(anyInt())).thenReturn(5L);

        // When - Ejecutar método
        var result = analyticsService.getYearlyProgress();

        // Then - Verificar estructura básica
        assertNotNull(result, "Yearly progress should not be null");
        assertFalse(result.isEmpty(), "Yearly progress should contain data");

        // Verificar que contiene el año actual
        boolean hasCurrentYear = result.stream()
                .anyMatch(yearly -> yearly.isCurrentYear());
        assertTrue(hasCurrentYear, "Should include current year data");

        // Verificar que se llamó al repository
        verify(bookRepository, atLeast(1)).countBooksFinishedInYear(anyInt());
    }

    /**
     * TEST 5: Productivity Stats - Funcionalidad Básica
     *
     * Verifica que las estadísticas de productividad funcionan
     */
    @Test
    void shouldReturnProductivityStatsWithoutErrors() {
        // Given - Mock para estadísticas de productividad
        when(bookRepository.count()).thenReturn(15L);
        when(bookRepository.countByStatus(BookStatus.FINISHED)).thenReturn(10L);
        when(bookRepository.countByStatus(BookStatus.ABANDONED)).thenReturn(2L);
        when(bookRepository.countBooksFinishedInYear(anyInt())).thenReturn(8L);

        // When - Ejecutar método
        AnalyticsService.ProductivityStatsDTO result = analyticsService.getProductivityStats();

        // Then - Verificar estructura y datos básicos
        assertNotNull(result, "Productivity stats should not be null");
        assertTrue(result.getTotalBooks() >= 0, "Total books should be non-negative");
        assertTrue(result.getFinishedBooks() >= 0, "Finished books should be non-negative");
        assertTrue(result.getAbandonedBooks() >= 0, "Abandoned books should be non-negative");

        // Verificar que las tasas están en rango válido (0-100%)
        if (result.getCompletionRate() != null) {
            assertTrue(result.getCompletionRate() >= 0 && result.getCompletionRate() <= 100,
                    "Completion rate should be between 0-100%");
        }

        if (result.getAbandonmentRate() != null) {
            assertTrue(result.getAbandonmentRate() >= 0 && result.getAbandonmentRate() <= 100,
                    "Abandonment rate should be between 0-100%");
        }

        // Verificar llamadas básicas a repository
        verify(bookRepository, atLeast(1)).count();
        verify(bookRepository, atLeast(1)).countByStatus(any(BookStatus.class));
    }
}