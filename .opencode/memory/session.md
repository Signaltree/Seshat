# Project: Seshat — Parroquia María Misionera

## Active Context
- Current branch: master
- **MVP implementation complete (2026-07-17)** — 8 tasks executed from Enfoque C plan
- **All 🔴 transactional issues fixed** — @Transactional where needed, file I/O outside transactions
- **Search + loading indicators** in all 4 list pages
- **Dashboard with Chart.js trend chart** + monthly stats service
- **Docker + nginx + local assets** ready
- **Build verified**: `mvn clean compile` + local server test (all endpoints 200)

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-25 | Migrated from JavaFX to Spring Boot + Thymeleaf + HTMX | Modern web stack, no client-side install |
| 2026-06-25 | All form submissions use hx-post single /guardar endpoint | HTMX pattern, avoids page reloads |
| 2026-06-25 | Chart.js via CDN + destroy/recreate pattern | HTMX compatibility |
| 2026-06-25 | Certificates stored in separate CERTIFICADO table | Multiple files per sacrament |
| 2026-06-25 | Files stored on disk with UUID naming | Prevents name collisions and path traversal |
| 2026-06-25 | Files served via controller streaming (not ResourceHandler) | Access control for authenticated users only |
| 2026-06-25 | Padrinos aislados por sacramento (no reuse) | Domain accuracy |
| 2026-06-25 | Padrinos inline in sacrament forms (no standalone CRUD) | UX simplicity |
| 2026-06-25 | Used Subagent-Driven Development with review | Quality gates, isolated context per task |
| 2026-06-27 | **ADMIN_PASS env var required at startup** | Eliminates hardcoded default password |
| 2026-06-27 | **BCryptPasswordEncoder** replaces {noop} | Password must be hashed |
| 2026-06-27 | **CSRF re-enabled** via CookieCsrfTokenRepository | Proper CSRF protection for HTMX |
| 2026-06-27 | **SLF4J logging** in all 8 controllers | Structured error context |
| 2026-06-27 | **Magic bytes validation** for file uploads | Attacker can rename .exe to .pdf |
| 2026-06-27 | **Predefined SQL query map** replaces dynamic concat | Eliminates SQL injection |
| 2026-06-27 | **RateLimitingFilter** at HIGHEST_PRECEDENCE | 5 POST/min/IP on /login |
| 2026-07-06 | **Frontend redesign "Bosque Sereno"** — biophilic palette | Natural/minimal aesthetic |
| 2026-07-17 | **Full council audit executed** — 29 findings | Comprehensive project audit |
| 2026-07-17 | **@Transactional added** to PadrinoService, all sacrament services | 🔴 Critical: multi-table ops need atomicity |
| 2026-07-17 | **File I/O moved out of @Transactional** (afterCommit callback + reorder) | 🔴 Critical: exception in file cleanup shouldn't rollback DB |
| 2026-07-17 | **Person names in lists** via SQL JOIN + nombrePersona field | High UX impact: show names not IDs |
| 2026-07-17 | **Search in all lists** via HTMX + ILIKE queries | UX feature request |
| 2026-07-17 | **Chart.js monthly trend** added back to dashboard | Data visualization for priest |
| 2026-07-17 | **Local assets** (HTMX + Tailwind served from /cdn/) | Offline-capable, no CDN dependency |
| 2026-07-17 | **Docker + nginx** deployment stack | Production-ready deployment |
| 2026-07-17 | **CSP updated** — removed unused CDN origins, keep jsdelivr for Chart.js | Security hardening |
| 2026-07-17 | **server.servlet.session.cookie.secure=true** | Requires HTTPS in production |
| 2026-07-17 | **RateLimiter logging + Retry-After header** | Operational visibility |

