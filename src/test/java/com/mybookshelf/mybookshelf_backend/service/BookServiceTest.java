package com.mybookshelf.mybookshelf_backend.service;

import com.mybookshelf.mybookshelf_backend.exception.BookNotFoundException;
import com.mybookshelf.mybookshelf_backend.model.Book;
import com.mybookshelf.mybookshelf_backend.model.BookStatus;
import com.mybookshelf.mybookshelf_backend.model.Author;
import com.mybookshelf.mybookshelf_backend.model.Genre;
import com.mybookshelf.mybookshelf_backend.repository.BookRepository;
import com.mybookshelf.mybookshelf_backend.repository.AuthorRepository;
import com.mybookshelf.mybookshelf_backend.repository.GenreRepository;
import com.mybookshelf.mybookshelf_backend.repository.ReadingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS para BookService
 *
 * Portfolio Demo: Testing de lógica de negocio con Mockito
 * Cubre: CRUD operations, business logic, exception handling
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @InjectMocks
    private BookService bookService;

    /**
     * TEST 1: Happy Path - Obtener libro por ID exitosamente
     */
    @Test
    void shouldGetBookByIdSuccessfully() {
        // Given - Configurar datos de prueba
        Long bookId = 1L;
        Book expectedBook = new Book();
        expectedBook.setId(bookId);
        expectedBook.setTitle("Clean Code");
        expectedBook.setStatus(BookStatus.READING);
        expectedBook.setTotalPages(464);
        expectedBook.setCurrentPage(150);

        // Mock del repository
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(expectedBook));

        // When - Ejecutar método bajo prueba
        Book result = bookService.getBookById(bookId);

        // Then - Verificar resultados
        assertNotNull(result, "Book should not be null");
        assertEquals(bookId, result.getId(), "Book ID should match");
        assertEquals("Clean Code", result.getTitle(), "Book title should match");
        assertEquals(BookStatus.READING, result.getStatus(), "Book status should match");
        assertEquals(464, result.getTotalPages(), "Total pages should match");
        assertEquals(150, result.getCurrentPage(), "Current page should match");

        // Verificar que se llamó al repository correctamente
        verify(bookRepository).findById(bookId);
    }

    /**
     * TEST 2: Exception Path - Libro no encontrado lanza BookNotFoundException
     */
    @Test
    void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
        // Given - ID que no existe en la base de datos
        Long nonExistentId = 999L;
        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then - Verificar que lanza la excepción correcta
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.getBookById(nonExistentId),
                "Should throw BookNotFoundException for non-existent book"
        );

        // Verificar mensaje de error específico
        assertEquals("Book not found with id: 999", exception.getMessage(),
                "Exception message should be descriptive");

        // Verificar que se llamó al repository
        verify(bookRepository).findById(nonExistentId);
    }

    /**
     * TEST 3: Business Logic - Actualización de progreso con auto-cambio de estado
     */
    @Test
    void shouldUpdateBookProgressAndChangeStatusToFinished() {
        // Given - Libro que se va a terminar
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Test Book");
        existingBook.setStatus(BookStatus.READING);
        existingBook.setTotalPages(300);
        existingBook.setCurrentPage(250);

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("Test Book");
        updatedBook.setStatus(BookStatus.FINISHED); // Estado cambiado automáticamente
        updatedBook.setTotalPages(300);
        updatedBook.setCurrentPage(300); // Terminado
        updatedBook.setFinishDate(LocalDate.now());

        // Mocks
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        // When - Actualizar progreso hasta el final
        Book result = bookService.updateBookProgress(bookId, 300);

        // Then - Verificar que se actualizó correctamente
        assertNotNull(result, "Updated book should not be null");
        assertEquals(300, result.getCurrentPage(), "Current page should be updated");
        assertEquals(BookStatus.FINISHED, result.getStatus(),
                "Status should auto-change to FINISHED when book is completed");
        assertNotNull(result.getFinishDate(), "Finish date should be set");

        // Verificar llamadas a repository
        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(any(Book.class));
    }

    /**
     * TEST 4: Validation Logic - Error al exceder páginas totales
     */
    @Test
    void shouldThrowExceptionWhenCurrentPageExceedsTotalPages() {
        // Given - Libro con 300 páginas totales
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Test Book");
        existingBook.setTotalPages(300);
        existingBook.setCurrentPage(200);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // When & Then - Intentar establecer página actual mayor que total
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.updateBookProgress(bookId, 350),
                "Should throw exception when current page exceeds total pages"
        );

        // Verificar mensaje descriptivo
        assertTrue(exception.getMessage().contains("cannot exceed total pages"),
                "Exception message should mention page limit");

        // Verificar que se consultó el libro pero no se guardó
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    /**
     * TEST 5: Collection Operations - Obtener todos los libros con paginación
     */
    @Test
    void shouldGetAllBooksWithPagination() {
        // Given - Lista de libros mock
        List<Book> bookList = new ArrayList<>();

        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book 1");
        book1.setStatus(BookStatus.READING);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book 2");
        book2.setStatus(BookStatus.FINISHED);

        bookList.add(book1);
        bookList.add(book2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(bookList, pageable, bookList.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        // When - Obtener libros paginados
        Page<Book> result = bookService.getAllBooks(pageable);

        // Then - Verificar resultados de paginación
        assertNotNull(result, "Result page should not be null");
        assertEquals(2, result.getContent().size(), "Should return 2 books");
        assertEquals(2, result.getTotalElements(), "Total elements should be 2");
        assertEquals("Book 1", result.getContent().get(0).getTitle(), "First book title should match");
        assertEquals("Book 2", result.getContent().get(1).getTitle(), "Second book title should match");

        // Verificar llamada al repository
        verify(bookRepository).findAll(pageable);
    }

    /**
     * TEST 6: Search Functionality - Búsqueda de libros por término
     */
    @Test
    void shouldSearchBooksSuccessfully() {
        // Given - Término de búsqueda y resultados esperados
        String searchTerm = "Clean";
        List<Book> searchResults = new ArrayList<>();

        Book matchingBook = new Book();
        matchingBook.setId(1L);
        matchingBook.setTitle("Clean Code");
        matchingBook.setStatus(BookStatus.READING);

        searchResults.add(matchingBook);

        when(bookRepository.searchBooks(searchTerm)).thenReturn(searchResults);

        // When - Buscar libros
        List<Book> result = bookService.searchBooks(searchTerm);

        // Then - Verificar resultados de búsqueda
        assertNotNull(result, "Search results should not be null");
        assertEquals(1, result.size(), "Should find 1 matching book");
        assertEquals("Clean Code", result.get(0).getTitle(), "Found book title should match search");

        // Verificar llamada al repository con término correcto
        verify(bookRepository).searchBooks(searchTerm);
    }

    /**
     * TEST 7: Edge Case - Búsqueda con término vacío
     */
    @Test
    void shouldReturnEmptyListForEmptySearchTerm() {
        // When - Buscar con término vacío
        List<Book> result = bookService.searchBooks("");

        // Then - Verificar que retorna lista vacía
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list for empty search term");

        // Verificar que NO se llama al repository
        verify(bookRepository, never()).searchBooks(anyString());
    }

    /**
     * TEST 8: Business Logic - Obtener libros por estado
     */
    @Test
    void shouldGetBooksByStatus() {
        // Given - Libros con estado específico
        BookStatus targetStatus = BookStatus.WISHLIST;
        List<Book> wishlistBooks = new ArrayList<>();

        Book wishlistBook1 = new Book();
        wishlistBook1.setId(1L);
        wishlistBook1.setTitle("Future Read 1");
        wishlistBook1.setStatus(BookStatus.WISHLIST);

        Book wishlistBook2 = new Book();
        wishlistBook2.setId(2L);
        wishlistBook2.setTitle("Future Read 2");
        wishlistBook2.setStatus(BookStatus.WISHLIST);

        wishlistBooks.add(wishlistBook1);
        wishlistBooks.add(wishlistBook2);

        when(bookRepository.findByStatus(targetStatus)).thenReturn(wishlistBooks);

        // When - Obtener libros por estado
        List<Book> result = bookService.getBooksByStatus(targetStatus);

        // Then - Verificar filtrado por estado
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 wishlist books");
        assertTrue(result.stream().allMatch(book -> book.getStatus() == BookStatus.WISHLIST),
                "All returned books should have WISHLIST status");

        // Verificar llamada al repository
        verify(bookRepository).findByStatus(targetStatus);
    }

    /**
     * TEST: Happy Path - Actualizar libro exitosamente
     */
    @Test
    void shouldUpdateBookSuccessfully() {
        // Given - Libro existente y datos actualizados
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Old Title");
        existingBook.setDescription("Old Description");
        existingBook.setIsbn("1234567890");
        existingBook.setTotalPages(300);

        Book updatedBook = new Book();
        updatedBook.setTitle("New Title");
        updatedBook.setDescription("New Description");
        updatedBook.setIsbn("0987654321");
        // NO incluir currentPage para evitar delegación a updateBookProgress

        Book savedBook = new Book();
        savedBook.setId(bookId);
        savedBook.setTitle("New Title");
        savedBook.setDescription("New Description");
        savedBook.setIsbn("0987654321");
        savedBook.setTotalPages(300);

        // Mocks
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("0987654321")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // When - Actualizar libro
        Book result = bookService.updateBook(bookId, updatedBook);

        // Then - Verificar actualización
        assertNotNull(result, "Updated book should not be null");
        assertEquals("New Title", result.getTitle(), "Title should be updated");
        assertEquals("New Description", result.getDescription(), "Description should be updated");
        assertEquals("0987654321", result.getIsbn(), "ISBN should be updated");
        assertEquals(300, result.getTotalPages(), "Total pages should be preserved");

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(any(Book.class));
    }

    /**
     * TEST: Exception Path - Libro no encontrado
     */
    @Test
    void shouldThrowBookNotFoundExceptionWhenUpdatingNonExistentBook() {
        // Given - ID que no existe
        Long nonExistentId = 999L;
        Book updatedBook = new Book();
        updatedBook.setTitle("New Title");

        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then - Verificar excepción
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.updateBook(nonExistentId, updatedBook),
                "Should throw BookNotFoundException for non-existent book"
        );

        assertEquals("Book not found with id: 999", exception.getMessage(),
                "Exception message should be descriptive");

        verify(bookRepository).findById(nonExistentId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    /**
     * TEST: Validation Logic - ISBN duplicado
     */
    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateIsbn() {
        // Given - Libro existente y otro libro con ISBN que queremos usar
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setIsbn("1234567890");

        Book otherBook = new Book();
        otherBook.setId(2L);
        otherBook.setIsbn("0987654321");

        Book updatedBook = new Book();
        updatedBook.setIsbn("0987654321"); // ISBN que ya existe

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("0987654321")).thenReturn(Optional.of(otherBook));

        // When & Then - Verificar excepción
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.updateBook(bookId, updatedBook),
                "Should throw exception for duplicate ISBN"
        );

        assertTrue(exception.getMessage().contains("already exists"),
                "Exception message should mention duplicate ISBN");

        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    /**
     * TEST: Logic - Campos null se ignoran
     */
    @Test
    void shouldIgnoreNullFieldsWhenUpdating() {
        // Given - Libro existente con datos completos
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Original Title");
        existingBook.setDescription("Original Description");
        existingBook.setIsbn("1234567890");
        existingBook.setTotalPages(300);

        // Updated book solo tiene título (otros campos null)
        Book updatedBook = new Book();
        updatedBook.setTitle("Updated Title");
        // description = null, isbn = null, totalPages = null, currentPage = null (importante)

        Book savedBook = new Book();
        savedBook.setId(bookId);
        savedBook.setTitle("Updated Title");
        savedBook.setDescription("Original Description");
        savedBook.setIsbn("1234567890");
        savedBook.setTotalPages(300);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // When - Actualizar solo título
        Book result = bookService.updateBook(bookId, updatedBook);

        // Then - Solo título cambia, otros campos preservados
        assertNotNull(result, "Updated book should not be null");
        assertEquals("Updated Title", result.getTitle(), "Title should be updated");
        assertEquals("Original Description", result.getDescription(), "Description should be preserved");
        assertEquals("1234567890", result.getIsbn(), "ISBN should be preserved");
        assertEquals(300, result.getTotalPages(), "Total pages should be preserved");

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(any(Book.class));
    }

    /**
     * TEST: Integration - Delega a updateBookProgress cuando se actualiza currentPage
     */
    @Test
    void shouldDelegateToUpdateProgressWhenCurrentPageProvided() {
        // Given - Libro existente
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Test Book");
        existingBook.setStatus(BookStatus.READING);
        existingBook.setTotalPages(300);
        existingBook.setCurrentPage(100);

        Book updatedBook = new Book();
        updatedBook.setCurrentPage(200); // Solo actualizar progreso

        Book progressUpdatedBook = new Book();
        progressUpdatedBook.setId(bookId);
        progressUpdatedBook.setTitle("Test Book");
        progressUpdatedBook.setStatus(BookStatus.READING);
        progressUpdatedBook.setTotalPages(300);
        progressUpdatedBook.setCurrentPage(200);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(progressUpdatedBook);

        // When - Actualizar con currentPage
        Book result = bookService.updateBook(bookId, updatedBook);

        // Then - Debe usar lógica de updateBookProgress
        assertNotNull(result, "Updated book should not be null");
        assertEquals(200, result.getCurrentPage(), "Current page should be updated");

        // Verificar que se llamó findById dos veces (una en updateBook, otra en updateBookProgress)
        verify(bookRepository, times(2)).findById(bookId);
        verify(bookRepository).save(any(Book.class));
    }


}