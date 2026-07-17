# Seshat MVP — Implementation Plan

> **Goal:** Transform Seshat from working prototype to presentable, deployable MVP in accelerated 2-week push.

**Architecture:** Fix 29 audit findings → Add production stack → Polish UI → Add key features → CI/CD deploy.

**Tech Stack:** Spring Boot 3.2.5, Thymeleaf, HTMX 1.9.12, Tailwind CSS, PostgreSQL 16, Docker, nginx, Chart.js, iText/flyingsaucer

## Global Constraints
- Java 21, Spring Boot 3.2.5, Maven
- Thymeleaf + HTMX (no SPA frameworks)
- Tailwind CDN → compiled local in production
- Bosque Sereno design system (sage/forest/gold/terracota/stone)
- All forms use hx-post single /guardar endpoint
- CSRF via CookieCsrfTokenRepository
- SLF4J logging in all controllers
- Docker multi-stage (eclipse-temurin:21-jre-alpine)

---

### Task 1: Fix Transactional Issues (🔴 critical)

**Files:**
- Modify: `src/main/java/org/seshat/service/PadrinoService.java`
- Modify: `src/main/java/org/seshat/service/BautizoService.java`
- Modify: `src/main/java/org/seshat/service/ConfirmacionService.java`
- Modify: `src/main/java/org/seshat/service/MatrimonioService.java`
- Modify: `src/main/java/org/seshat/service/PersonaService.java`
- Modify: `src/main/java/org/seshat/service/CertificadoService.java`

- [ ] Add `@Transactional` to `PadrinoService.agregar()` and `eliminar()`
- [ ] Add `@Transactional` to `BautizoService.eliminar()`, `ConfirmacionService.eliminar()`, `MatrimonioService.eliminar()`
- [ ] Reorder `CertificadoService.eliminar()`: DELETE SQL first, THEN file I/O
- [ ] Add file cleanup in `PersonaService.eliminar()` as `afterCommit` callback or reorder
- [ ] Import TransactionSynchronizationManager if needed

### Task 2: Show Person Names in Sacrament Lists (high UX impact)

**Files:**
- Modify: `src/main/java/org/seshat/model/Bautizo.java` — add `nombrePersona`
- Modify: `src/main/java/org/seshat/model/Confirmacion.java` — add `nombrePersona`
- Modify: `src/main/java/org/seshat/model/Matrimonio.java` — add `nombrePersona`
- Modify: `src/main/java/org/seshat/repository/BautizoRepository.java` — JOIN + RowMapper
- Modify: `src/main/java/org/seshat/repository/ConfirmacionRepository.java`
- Modify: `src/main/java/org/seshat/repository/MatrimonioRepository.java`
- Modify: `src/main/resources/templates/bautizos/listar.html`
- Modify: `src/main/resources/templates/confirmaciones/listar.html`
- Modify: `src/main/resources/templates/matrimonios/listar.html`

### Task 3: Search/Filter in Lists

**Files:**
- Modify: `src/main/java/org/seshat/service/PersonaService.java` — add `buscar(String q)`
- Modify: `src/main/java/org/seshat/service/BautizoService.java` — add `buscar(String q)`
- Modify: `src/main/java/org/seshat/service/ConfirmacionService.java`
- Modify: `src/main/java/org/seshat/service/MatrimonioService.java`
- Modify: `src/main/java/org/seshat/repository/PersonaRepository.java`
- Modify: `src/main/java/org/seshat/repository/BautizoRepository.java`
- Modify: `src/main/java/org/seshat/repository/ConfirmacionRepository.java`
- Modify: `src/main/java/org/seshat/repository/MatrimonioRepository.java`
- Modify: `src/main/resources/templates/personas/listar.html`
- Modify: `src/main/resources/templates/bautizos/listar.html`
- Modify: `src/main/resources/templates/confirmaciones/listar.html`
- Modify: `src/main/resources/templates/matrimonios/listar.html`

### Task 4: HTMX Loading Indicators + Transitions

**Files:**
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/templates/personas/listar.html`
- Modify: `src/main/resources/templates/bautizos/listar.html`
- Modify: `src/main/resources/templates/confirmaciones/listar.html`
- Modify: `src/main/resources/templates/matrimonios/listar.html`

### Task 5: Security Fixes

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/org/seshat/config/RateLimitingFilter.java`
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/templates/login.html`
- Modify: `src/main/resources/templates/error.html`

### Task 6: Dashboard with Chart.js + Trend Graph

**Files:**
- Modify: `src/main/resources/templates/dashboard/index.html`
- Modify: `src/main/java/org/seshat/controller/DashboardController.java`
- Modify: `src/main/java/org/seshat/service/DashboardService.java`
- Modify: `src/main/java/org/seshat/dto/StatsDashboard.java`

### Task 7: Docker + nginx

**Files:**
- Create: `Dockerfile`
- Create: `docker-compose.yml`
- Create: `nginx.conf`

### Task 8: Local Assets (Tailwind, HTMX, Fonts)

**Files:**
- Create: `src/main/resources/static/cdn/` directory
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/templates/login.html`