## Architecture
```
org.seshat
├── SeshatApplication.java
├── config/
│   ├── SecurityConfig.java          ← BCrypt, CSRF, CSP, HSTS, frame-ancestors, XSS
│   ├── RateLimitingFilter.java      ← 5/min/IP POST /login, logging, Retry-After
│   └── GlobalExceptionHandler.java  ← @ControllerAdvice, SLF4J
├── model/                          ← 7 POJOs (nombrePersona added to Bautizo/Confirmacion/Matrimonio)
├── repository/                     ← JdbcTemplate repos, ILIKE search queries
├── service/                        ← @Transactional on all multi-table ops
├── controller/                     ← Search via `q` param on all list endpoints
├── dto/StatsDashboard.java         ← +tendenciaMensual field
├── util/ValidacionUtil.java
```

## Files Changed (2026-07-17 MVP push)
### 🔴 Transaction fixes
- `PadrinoService.java` — @Transactional on agregar()+eliminar()
- `BautizoService.java` — @Transactional on eliminar()
- `ConfirmacionService.java` — @Transactional on eliminar()
- `MatrimonioService.java` — @Transactional on eliminar()
- `CertificadoService.java` — DELETE SQL before file I/O
- `PersonaService.java` — file cleanup via TransactionSynchronization.afterCommit()

### 👤 Person names in lists
- `Bautizo.java` — +nombrePersona field, constructor update
- `Confirmacion.java` — +nombrePersona field, constructor update
- `Matrimonio.java` — +nombrePersona1/2 fields, constructor update
- `BautizoRepository.java` — JOIN query in findAll()+findById()
- `ConfirmacionRepository.java` — JOIN query
- `MatrimonioRepository.java` — JOIN query (double JOIN)
- `bautizos/listar.html` — show ${b.nombrePersona}
- `confirmaciones/listar.html` — show ${c.nombrePersona}
- `matrimonios/listar.html` — show ${m.nombrePersona1/2}

### 🔍 Search + Loading indicators
- `PersonaRepository.java` — +findByQuery() ILIKE on nombres/apellidos/rut
- `BautizoRepository.java` — +findByQuery() ILIKE on PERSONA join
- `ConfirmacionRepository.java` — +findByQuery()
- `MatrimonioRepository.java` — +findByQuery()
- All 4 service classes — +buscar(q)
- All 4 controllers — +q param on @GetMapping
- All 4 listar templates — search input + spinner
- `index.html` — CSS .htmx-indicator + #main-content transition

### 📊 Dashboard Chart.js
- `StatsDashboard.java` — +tendenciaMensual field
- `DashboardService.java` — +obtenerTendenciaMensual() query
- `dashboard/index.html` — Chart.js line chart (3 datasets), spinner

### 🐳 Docker + nginx
- `Dockerfile` — multi-stage (maven build → jre-alpine)
- `docker-compose.yml` — db + app + nginx
- `nginx.conf` — reverse proxy, gzip, static cache

### 📦 Local assets
- `static/cdn/htmx.min.js` — downloaded 1.9.12
- `static/cdn/tailwind.min.js` — downloaded Play CDN
- `index.html`, `login.html`, `error.html` — switch from CDN to local

### 🛡️ Security
- `application.properties` — +secure=true, timeout 30m
- `error.html` — redesigned with Bosque Sereno theme
- `RateLimitingFilter.java` — +SLF4J logging, Retry-After header
- `SecurityConfig.java` — CSP cleanup, permitAll /cdn/ + /uploads/

## Known Issues
- No HTTPS (Docker/nginx ready, needs cert config)
- Rate limiter in-memory (lost on restart, no X-Forwarded-For)
- CSP 'unsafe-inline' (accepted risk for single-user intranet)
- No automated tests yet
- Dashboard Chart.js loaded from CDN (jsdelivr), fails without internet

## Next Steps
1. Deploy with Docker (set env vars + Let's Encrypt SSL)
2. Add SRI integrity hashes to CDN scripts
3. Write automated tests (ValidacionUtil, FileStorageService, services)
4. Add X-Forwarded-For support to RateLimitingFilter
5. Add PDF certificate export
6. Add household/family groups
