# Project: Seshat — Parroquia María Misionera

## Active Context
- Current branch: master (local only, single dev)
- Active task: Security hardening implementation (7 tasks, Task 1 brief created, not yet dispatched)
- Last files modified: SecurityConfig.java (to be replaced), FileStorageService, all controllers (to be updated)

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-25 | Migrated from JavaFX to Spring Boot + Thymeleaf + HTMX | Modern web stack, no client-side install |
| 2026-06-25 | CSRF disabled during initial dev | Single-user app, simplified HTMX integration (REVERSED: being re-enabled in security hardening) |
| 2026-06-25 | All form submissions use hx-post single /guardar endpoint | HTMX pattern, avoids page reloads |
| 2026-06-25 | Chart.js via CDN + destroy/recreate pattern | HTMX compatibility |
| 2026-06-25 | Certificates stored in separate CERTIFICADO table | Multiple files per sacrament, not just one ruta_imagen |
| 2026-06-25 | Files stored on disk with UUID naming | Prevents name collisions and path traversal |
| 2026-06-25 | Files served via controller streaming (not ResourceHandler) | Access control for authenticated users only |
| 2026-06-25 | Padrinos aislados por sacramento (no reuse) | Domain accuracy: a padrino at bautizo is not a padrino at matrimonio |
| 2026-06-25 | Padrinos inline in sacrament forms (no standalone CRUD) | UX simplicity: manage padrinos within each sacrament form |
| 2026-06-25 | Used Subagent-Driven Development with review | Quality gates, isolated context per task |
| 2026-06-27 | ADMlN_PASS env var required at startup (no fallback) | CRÍT-01 remediation — eliminates hardcoded default password |
| 2026-06-27 | BCryptPasswordEncoder replaces {noop} | CRÍT-01 remediation — password must be hashed |
| 2026-06-27 | CSRF re-enabled via CookieCsrfTokenRepository | CRÍT-02 remediation — proper CSRF protection for HTMX |
| 2026-06-27 | Magic bytes validation for file uploads (not just extension) | ALTO-01 remediation — attacker can rename .exe to .pdf |
| 2026-06-27 | Content-Disposition: attachment for all file downloads | ALTO-05 remediation — prevents XSS via inline rendering |
| 2026-06-27 | Dynamic SQL replaced with predefined query map in DashboardService | ALTO-04 remediation — eliminates SQL injection via parameters |

## Architecture

```
org.seshat
├── SeshatApplication.java
├── config/
│   ├── SecurityConfig.java          ← Spring Security (BCrypt, CSRF, CSP, HSTS, session)
│   └── RateLimitingFilter.java      ← In-memory rate limiter (5 POST/min/IP on /login)
├── model/                          ← POJOs (Persona, Bautizo, Confirmacion, Matrimonio, Padrino, Certificado, Foto)
├── repository/                     ← JdbcTemplate repos (all use new String[]{"id"} for RETURN_GENERATED_KEYS)
├── service/
│   ├── PersonaService.java          ← Validates RUT, email, phone, field lengths
│   ├── BautizoService.java         ← CRUD + padrino cleanup on delete
│   ├── ConfirmacionService.java    ← CRUD + padrino cleanup on delete
│   ├── MatrimonioService.java      ← CRUD + padrino cleanup on delete
│   ├── PadrinoService.java         ← RUT validation, orphan cleanup, role mgmt
│   ├── CertificadoService.java     ← CRUD for certificates
│   ├── FotoService.java            ← CRUD for photos
│   ├── FileStorageService.java     ← UUID naming, path traversal protection, magic bytes validation
│   └── DashboardService.java       ← Stats with predefined query map (NOT dynamic SQL)
├── controller/
│   ├── PersonaController.java      ← CRUD + DataIntegrityViolationException + logging
│   ├── BautizoController.java      ← CRUD + padrinos + certificates + logging
│   ├── ConfirmacionController.java ← CRUD + padrinos + certificates + logging
│   ├── MatrimonioController.java   ← CRUD + padrinos + certificates + logging
│   ├── CertificadoController.java  ← Upload/download/delete + attachment + logging
│   ├── FotoController.java         ← Upload/view/delete + attachment + logging
│   ├── PadrinoController.java      ← HTMX fragment + add/delete + logging
│   ├── DashboardController.java    ← Stats with year/month filters
│   └── LoginController.java        ← Login page
├── dto/
│   └── StatsDashboard.java         ← Dashboard stats DTO
└── util/
    └── ValidacionUtil.java         ← RUT (mod-11), email regex, Chilean phone regex
```

## Key Dependencies
- Spring Boot 3.2.5, Java 21
- PostgreSQL 42.7.3 (driver only, runtime scope)
- Thymeleaf + spring-security6 extras
- HTMX 1.9.12 (CDN: unpkg.com)
- Tailwind CSS (CDN: cdn.tailwindcss.com)
- Chart.js (CDN: cdn.jsdelivr.net/npm/chart.js)
- No JUnit, no test framework (not in pom.xml)

## Database Schema
11 tables: PERSONA, PADRINO, BAUTIZO, CONFIRMACION, MATRIMONIO, BAUTIZO_PADRINO, CONFIRMACION_PADRINO, MATRIMONIO_PADRINO, CERTIFICADO, FOTO + sequences

## Security Posture (CURRENT — being hardened)
- Before hardening: {noop} passwords, CSRF disabled, inline downloads, no CSP/HSTS, dynamic SQL, no rate limiting, CDN without SRI, no logging, error messages leak internals
- After hardening: BCrypt, required ADMIN_PASS env var, CSRF with CookieCsrfTokenRepository + HTMX meta tags, magic bytes validation, attachment downloads with nosniff, CSP/HSTS/X-Frame-Options/XSS-Protection, 15min session timeout, predefined SQL queries, rate limiting (5/min/IP on login), SRI attributes, OWASP Dependency Check

## Known Issues
- No test framework (JUnit not in pom.xml)
- No HTTPS (requires deploy-time keystore config or reverse proxy)
- No Docker configuration
- OWASP Dependency Check is configured but hashes for SRI attributes still need verification
- Rate limiting uses in-memory ConcurrentHashMap (lost on restart — acceptable for single-user app)

## Next Steps
1. Implement Security Hardening Task 1: BCrypt + env var enforcement (SecurityConfig.java)
2. Implement Security Hardening Task 2: CSRF with HTMX Token (SecurityConfig.java + index.html)
3. Implement Security Hardening Task 3: Error handling + logging (all controllers)
4. Implement Security Hardening Task 4: File upload security (FileStorageService + controllers)
5. Implement Security Hardening Task 5: Security headers + session config (SecurityConfig + properties)
6. Implement Security Hardening Task 6: Input validation + Dashboard SQL refactor
7. Implement Security Hardening Task 7: Rate limiting + SRI + OWASP plugin
8. Final whole-branch code review
9. Verify mvn compile -q after all changes
10. Consider adding HTTPS via Let's Encrypt or reverse proxy (nginx)
