# Seshat — Migración a Spring Boot + Thymeleaf + HTMX + Tailwind

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.x, Java 21, Maven |
| DB access | Spring JDBC (`JdbcTemplate`) |
| Template engine | Thymeleaf |
| Frontend interactivo | HTMX (CDN) |
| CSS | Tailwind CDN (play CDN) |
| Base de datos | PostgreSQL (mismo schema) |

## Arquitectura

```
Cliente (navegador)
     ↕ HTTP
Spring MVC Controller (@Controller)
     ↕
Service (@Service)
     ↕
Repository (JdbcTemplate)
     ↕
PostgreSQL
```

Sin capas UI/FXML/JavaFX. Todo es HTML + HTMX + Tailwind servido por Spring.

## Convenciones de código

- **Modelos:** POJOs con `snake_case` en campos (coherencia con `Persona.java` existente)
- **Repositorios:** Clases concretas con `JdbcTemplate`, no interfaces con implementación única (ponytail)
- **Servicios:** Anotados `@Service`, inyectan repositorios
- **Controladores:** Anotados `@Controller`, rutas RESTful, devuelven Thymeleaf o fragmentos HTMX
- **Frontend:** Sin JavaScript custom. HTMX attributes para toda interacción.

## Database

- Config en `application.properties` via environment variables:
  ```properties
  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USER}
  spring.datasource.password=${DB_PASSWORD}
  ```
- Mismo schema (`db/schema.sql`). Sin migraciones automáticas (Flyway/Liquibase) por ahora.

## UI / UX

- **Pantalla única** con sidebar de navegación.
- Sidebar: botones grandes con iconos (Personas, Bautizos, Confirmaciones, Matrimonios).
- Contenido se carga vía HTMX `hx-get` en el div principal.
- Formularios en modales o paneles inline.
- Diseño responsive básico (móvil no es prioridad, pero que no se rompa).
- Botones grandes, tipografía clara, contraste suficiente — pensado para usuario con poca cultura digital.

## Archivos legacy a eliminar

- `src/main/java/org/seshat/Main.java`
- `src/main/java/org/seshat/util/DatabaseConnection.java`
- `src/main/java/org/seshat/ui/*` (todos los controladores JavaFX)
- `src/main/resources/fxml/*`

## Archivos nuevos a crear

- `src/main/java/org/seshat/SeshatApplication.java` — entry point Spring Boot
- `src/main/java/org/seshat/controller/PersonaController.java`
- `src/main/java/org/seshat/controller/BautizoController.java`
- `src/main/java/org/seshat/controller/ConfirmacionController.java`
- `src/main/java/org/seshat/controller/MatrimonioController.java`
- `src/main/resources/application.properties`
- `src/main/resources/templates/index.html` — layout principal
- `src/main/resources/templates/personas/` — CRUD personas
- `src/main/resources/templates/bautizos/` — CRUD bautizos
- `src/main/resources/templates/confirmaciones/` — CRUD confirmaciones
- `src/main/resources/templates/matrimonios/` — CRUD matrimonios
- `src/main/resources/templates/fragmentos/` — componentes reutilizables HTMX

## Orden de implementación

1. `pom.xml` — cambiar dependencias
2. Modelos — implementar `Bautizo`, `Confirmacion`, `Matrimonio`
3. Repositorios — implementar con JdbcTemplate
4. Servicios — @Service con lógica básica CRUD
5. `SeshatApplication.java` — entry point
6. `application.properties` — config
7. Controladores — Spring MVC
8. Templates — Thymeleaf + HTMX + Tailwind
9. Limpiar legacy
10. Compilar y verificar
