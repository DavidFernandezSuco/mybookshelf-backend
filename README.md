# 📚 MyBookShelf Backend

> Sistema de gestión de biblioteca personal con Spring Boot

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/Tests-13%20Passing-success.svg)](#testing)

## 🎯 Descripción

API REST completa para gestión de biblioteca personal que demuestra capacidades backend avanzadas con Spring Boot. Permite gestionar libros, autores, géneros y análisis de lectura con lógica de negocio inteligente.

## ✨ Funcionalidades

- **📖 Gestión de libros** - CRUD completo con estados automáticos (WISHLIST → READING → FINISHED)
- **👥 Autores y géneros** - Relaciones many-to-many
- **📊 Dashboard analytics** - Estadísticas de lectura, progreso anual, tasas de completación
- **🔍 Búsqueda avanzada** - Por título, autor y género con paginación
- **📈 Seguimiento de progreso** - Actualización automática de estado al terminar libros
- **⏱️ Sesiones de lectura** - Análisis temporal detallado
- **🛡️ Manejo de errores** - Respuestas JSON profesionales

## 🛠️ Tecnologías

- **Framework:** Spring Boot 3.5.4
- **Lenguaje:** Java 21
- **Base de Datos:** H2 (desarrollo) / PostgreSQL (producción)
- **ORM:** Spring Data JPA
- **Seguridad:** Spring Security
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

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/books` | GET | Obtener libros (paginado) |
| `/api/books` | POST | Crear libro |
| `/api/books/{id}` | GET | Obtener libro por ID |
| `/api/books/{id}/progress` | PATCH | Actualizar progreso |
| `/api/analytics/dashboard` | GET | Estadísticas principales |
| `/api/analytics/yearly-progress` | GET | Progreso anual |
| `/api/authors` | GET, POST | Gestión de autores |
| `/api/genres` | GET, POST | Gestión de géneros |

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
├── controller/     # Controladores REST
├── service/        # Lógica de negocio
├── repository/     # Acceso a datos JPA
├── model/          # Entidades (Book, Author, Genre)
├── dto/            # Objetos de transferencia
├── exception/      # Excepciones personalizadas
└── config/         # Configuración Spring
```

### Características técnicas
- **Arquitectura en capas** con separación de responsabilidades
- **Patrón DTO** para transferencia de datos
- **GlobalExceptionHandler** para manejo centralizado de errores
- **Relaciones JPA** many-to-many bidireccionales
- **Lógica de negocio inteligente** con transiciones automáticas

## 🧪 Testing

### Suite completa: 13 unit tests

#### Ejecutar tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest="BookServiceTest"
```

#### BookServiceTest (8 tests) con Mockito
- ✅ Obtención exitosa por ID
- ✅ BookNotFoundException para IDs inexistentes
- ✅ Cambio automático de estado al completar libro
- ✅ Validación de páginas máximas
- ✅ Búsqueda y filtrado
- ✅ Paginación de resultados

#### AnalyticsServiceTest (5 tests)
- ✅ Cálculo de estadísticas dashboard
- ✅ Porcentajes de progreso
- ✅ Manejo de biblioteca vacía
- ✅ Análisis temporal

### Tecnologías de testing
- **JUnit 5** para estructura
- **Mockito** para mocking de repositories
- **@ExtendWith(MockitoExtension.class)** para inyección
- **Given/When/Then** pattern para claridad

### Testing con Postman
Collection completa disponible con:
- Casos de éxito y error
- Variables de entorno
- Tests automatizados de respuesta
- Ejemplos de todos los endpoints

## 🌟 Lógica de Negocio

### Transiciones automáticas
- **Estado auto-actualizado** al completar libro (READING → FINISHED)
- **Fechas automáticas** (startDate, finishDate)
- **Cálculo dinámico** de porcentajes de progreso

### Sistema de Analytics
- **Dashboard principal** con métricas clave
- **Análisis temporal** por año/mes
- **Estadísticas productividad** (tasas completación/abandono)
- **Quick stats** para widgets

## 🛡️ Manejo de Errores

Respuestas JSON consistentes:
```json
{
  "error": "BOOK_NOT_FOUND",
  "message": "Book not found with id: 999",
  "timestamp": "2025-08-21T15:30:00"
}
```

## 👨‍💻 Autor

**David Fernández Suco**
- GitHub: [@DavidFernandezSuco](https://github.com/DavidFernandezSuco)
- LinkedIn: [david-fernandez-suco](https://linkedin.com/in/david-fernandez-suco)
- Email: dfsuco@gmail.com

---
