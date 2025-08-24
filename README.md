# 📚 MyBookShelf Backend

> Sistema de gestión de biblioteca personal con Spring Boot + Integración Google Books API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Google Books API](https://img.shields.io/badge/Google%20Books%20API-Integrated-red.svg)](https://developers.google.com/books)
[![API Endpoints](https://img.shields.io/badge/API%20Endpoints-40%2B-blue.svg)](#api-endpoints)
[![Tests](https://img.shields.io/badge/Tests-Comprehensive-success.svg)](#testing)

## 🎯 Descripción

API REST para gestión de biblioteca personal con Spring Boot. Incluye gestión de libros, autores, géneros, análisis de lectura e integración con Google Books API para búsqueda e importación externa.

## ✨ Funcionalidades

### Core Features
- **Gestión de libros** - CRUD con estados (WISHLIST, READING, FINISHED, ON_HOLD, ABANDONED)
- **Autores y géneros** - Relaciones many-to-many
- **Analytics** - Estadísticas de lectura y progreso
- **Búsqueda** - Por título, autor y género con paginación
- **Seguimiento de progreso** - Actualización automática de estado
- **Sesiones de lectura** - Registro temporal de actividad

### Google Books Integration
- **Búsqueda externa** - Consulta libros en Google Books
- **Importación** - Añade libros con datos completos automáticamente
- **Enriquecimiento** - Actualiza libros existentes con metadatos
- **Búsqueda híbrida** - Combina resultados locales y externos
- **Autocompletado** - Sugerencias basadas en Google Books
- **Detección de duplicados** - Previene libros repetidos

### Seguridad y Calidad
- **Autenticación JWT** - Sistema de seguridad
- **Manejo de errores** - Respuestas JSON consistentes
- **Testing** - JUnit 5 + Mockito con alta cobertura

## 🛠️ Tecnologías

- **Framework:** Spring Boot 3.5.4
- **Lenguaje:** Java 21
- **Base de Datos:** H2 (desarrollo) / PostgreSQL (producción)
- **ORM:** Spring Data JPA
- **Integración Externa:** Google Books API
- **Cliente HTTP:** RestTemplate
- **Seguridad:** Spring Security + JWT
- **Testing:** JUnit 5 + Mockito
- **Build:** Maven

## 🚀 Setup e Instalación

### Prerrequisitos
- Java 21 o superior
- Maven 3.6+
- Google Books API Key (opcional)

### Configuración Google Books API (Opcional)
```properties
# application.properties
google.books.api.key=TU_API_KEY_AQUI
google.books.api.url=https://www.googleapis.com/books/v1/volumes
google.books.timeout=5000
google.books.max.results=10
```

### Ejecutar el proyecto
```bash
# Clonar repositorio
git clone https://github.com/DavidFernandezSuco/mybookshelf-backend.git
cd mybookshelf-backend

# Ejecutar aplicación
./mvnw spring-boot:run

# API disponible en http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### Verificar instalación
```bash
# Endpoint principal
curl http://localhost:8080/api/books

# Google Books integration
curl "http://localhost:8080/api/books/search-external?q=programming"

# Analytics
curl http://localhost:8080/api/analytics/dashboard
```

## 📖 API Endpoints

### BookController - 10 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/books` | GET | Listar libros (paginado) |
| `/api/books` | POST | Crear libro |
| `/api/books/{id}` | GET | Obtener libro por ID |
| `/api/books/{id}/progress` | PATCH | Actualizar progreso |
| `/api/books/{id}` | DELETE | Eliminar libro |
| `/api/books/search` | GET | Buscar libros locales |
| `/api/books/status/{status}` | GET | Filtrar por estado |
| `/api/books/search-external` | GET | Buscar en Google Books |
| `/api/books/import-google` | POST | Importar desde Google Books |
| `/api/books/{id}/enrich-google` | PATCH | Enriquecer con Google Books |

### Google Books Features
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/books/search-hybrid` | GET | Búsqueda local + externa |
| `/api/books/autocomplete` | GET | Autocompletado |
| `/api/books/suggestions` | GET | Sugerencias al crear |

### AuthorController - 8 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/authors` | GET | Listar autores (paginado) |
| `/api/authors` | POST | Crear autor |
| `/api/authors/{id}` | GET | Obtener autor por ID |
| `/api/authors/{id}` | PUT | Actualizar autor |
| `/api/authors/{id}` | DELETE | Eliminar autor |
| `/api/authors/search` | GET | Buscar autores |
| `/api/authors/autocomplete` | GET | Autocompletado UI |
| `/api/authors/statistics/{id}` | GET | Estadísticas del autor |

### GenreController - 10 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/genres` | GET | Listar géneros (paginado) |
| `/api/genres` | POST | Crear género |
| `/api/genres/{id}` | GET | Obtener género por ID |
| `/api/genres/{id}` | PUT | Actualizar género |
| `/api/genres/{id}` | DELETE | Eliminar género |
| `/api/genres/search` | GET | Buscar géneros |
| `/api/genres/autocomplete` | GET | Autocompletado UI |
| `/api/genres/ordered` | GET | Lista ordenada |
| `/api/genres/popular` | GET | Ranking popularidad |
| `/api/genres/stats` | GET | Estadísticas básicas |

### AnalyticsController - 5 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/analytics/dashboard` | GET | Estadísticas principales |
| `/api/analytics/quick` | GET | Métricas rápidas |
| `/api/analytics/yearly-progress` | GET | Progreso anual |
| `/api/analytics/monthly-progress` | GET | Progreso mensual |
| `/api/analytics/productivity` | GET | Estadísticas productividad |

### Ejemplos

#### Buscar en Google Books
```bash
GET /api/books/search-external?q=clean code
```

#### Importar libro
```json
POST /api/books/import-google
{
  "googleBooksId": "9aORjgEACAAJ",
  "status": "WISHLIST"
}
```

#### Búsqueda híbrida
```bash
GET /api/books/search-hybrid?q=programming&includeExternal=true
```

#### Crear libro tradicional
```json
POST /api/books
{
  "title": "Código Limpio",
  "totalPages": 464,
  "status": "WISHLIST"
}
```

#### Enriquecer libro existente
```json
PATCH /api/books/1/enrich-google
{
  "searchQuery": "Clean Code Robert Martin"
}
```

## 🏗️ Arquitectura

```
src/main/java/com/mybookshelf/mybookshelf_backend/
├── controller/     # REST Controllers
├── service/        # Lógica de negocio + GoogleBooksService
├── client/         # GoogleBooksClient (API externa)
├── repository/     # Acceso a datos JPA
├── model/          # Entidades (Book, Author, Genre, ReadingSession)
├── dto/            # Data Transfer Objects
├── mapper/         # Mappers DTO ↔ Entity
├── exception/      # Excepciones personalizadas
└── config/         # Configuración Spring
```

### Características técnicas
- Arquitectura en capas
- Patrón DTO para transferencia de datos
- Integración API externa con RestTemplate
- GlobalExceptionHandler centralizado
- Relaciones JPA many-to-many
- Cliente HTTP con timeouts y error handling
- Detección automática de duplicados
- Autenticación JWT

## 🧪 Testing

### Ejecutar tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest="BookServiceTest"
./mvnw test -Dtest="GoogleBooksClientTest"

# Coverage report
./mvnw test jacoco:report
```

### Tests implementados

#### BookServiceTest - Unit tests con Mockito
- Obtención por ID y manejo de excepciones
- Cambio automático de estado al completar
- Validaciones de progreso y páginas
- Búsqueda y filtrado
- Paginación

#### GoogleBooksClientTest - Integration tests
- Conexión con Google Books API
- Búsqueda por título, autor e ISBN
- Manejo de errores (timeout, respuesta inválida)
- Validación de parámetros

#### GoogleBooksServiceTest - Business logic tests
- Mapeo GoogleBookDTO → Book entity
- Detección de duplicados por ISBN/título
- Importación completa desde Google Books
- Enriquecimiento de libros existentes

#### AnalyticsServiceTest - Analytics tests
- Cálculo de estadísticas dashboard
- Porcentajes de progreso
- Manejo de biblioteca vacía
- Análisis temporal

### Tecnologías de testing
- JUnit 5
- Mockito para mocking
- @SpringBootTest para integration tests
- Given/When/Then pattern
- Error scenario testing

## 🌟 Lógica de Negocio

### Transiciones automáticas
- Estado auto-actualizado al completar libro (READING → FINISHED)
- Fechas automáticas (startDate, finishDate)
- Cálculo dinámico de porcentajes de progreso
- Validaciones (páginas no pueden exceder total)

### Sistema de Analytics
- Dashboard con métricas clave
- Análisis temporal por año/mes
- Estadísticas de productividad
- Quick stats para widgets
- Métricas por autor/género

## 🛡️ Manejo de Errores

GlobalExceptionHandler con respuestas JSON consistentes:
```json
{
  "error": "BOOK_NOT_FOUND",
  "message": "Book not found with id: 999",
  "timestamp": "2025-08-21T15:30:00"
}
```

Tipos de errores:
- Validation errors (400 BAD_REQUEST)
- Not found errors (404 NOT_FOUND)
- Business logic errors (409 CONFLICT)
- Authentication errors (401 UNAUTHORIZED)

## 🚀 Quick Demo

### Demo con Google Books
```bash
# 1. Buscar en Google Books
curl "http://localhost:8080/api/books/search-external?q=clean+code"

# 2. Importar libro
curl -X POST http://localhost:8080/api/books/import-google \
  -H "Content-Type: application/json" \
  -d '{"googleBooksId":"9aORjgEACAAJ","status":"READING"}'

# 3. Actualizar progreso
curl -X PATCH http://localhost:8080/api/books/1/progress \
  -H "Content-Type: application/json" \
  -d '{"currentPage":232}'

# 4. Búsqueda híbrida
curl "http://localhost:8080/api/books/search-hybrid?q=programming&includeExternal=true"

# 5. Ver analytics
curl http://localhost:8080/api/analytics/dashboard
```

## 👨‍💻 Autor

**David Fernández Suco**
- GitHub: [@DavidFernandezSuco](https://github.com/DavidFernandezSuco)
- LinkedIn: [david-fernandez-suco](https://linkedin.com/in/david-fernandez-suco)
- Email: dfsuco@gmail.com

---

*API REST con integración Google Books desarrollada con Spring Boot*