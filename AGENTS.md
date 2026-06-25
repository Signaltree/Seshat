# Seshat — AGENTS.md

## Project

Spring Boot + Thymeleaf + HTMX + Tailwind (CDN) web app for parish sacraments registry (Baptism, Confirmation, Marriage). Parroquia María Misionera de Renca.

- **Java 21, Spring Boot 3.2.3, Thymeleaf, PostgreSQL 42.7.3**
- **Main class:** `org.seshat.SeshatApplication`

## Commands

```sh
mvn compile               # compiles
mvn spring-boot:run       # runs the web app
```

No test framework configured (JUnit not in `pom.xml`). No tests exist yet.

## Database

- **Environment variables required:** `DB_URL`, `DB_USER`, `DB_PASSWORD` (read by `application.properties`, no fallbacks)
- Schema in `src/main/resources/db/schema.sql` — 4 tables (`PERSONA`, `PADRINO`, `BAUTIZO`, `CONFIRMACION`, `MATRIMONIO`, plus 3 join tables for godparents)

## Architecture

```
org.seshat
├── SeshatApplication.java           ← entry point (Spring Boot)
├── model/                           ← JPA entities
├── repository/                      ← Spring Data JPA repositories
├── service/                         ← business logic
├── controller/                      ← Spring MVC controllers
└── util/DatabaseConnection.java     ← legacy (kept for reference)
```
