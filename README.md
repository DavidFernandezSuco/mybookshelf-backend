# 📚 MyBookShelf Backend

> Sistema de gestión de biblioteca personal con Spring Boot

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![API Endpoints](https://img.shields.io/badge/API%20Endpoints-30%2B-blue.svg)](#api-endpoints)
[![Tests](https://img.shields.io/badge/Tests-Comprehensive-success.svg)](#testing)

## 🎯 Descripción

**API REST completa** para gestión de biblioteca personal que demuestra capacidades backend avanzadas con Spring Boot. Permite gestionar libros, autores, géneros y análisis de lectura con lógica de negocio inteligente y **30+ endpoints funcionales**.

## ✨ Funcionalidades

- **📖 Gestión completa de libros** - CRUD con estados automáticos (WISHLIST → READING → FINISHED)
- **👥 Autores y géneros** - Relaciones many-to-many con autocompletado
- **📊 Dashboard analytics** - Estadísticas de lectura, progreso anual, tasas de completación
- **🔍 Búsqueda avanzada** - Por título, autor y género con paginación
- **📈 Seguimiento de progreso** - Actualización automática de estado al terminar libros
- **⏱️ Sesiones de lectura** - Análisis temporal detallado
- **🔐 Autenticación JWT** - Sistema de seguridad moderno
- **🛡️ Manejo de errores** - Respuestas JSON profesionales

## 🛠️ Tecnologías

- **Framework:** Spring Boot 3.2.x
- **Lenguaje:** Java 21
- **Base de Datos:** H2 (desarrollo) / PostgreSQL (producción)
- **ORM:** Spring Data JPA
- **Seguridad:** Spring Security + JWT
- **Testing:** JUnit 5 + Mockito
- **Build:** Maven

## 🚀 Setup e Instalación

### Prerrequisitos
- Java 21 o superior
- Maven 3.6+

### Ejecutar el proyecto
```bash
# Clonar repositorio
git clone https://github.com/DavidFernandezSuco/mybookshelf-backend.git
cd mybookshelf-backend

# Ejecutar aplicación
./mvnw spring-boot:run

# API disponible en http://localhost:8080
```

### Verificar instalación
```bash
# Probar endpoint principal
curl http://localhost:8080/api/books

# Obtener analytics
curl http://localhost:8080/api/analytics/dashboard
```

## 📖 API Endpoints

### 🔹 **30+ Endpoints Funcionales**

#### **BookController** - 7 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/books` | GET | Obtener libros (paginado) |
| `/api/books` | POST | Crear libro |
| `/api/books/{id}` | GET | Obtener libro por ID |
| `/api/books/{id}/progress` | PATCH | Actualizar progreso |
| `/api/books/{id}` | DELETE | Eliminar libro |
| `/api/books/search` | GET | Buscar libros |
| `/api/books/status/{status}` | GET | Filtrar por estado |

#### **AuthorController** - 8 endpoints
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

#### **GenreController** - 10 endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/genres` | GET | Listar géneros (paginado) |
| `/api/genres` | POST | Crear género |
| `/api/genres/{id}` | GET | Obtener género por ID |
| `/api/genres/{id}` | PUT | Actualizar género |
| `/api/genres/{id}` | DELETE | Eliminar género |
| `/api/genres/search` | GET | Buscar géneros |
| `/api/genres/autocomplete` | GET | Autocompletado UI |
| `/api/genres/ordered` | GET | Lista ordenada alfabéticamente |
| `/api/genres/popular` | GET | Ranking de popularidad |
| `/api/genres/stats` | GET | Estadísticas básicas |

#### **AnalyticsController** - 5+ endpoints
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/analytics/dashboard` | GET | Estadísticas principales |
| `/api/analytics/quick` | GET | Métricas rápidas |
| `/api/analytics/yearly-progress` | GET | Progreso anual |
| `/api/analytics/monthly-progress` | GET | Progreso mensual |
| `/api/analytics/productivity` | GET | Estadísticas de productividad |

### Ejemplos con Postman

#### Crear libro
```json
POST /api/books
{
  "title": "Código Limpio",
  "totalPages": 464,
  "status": "WISHLIST",
  "description": "Manual de desarrollo de software artesanal"
}
```

#### Actualizar progreso
```json
PATCH /api/books/1/progress
{
  "currentPage": 200
}
```

#### Respuesta Analytics
```json
GET /api/analytics/dashboard
{
  "totalBooks": 25,
  "booksReading": 3,
  "booksFinished": 18,
  "booksWishlist": 4,
  "completionRate": 72.0,
  "averagePages": 342.5
}
```

## 🏗️ Arquitectura

```
src/main/java/com/mybookshelf/mybookshelf_backend/
├── controller/     # Controladores REST (30+ endpoints)
├── service/        # Lógica de negocio
├── repository/     # Acceso a datos JPA
├── model/          # Entidades (Book, Author, Genre, ReadingSession)
├── dto/            # Objetos de transferencia
├── mapper/         # Mappers DTO ↔ Entity
├── exception/      # Excepciones personalizadas
└── config/         # Configuración Spring Security
```

### Características técnicas
- **Arquitectura en capas** con separación de responsabilidades
- **Patrón DTO** para transferencia de datos segura
- **GlobalExceptionHandler** para manejo centralizado de errores
- **Relaciones JPA** many-to-many bidireccionales optimizadas
- **Lógica de negocio inteligente** con transiciones automáticas
- **Security JWT** con autenticación moderna

## 🧪 Testing

### **Testing Comprehensivo** con JUnit 5 + Mockito

#### Ejecutar tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest="BookServiceTest"

# Coverage report
./mvnw test jacoco:report
```

#### **BookServiceTest** - Testing completo con Mockito
- ✅ Obtención exitosa por ID
- ✅ BookNotFoundException para IDs inexistentes
- ✅ Cambio automático de estado al completar libro
- ✅ Validación de páginas máximas
- ✅ Búsqueda y filtrado
- ✅ Paginación de resultados

#### **AnalyticsServiceTest** - Testing de métricas
- ✅ Cálculo de estadísticas dashboard
- ✅ Porcentajes de progreso
- ✅ Manejo de biblioteca vacía
- ✅ Análisis temporal

#### **Integration Tests** - Testing E2E
- ✅ Controllers con @WebMvcTest
- ✅ Repository con @DataJpaTest
- ✅ Security configuration
- ✅ API endpoints completos

### Tecnologías de testing
- **JUnit 5** para estructura de tests
- **Mockito** para mocking de dependencies
- **@ExtendWith(MockitoExtension.class)** para inyección limpia
- **Given/When/Then** pattern para tests legibles

## 🌟 Lógica de Negocio

### Transiciones automáticas inteligentes
- **Estado auto-actualizado** al completar libro (READING → FINISHED)
- **Fechas automáticas** (startDate, finishDate) con lógica temporal
- **Cálculo dinámico** de porcentajes de progreso
- **Validaciones de negocio** (páginas no pueden exceder total)

### Sistema de Analytics completo
- **Dashboard principal** con métricas clave en tiempo real
- **Análisis temporal** por año/mes con trends
- **Estadísticas de productividad** (tasas completación/abandono)
- **Quick stats** para widgets de UI
- **Métricas por autor/género** para insights detallados

## 🛡️ Manejo de Errores

**GlobalExceptionHandler** con respuestas JSON consistentes:
```json
{
  "error": "BOOK_NOT_FOUND",
  "message": "Book not found with id: 999",
  "timestamp": "2025-08-21T15:30:00"
}
```

#### Tipos de errores manejados:
- ✅ **Validation errors** (400 BAD_REQUEST)
- ✅ **Not found errors** (404 NOT_FOUND)
- ✅ **Business logic errors** (409 CONFLICT)
- ✅ **Authentication errors** (401 UNAUTHORIZED)

## 🎯 Valor Técnico del Proyecto

Este proyecto demuestra:

### **🔥 Skills Backend Avanzados**
- **30+ REST endpoints** funcionales con documentación Swagger
- **Arquitectura escalable** siguiendo principios SOLID
- **Testing comprehensivo** con alta cobertura
- **Security moderna** con JWT authentication
- **Performance optimizada** con paginación y queries eficientes

### **💼 Ready for Production**
- **Error handling profesional** con responses consistentes
- **Validaciones robustas** en todas las capas
- **Database design** optimizado con índices y relaciones
- **Docker ready** con profiles de desarrollo/producción
- **CI/CD friendly** con Maven y testing automatizado

### **📈 Escalabilidad**
- **Microservices ready** con controllers independientes
- **Event-driven architecture** preparada para extensiones
- **Analytics engine** base para machine learning
- **API versioning** structure implementada

## 🚀 Quick Demo

```bash
# 1. Crear libro
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","totalPages":464,"status":"READING"}'

# 2. Actualizar progreso
curl -X PATCH http://localhost:8080/api/books/1/progress \
  -H "Content-Type: application/json" \
  -d '{"currentPage":232}'

# 3. Ver analytics
curl http://localhost:8080/api/analytics/dashboard

# 4. Buscar libros
curl "http://localhost:8080/api/books/search?q=Clean"
```

## 👨‍💻 Autor

**David Fernández Suco**
- GitHub: [@DavidFernandezSuco](https://github.com/DavidFernandezSuco)
- LinkedIn: [david-fernandez-suco](https://linkedin.com/in/david-fernandez-suco)
- Email: dfsuco@gmail.com

---

*Proyecto desarrollado como demostración de capacidades **backend avanzadas** con Spring Boot*