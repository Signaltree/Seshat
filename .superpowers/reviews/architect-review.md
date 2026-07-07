# Architecture Review: Security Hardening Plan

**Reviewer:** Software Architect  
**Date:** 2026-07-06  
**Plan:** `docs/superpowers/plans/2026-06-25-security-hardening.md`  
**Design:** `docs/superpowers/specs/2026-06-25-security-hardening-design.md`  
**Codebase:** Seshat — Spring Boot 3.2.5 + Thymeleaf + HTMX parish registry

---

## 1. Overall Assessment

The plan is **sound in structure** and covers all 15 audit findings. The 7-task decomposition is logical, the code snippets are mostly correct, and the global constraints (BCrypt, CSRF via cookie repo, generic error messages, magic bytes, 15min session, 5/min rate limit) are the right targets for a single-user intranet application.

That said, the plan has **4 issues that will cause broken behavior or security regressions if implemented as written**, and **3 missing concerns** that should be addressed before or during implementation.

---

## 2. Architecture Recommendations

### 2.1 CRITICAL: CSP Incompatibility with Tailwind CDN + Inline Scripts

**Problem:** The plan's CSP (Task 5) includes:
```
script-src 'self' https://cdn.tailwindcss.com https://unpkg.com https://cdn.jsdelivr.net
```
This **does not include `'unsafe-inline'`** or a nonce. The application has **two inline `<script>` blocks** that will be blocked:

1. `tailwind.config = { ... }` (index.html lines 11–23) — required for Tailwind CDN theming
2. Enter-key submission handler (index.html lines 69–79)

Additionally, `cdn.tailwindcss.com` injects inline styles at runtime. Without `'unsafe-inline'` on `style-src`, Tailwind's utility classes will not render.

**Recommendation:** Either:
- **(Preferred for this app)** Add `'unsafe-inline'` to both `script-src` and `style-src`. Document the trade-off in a `ponytail:` comment: single-user intranet app, XSS via CSP bypass requires script injection first. Acceptable risk.
- Extract the inline `<script>` blocks into static `.js` files served from `/js/`.
- Replace Tailwind CDN with a pre-built CSS file (eliminates the inline config issue entirely, also solves SRI concern from Task 7).

Change the CSP to:
```
default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com https://unpkg.com https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; img-src 'self' data:; font-src 'self'; form-action 'self'; frame-ancestors 'none'
```

Or more simply, if moving to local static files:
```
default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; form-action 'self'; frame-ancestors 'none'
```

### 2.2 Task 6: Stated scope doesn't match changes shown

**Problem:** Task 6 lists `CertificadoService.java` and `FotoService.java` as files to modify, but the plan shows zero changes for them. Neither service has validation methods — they delegate to `FileStorageService`. Are they included because their `guardar()` methods have extension-based MIME detection that should be tightened? Or are they listed by mistake? The implementer needs clarity.

**Recommendation:** Remove them from the file list if no changes are needed. If you do want to validate there, add it explicitly.

### 2.3 Photo download: Inline vs Attachment UX

**Problem:** Task 4 changes `FotoController.ver()` from `Content-Disposition: inline` to `Content-Disposition: attachment`. This means photos will trigger a browser download prompt instead of displaying in the page. For a parish registry where the priest views photos of parishioners, this is a UX degradation.

The security concern (ALTO-05) is about content-sniffing XSS, which is already mitigated by `X-Content-Type-Options: nosniff` + correct `Content-Type: image/jpeg`. Forcing `attachment` on photos is overly conservative.

