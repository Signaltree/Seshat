# Security Hardening Design

## Goal
Remediate 15 security findings from the security audit across 7 implementation tasks.

## Approach
Each task is implemented by a security-specialized subagent, reviewed by another security subagent post-implementation. All tasks run in a single iteration.

## Tasks

### Task 1: BCrypt + Env Var Enforcement
**Findings:** CRÍT-01 (hardcoded default password, {noop})
- Replace `{noop}` with `BCryptPasswordEncoder`
- Make `ADMIN_PASS` env var required (throw on startup if missing)
- Keep `ADMIN_USER` env var with fallback to "admin"

### Task 2: CSRF with HTMX
**Findings:** CRÍT-02 (CSRF disabled)
- Enable CSRF via `CookieCsrfTokenRepository.withHttpOnlyFalse()`
- Add CSRF meta tag to `<head>` in layout template
- Configure HTMX to read CSRF token from meta tag and send as header

### Task 3: Error Handling + Logging
**Findings:** CRÍT-03 (error message leakage), MEDIO-01 (no logging)
- Replace all `e.getMessage()` in catch blocks with generic user-facing messages
- Add SLF4J `Logger` to every controller
- Log errors with structured context (entity IDs, operation type)

### Task 4: File Upload Security
**Findings:** ALTO-01 (extension-only validation), ALTO-05 (inline download)
- Add magic bytes validation in FileStorageService (whitelist: PDF, JPEG, PNG, GIF)
- Change `Content-Disposition: inline` → `attachment`
- Add `X-Content-Type-Options: nosniff` header

### Task 5: Security Headers + HTTPS + Session
**Findings:** ALTO-02 (no CSP/HSTS), ALTO-06 (no HTTPS), MEDIO-04 (no session timeout)
- Configure CSP, HSTS, X-Frame-Options in SecurityConfig
- Add `server.servlet.session.timeout=15m` to properties
- Add HTTPS keystore config (optional — document as deploy-time concern)

### Task 6: Input Validation + Dashboard SQL
**Findings:** MEDIO-02 (no length limits), ALTO-04 (dynamic SQL)
- Add max length validation (100 chars names, 255 rut/email, etc.) in services
- Replace dynamic SQL concatenation in DashboardService with predefined query map

### Task 7: Rate Limiting + SRI + OWASP
**Findings:** ALTO-03 (no rate limit), MEDIO-03 (CDN without SRI), MEDIO-05 (no dep scan)
- Add in-memory rate limiter filter (5 attempts/minute/IP on /login, returns 429)
- Add `integrity` attributes to CDN script tags
- Add OWASP Dependency Check plugin to pom.xml
