package com.mybookshelf.mybookshelf_backend.service;

// IMPORTS: Librerías necesarias + la nueva excepción
import com.mybookshelf.mybookshelf_backend.model.*;
import com.mybookshelf.mybookshelf_backend.repository.*;
import com.mybookshelf.mybookshelf_backend.exception.BookNotFoundException; // ← NUEVO IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CLASE BookService - "Cerebro del Sistema de Libros"
 *
 * ACTUALIZADO: Usa BookNotFoundException en lugar de RuntimeException genérica
 */
@Service
@Transactional
public class BookService {

    // ========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ========================================

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final ReadingSessionRepository readingSessionRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       GenreRepository genreRepository,
                       ReadingSessionRepository readingSessionRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.readingSessionRepository = readingSessionRepository;
    }

    // ========================================
    // OPERACIONES CRUD CON EXCEPCIONES MEJORADAS
    // ========================================

    /**
     * OBTENER TODOS LOS LIBROS (CON PAGINACIÓN)
     */
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * OBTENER LIBRO POR ID
     *
     * ✅ CORREGIDO: Usa BookNotFoundException
     */
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    /**
     * CREAR NUEVO LIBRO
     */
    public Book createBook(Book book, Set<Long> authorIds, Set<Long> genreIds) {
        // Validación ISBN único
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
            if (existingBook.isPresent()) {
                throw new RuntimeException("Book with ISBN " + book.getIsbn() + " already exists");
            }
        }

        // Validación título obligatorio
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Book title is required");
        }

        // Valores por defecto
        if (book.getStatus() == null) {
            book.setStatus(BookStatus.WISHLIST);
        }
        if (book.getCurrentPage() == null) {
            book.setCurrentPage(0);
        }

        // Asociar autores
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<Author> authors = Set.copyOf(authorRepository.findAllById(authorIds));
            if (authors.size() != authorIds.size()) {
                throw new RuntimeException("Some authors not found");
            }
            book.setAuthors(authors);
            authors.forEach(author -> author.getBooks().add(book));
        }

        // Asociar géneros
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = Set.copyOf(genreRepository.findAllById(genreIds));
            if (genres.size() != genreIds.size()) {
                throw new RuntimeException("Some genres not found");
            }
            book.setGenres(genres);
            genres.forEach(genre -> genre.getBooks().add(book));
        }

        return bookRepository.save(book);
    }

    /**
     * ACTUALIZAR PROGRESO DE LECTURA
     *
     * ✅ CORREGIDO: Usa BookNotFoundException
     */
    public Book updateBookProgress(Long bookId, Integer newCurrentPage) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        // Validaciones de progreso
        if (newCurrentPage < 0) {
            throw new RuntimeException("Current page cannot be negative");
        }
        if (book.getTotalPages() != null && newCurrentPage > book.getTotalPages()) {
            throw new RuntimeException("Current page cannot exceed total pages (" + book.getTotalPages() + ")");
        }

        // Actualizar progreso
        book.setCurrentPage(newCurrentPage);

        // Lógica de auto-cambio de estado
        if (book.getStatus() == BookStatus.WISHLIST && newCurrentPage > 0) {
            book.setStatus(BookStatus.READING);
            book.setStartDate(LocalDate.now());
        }

        // Si terminó el libro
        if (book.getTotalPages() != null && newCurrentPage.equals(book.getTotalPages())) {
            book.setStatus(BookStatus.FINISHED);
            book.setFinishDate(LocalDate.now());
        }

        return bookRepository.save(book);
    }

    /**
     * BUSCAR LIBROS POR TÍTULO
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.searchBooks(searchTerm.trim());
    }

    /**
     * OBTENER LIBROS POR STATUS
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksByStatus(BookStatus status) {
        return bookRepository.findByStatus(status);
    }

    /**
     * ELIMINAR LIBRO
     *
     * ✅ CORREGIDO: Usa BookNotFoundException
     */
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        // Verificar sesiones de lectura
        List<ReadingSession> sessions = readingSessionRepository.findByBookIdOrderByStartTimeDesc(bookId);
        if (!sessions.isEmpty()) {
            throw new RuntimeException("Cannot delete book with existing reading sessions. " +
                    "Delete sessions first or keep the book.");
        }

        // Limpiar relaciones bidireccionales
        book.getAuthors().forEach(author -> author.getBooks().remove(book));
        book.getGenres().forEach(genre -> genre.getBooks().remove(book));

        bookRepository.delete(book);
    }

    /**
     * DUPLICAR LIBRO
     *
     * ✅ CORREGIDO: Usa BookNotFoundException
     */
    public Book duplicateBook(Long bookId) {
        Book original = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        Book duplicate = new Book();
        duplicate.setTitle(original.getTitle() + " (Copy)");
        duplicate.setIsbn(null); // No duplicar ISBN
        duplicate.setTotalPages(original.getTotalPages());
        duplicate.setPublishedDate(original.getPublishedDate());
        duplicate.setPublisher(original.getPublisher());
        duplicate.setDescription(original.getDescription());

        // Copiar relaciones
        duplicate.setAuthors(Set.copyOf(original.getAuthors()));
        duplicate.setGenres(Set.copyOf(original.getGenres()));

        // Estado inicial
        duplicate.setStatus(BookStatus.WISHLIST);
        duplicate.setCurrentPage(0);

        return bookRepository.save(duplicate);
    }

    /**
     * VERIFICAR SI EXISTE LIBRO POR ISBN
     */
    @Transactional(readOnly = true)
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return bookRepository.findByIsbn(isbn).isPresent();
    }

    /**
     * OBTENER LIBROS ACTUALMENTE LEYENDO
     *
     * Método de conveniencia para BookController
     */
    @Transactional(readOnly = true)
    public List<Book> getCurrentlyReadingBooks(BookStatus reading) {
        return bookRepository.findByStatus(BookStatus.READING);
    }

    /**
     * ACTUALIZAR LIBRO COMPLETO
     *
     * Siguiendo patrón de updateBookProgress() - actualiza campos de un libro existente.
     * Útil para GoogleBooksService.enrichExistingBook() y futuros endpoints PUT.
     *
     * @param bookId ID del libro a actualizar
     * @param updatedBook Book con los nuevos datos (campos null se ignoran)
     * @return Book actualizado y guardado
     */
    public Book updateBook(Long bookId, Book updatedBook) {
        // 1. Obtener libro existente (lanza excepción si no existe)
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        // 2. Actualizar solo campos que no son null (patrón que ya usas)

        // INFORMACIÓN BÁSICA
        if (updatedBook.getTitle() != null && !updatedBook.getTitle().trim().isEmpty()) {
            existingBook.setTitle(updatedBook.getTitle().trim());
        }

        if (updatedBook.getDescription() != null) {
            // Permitir vaciar descripción si se envía string vacío
            existingBook.setDescription(updatedBook.getDescription().trim().isEmpty() ? null : updatedBook.getDescription().trim());
        }

        // ISBN - Validar unicidad como en createBook()
        if (updatedBook.getIsbn() != null) {
            String newIsbn = updatedBook.getIsbn().trim();
            if (!newIsbn.isEmpty()) {
                // Verificar que no existe en otro libro
                Optional<Book> bookWithIsbn = bookRepository.findByIsbn(newIsbn);
                if (bookWithIsbn.isPresent() && !bookWithIsbn.get().getId().equals(bookId)) {
                    throw new RuntimeException("Book with ISBN " + newIsbn + " already exists");
                }
                existingBook.setIsbn(newIsbn);
            } else {
                existingBook.setIsbn(null); // Permitir vaciar ISBN
            }
        }

        // INFORMACIÓN DE PUBLICACIÓN
        if (updatedBook.getPublisher() != null) {
            existingBook.setPublisher(updatedBook.getPublisher().trim().isEmpty() ? null : updatedBook.getPublisher().trim());
        }

        if (updatedBook.getPublishedDate() != null) {
            existingBook.setPublishedDate(updatedBook.getPublishedDate());
        }

        // PÁGINAS - Validar lógica como en updateBookProgress()
        if (updatedBook.getTotalPages() != null) {
            if (updatedBook.getTotalPages() < 0) {
                throw new RuntimeException("Total pages cannot be negative");
            }

            // Si ya tiene progreso, verificar que no sea menor que página actual
            if (existingBook.getCurrentPage() != null && existingBook.getCurrentPage() > updatedBook.getTotalPages()) {
                throw new RuntimeException("Total pages (" + updatedBook.getTotalPages() +
                        ") cannot be less than current page (" + existingBook.getCurrentPage() + ")");
            }

            existingBook.setTotalPages(updatedBook.getTotalPages());
        }

        // PROGRESO - Solo actualizar si se proporciona
        if (updatedBook.getCurrentPage() != null) {
            // Usar la lógica existente de updateBookProgress para consistencia
            return updateBookProgress(bookId, updatedBook.getCurrentPage());
        }

        // STATUS - Solo actualizar si se proporciona y es diferente
        if (updatedBook.getStatus() != null && !updatedBook.getStatus().equals(existingBook.getStatus())) {
            // Lógica de transición de estados (similar a updateBookProgress)
            BookStatus oldStatus = existingBook.getStatus();
            BookStatus newStatus = updatedBook.getStatus();

            existingBook.setStatus(newStatus);

            // Auto-establecer fechas según transiciones
            if (oldStatus == BookStatus.WISHLIST && newStatus == BookStatus.READING) {
                existingBook.setStartDate(LocalDate.now());
            }

            if (newStatus == BookStatus.FINISHED && existingBook.getFinishDate() == null) {
                existingBook.setFinishDate(LocalDate.now());
            }

            if (newStatus != BookStatus.FINISHED) {
                existingBook.setFinishDate(null); // Limpiar si ya no está terminado
            }
        }

        // NOTA: No actualizamos relaciones (authors/genres) aquí
        // Eso se manejaría en métodos específicos como updateBookAuthors(), etc.

        // 3. Guardar cambios (igual que en tus otros métodos)
        return bookRepository.save(existingBook);
    }
}