**Recommendation:** Keep `inline` for photos (images), use `attachment` only for certificates (PDFs). Add `nosniff` to both. The Foto controller can safely return `inline` with `nosniff` since the content type is image/* (not text/html).

### 2.4 Rate limiting filter: dual-registration ambiguity

**Problem:** Task 7's `RateLimitingFilter.java` code has both `@Component @Order(1)` (auto-registration) AND the plan discusses a `FilterRegistrationBean` approach. Both are described as alternatives, but the code block includes `@Component @Order(1)` while the text recommends `FilterRegistrationBean`. The implementer needs a single, unambiguous approach.

**Recommendation:** Pick one and remove the other from the code. The `@Component @Order(1)` approach is simpler and achieves the same result (the filter runs in the servlet container chain, before Spring Security's filter chain). Stick with that. Remove the `FilterRegistrationBean` discussion.

---

## 3. Task Order Conflicts

The order is largely correct. One observation:

- **Task 2 (CSRF) and Task 5 (security headers)** could be merged — both modify `SecurityConfig.java`. However, keeping them separate makes commits atomic and reviewable. Fine as is.
- **Task 6 (input validation) should ideally come before Task 3 (error handling)** — so that validation errors are properly formatted before error message cleanup. But the validation errors are returned via `errores` map, not exceptions, so they're not affected by the exception-handling changes. No conflict.
- **Task 7 (rate limiting) touches `SecurityConfig.java`** — same file as Tasks 1, 2, 5. Merge conflicts if implemented in parallel. If doing sequential implementation, order matters: Task 1 → Task 2 → Task 5 → Task 7 touching SecurityConfig. The plan's implicit order is correct.

---

## 4. Missing Concerns

### 4.1 No global exception handler

The plan adds try-catch blocks per controller, but misses:
- **`DashboardController.dashboard()`** — no try-catch at all. If `service.obtenerStats()` throws, the stack trace propagates to Spring's default error page (leaks internal details).
- **`BautizoController.guardar()`**, **`ConfirmacionController.guardar()`**, **`MatrimonioController.guardar()`** — no try-catch. An uncaught exception in any of these leaks stack trace.
- **`PersonaController.guardar()`** — no try-catch around `service.guardar()`/`service.actualizar()`.

**Recommendation:** Instead of per-controller try-catch blocks (which are easy to miss), add a **global `@ControllerAdvice`** that catches `Exception` and returns a generic error to the model. This is the standard Spring pattern and is more robust. Then the per-controller blocks become only for cases needing specific recovery (e.g., `DataIntegrityViolationException`).

```
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleError(Model model) {
        log.error("Error no controlado", e);
        model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        return "error"; // or redirect
    }
}
```

For HTMX fragment controllers (Foto, Certificado, Padrino), uncaught exceptions return empty fragments with no error UI. The per-controller catch is still needed there to add the error to the model and return the fragment. But for full-page controllers (Persona, Bautizo, Confirmacion, Matrimonio, Dashboard), the global handler covers them.

### 4.2 OWASP failBuildOnCVSS is too aggressive

Setting `failBuildOnCVSS > 7` means the build fails on ANY dependency with a CVSS 7+ vulnerability. For a Spring Boot 3.2.5 app with transitive dependencies, this will almost certainly break the build on first run — many widely-used libraries have older CVEs that won't be patched.

**Recommendation:** Either:
- Set to `failBuildOnCVSS > 9` (only critical), or
- Run in report-only mode (remove `<failBuildOnCVSS>`), or
- Add a note to the implementer that the first `mvn verify` will likely fail and they'll need to suppress specific CVEs via `<suppressionFiles>`.

Since this is a single-user intranet app not exposed to the internet, report-only is appropriate.

### 4.3 No verification/rollback plan for CSRF breakage

Task 2 re-enables CSRF, which WILL break all HTMX POST/PUT/DELETE requests if the meta tags or JS event handler are misconfigured. The plan has no verification step for this beyond `mvn compile`.

**Recommendation:** Add a verification step: start the app, log in, and test at least one POST operation (e.g., create a persona) to confirm CSRF works with HTMX.

### 4.4 PersonaController.guardar() has no exception handling

The plan correctly identifies the controllers with `e.getMessage()` leaks, but misses that `PersonaController.guardar()` has NO try-catch at all (line 37-41). An exception here will propagate to Spring's default error page, leaking stack traces.

**Recommendation:** Add try-catch to `PersonaController.guardar()`.

---

## 5. Implementation Notes

### 5.1 CSRF token on login page

The login form uses `<form method="post" th:action="@{/login}">`. With CSRF enabled, Thymeleaf automatically includes a `_csrf` hidden input when the form uses `th:action`. **This should work without changes** to login.html. No additional work needed.

### 5.2 Logout form with CSRF

The logout button in `index.html` uses `<form method="post" th:action="@{/logout}">`. Same as login — Thymeleaf auto-injects the CSRF token. This will work. But verify after implementation.

### 5.3 CookieCsrfTokenRepository with HttpOnly false

`CookieCsrfTokenRepository.withHttpOnlyFalse()` sets the CSRF cookie as readable by JavaScript. This is **required** for HTMX to work — HTMX reads the token from the meta tag, not the cookie. The cookie is only the transport mechanism between server and Thymeleaf. Verify that the meta tags are in the `<head>` of the initial page load (they are — index.html).

### 5.4 Magic bytes: 4 bytes is sufficient but not exhaustive for JPEG

The plan reads 4 bytes for magic byte validation. For JPEG, the first 2 bytes are always `FF D8` (SOI marker). Bytes 3-4 vary by APP marker. The plan checks `FF D8 FF E0`, `FF D8 FF E1`, `FF D8 FF E2` — this covers most JPEGs but misses `FF D8 FF DB` (JPEG with no APP0) and `FF D8 FF FE` (JPEG with COM comment). For a single-user intranet app, this coverage is acceptable.

### 5.5 HSTS includeSubDomains for localhost

`includeSubDomains(true)` with `maxAgeInSeconds(31536000)` is fine for intranet but unnecessary for localhost. Consider documenting this as a deploy-time concern along with HTTPS.

### 5.6 Session cookie same-site

Adding `server.servlet.session.cookie.same-site=lax` is correct and prevents CSRF for non-GET navigation. With CSRF already enabled via `CookieCsrfTokenRepository`, this is defense-in-depth.

---

## 6. Verdict

**APPROVED — WITH CHANGES REQUESTED**

### Summary

The plan comprehensively addresses the 15 audit findings with well-scoped tasks. Four issues must be resolved before implementation: **(1)** CSP must account for Tailwind CDN's inline script/config requirements — add `'unsafe-inline'` or extract inline scripts to static files; **(2)** keep `Content-Disposition: inline` for photos (images with `nosniff` are safe) instead of blanket attachment; **(3)** CertificadoService/FotoService are listed in Task 6 with no described changes — clarify or remove; **(4)** add a global `@ControllerAdvice` for uncaught exceptions, plus specific try-catch for PersonaController.guardar() and DashboardController. Additionally, relax the OWASP `failBuildOnCVSS` threshold given the intranet threat model.

The implementation order (1→2→3→4→5→6→7) is sound. Each task is independently testable. With the above corrections, this plan is ready for execution.
