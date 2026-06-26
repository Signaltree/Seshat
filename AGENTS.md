# Seshat — AGENTS.md

## Project

Spring Boot + Thymeleaf + HTMX + Tailwind (CDN) web app for parish sacraments registry (Baptism, Confirmation, Marriage). Parroquia María Misionera de Renca.

- **Java 21, Spring Boot 3.2.5, Thymeleaf, PostgreSQL 42.7.3**
- **Main class:** `org.seshat.SeshatApplication`

## Commands

```sh
mvn compile               # compiles
mvn spring-boot:run       # runs the web app
```

No test framework configured (JUnit not in `pom.xml`). No tests exist yet.

## Database

- **Environment variables required:** `DB_URL`, `DB_USER`, `DB_PASSWORD` (read by `application.properties`, no fallbacks)
- Schema in `src/main/resources/db/schema.sql` — 5 tables (`PERSONA`, `PADRINO`, `BAUTIZO`, `CONFIRMACION`, `MATRIMONIO`, plus 3 join tables for godparents)

## Auth

- **Spring Security** with form login
- Default credentials: `admin` / `admin123`
- Override via env vars: `ADMIN_USER`, `ADMIN_PASS`
- CSRF disabled (single-user app)

## Validation

- RUT validation with check digit (formato: XX.XXX.XXX-X)
- Email format validation
- Chilean phone format validation (+56 9 XXXX XXXX)
- All in `org.seshat.util.ValidacionUtil`

## Architecture

```
org.seshat
├── SeshatApplication.java           ← entry point (Spring Boot)
├── config/SecurityConfig.java       ← Spring Security config
├── model/                           ← POJO model classes
├── repository/                      ← JdbcTemplate repositories
├── service/                         ← business logic + validation
├── controller/                      ← Spring MVC controllers
└── util/ValidacionUtil.java         ← RUT, email, phone validators
```
