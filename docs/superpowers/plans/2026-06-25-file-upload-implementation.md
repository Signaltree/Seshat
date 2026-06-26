# File Upload (Certificados + Fotos) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add scanned certificate upload (PDF/imagen) for each sacrament record and photo upload for each person, with multiple files allowed in both cases.

**Architecture:** Two new DB tables (CERTIFICADO, FOTO), a shared FileStorageService for disk I/O (UUID naming, `./uploads/` directory), separate controllers for each feature. Files served via Spring `Resource` streaming through controller endpoints. HTMX fragments for inline upload/list/delete.

**Tech Stack:** Spring Boot 3.2.5, Thymeleaf, HTMX, PostgreSQL, no new dependencies.

## Global Constraints

- Java 21, Spring Boot 3.2.5, Maven, PostgreSQL, Thymeleaf, HTMX CDN, Tailwind CDN
- Model fields use snake_case in DB, camelCase in Java POJOs
- `application.properties`: read `spring.servlet.multipart.max-file-size=10MB`, `spring.servlet.multipart.max-request-size=10MB`, `seshat.upload-dir=./uploads`
- CSRF disabled (single-user app)
- No tests (no JUnit in pom.xml) — verification via `mvn compile`

---
### Task 1: Schema + application.properties + FileStorageService

**Files:**
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/main/resources/application.properties`
- Create: `src/main/java/org/seshat/service/FileStorageService.java`

**Interfaces:**
- Consumes: nothing (pure new code)
- Produces: `FileStorageService` with methods `guardar(MultipartFile, String subdirectorio) → String rutaRelativa`, `eliminar(String rutaRelativa)`, `cargarComoResource(String rutaRelativa) → Resource`

- [ ] **Step 1: Agregar tablas al schema**

Add to `schema.sql` before the last line:

```sql
CREATE TABLE IF NOT EXISTS CERTIFICADO (
  id              SERIAL PRIMARY KEY,
  persona_id      INTEGER NOT NULL REFERENCES PERSONA(id),
  tipo            TEXT NOT NULL,
  entidad_id      INTEGER NOT NULL,
  nombre_original TEXT NOT NULL,
  ruta_archivo    TEXT NOT NULL,
  tipo_archivo    TEXT,
  fecha_subida    DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS FOTO (
  id              SERIAL PRIMARY KEY,
  persona_id      INTEGER NOT NULL REFERENCES PERSONA(id),
  descripcion     TEXT,
  ruta_archivo    TEXT NOT NULL,
  tipo_archivo    TEXT,
  fecha_subida    DATE NOT NULL,
  fecha_foto      DATE
);
```

- [ ] **Step 2: Agregar config multipart + upload dir a application.properties**

```properties
# File uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
seshat.upload-dir=./uploads
```

- [ ] **Step 3: Crear FileStorageService**

File: `src/main/java/org/seshat/service/FileStorageService.java`

```java
package org.seshat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${seshat.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir.resolve("certificados"));
            Files.createDirectories(this.uploadDir.resolve("fotos"));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear directorios de upload: " + this.uploadDir, e);
        }
    }

    public String guardar(MultipartFile archivo, String subdirectorio) {
        if (archivo.isEmpty()) throw new RuntimeException("Archivo vacío");
        String extension = "";
        String original = archivo.getOriginalFilename();
        if (original != null && original.contains("."))
            extension = original.substring(original.lastIndexOf("."));
        String nombre = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(subdirectorio).resolve(nombre);
        try {
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return subdirectorio + "/" + nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }

    public void eliminar(String rutaArchivo) {
        Path archivo = uploadDir.resolve(rutaArchivo).normalize();
        if (!archivo.startsWith(uploadDir))
            throw new SecurityException("Ruta fuera del directorio permitido");
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo", e);
        }
    }

    public Resource cargarComoResource(String rutaArchivo) {
        Path archivo = uploadDir.resolve(rutaArchivo).normalize();
        if (!archivo.startsWith(uploadDir))
            throw new SecurityException("Ruta fuera del directorio permitido");
        try {
            Resource r = new UrlResource(archivo.toUri());
            if (r.exists() && r.isReadable()) return r;
            throw new RuntimeException("Archivo no encontrado: " + rutaArchivo);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer archivo", e);
        }
    }
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```
Expected: no output (success)

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/schema.sql src/main/resources/application.properties src/main/java/org/seshat/service/FileStorageService.java
git commit -m "feat: add schema tables, multipart config, FileStorageService"
```

---
### Task 2: Certificado model + repository + service

**Files:**
- Create: `src/main/java/org/seshat/model/Certificado.java`
- Create: `src/main/java/org/seshat/repository/CertificadoRepository.java`
- Create: `src/main/java/org/seshat/service/CertificadoService.java`

**Interfaces:**
- Consumes: `FileStorageService` from Task 1
- Produces: `CertificadoService.guardar(int personaId, String tipo, int entidadId, MultipartFile archivo) → Certificado`, `listarPorEntidad(String tipo, int entidadId) → List<Certificado>`, `buscarPorId(int id) → Certificado`, `eliminar(int id)`, `cargarComoResource(int id) → Resource`

- [ ] **Step 1: Crear Certificado.java**

```java
package org.seshat.model;

import java.time.LocalDate;

public class Certificado {
    private int id;
    private int personaId;
    private String tipo;
    private int entidadId;
    private String nombreOriginal;
    private String rutaArchivo;
    private String tipoArchivo;
    private LocalDate fechaSubida;

    public Certificado() {}

    public Certificado(int id, int personaId, String tipo, int entidadId,
                       String nombreOriginal, String rutaArchivo,
                       String tipoArchivo, LocalDate fechaSubida) {
        this.id = id;
        this.personaId = personaId;
        this.tipo = tipo;
        this.entidadId = entidadId;
        this.nombreOriginal = nombreOriginal;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.fechaSubida = fechaSubida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getEntidadId() { return entidadId; }
    public void setEntidadId(int entidadId) { this.entidadId = entidadId; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public LocalDate getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDate fechaSubida) { this.fechaSubida = fechaSubida; }
}
```

- [ ] **Step 2: Crear CertificadoRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Certificado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CertificadoRepository {

    private final JdbcTemplate jdbc;

    public CertificadoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Certificado> mapper = (rs, rowNum) -> new Certificado(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("tipo"),
            rs.getInt("entidad_id"),
            rs.getString("nombre_original"),
            rs.getString("ruta_archivo"),
            rs.getString("tipo_archivo"),
            rs.getDate("fecha_subida").toLocalDate()
    );

    public List<Certificado> findByTipoAndEntidadId(String tipo, int entidadId) {
        return jdbc.query("SELECT * FROM CERTIFICADO WHERE tipo = ? AND entidad_id = ? ORDER BY fecha_subida DESC",
                mapper, tipo, entidadId);
    }

    public Certificado findById(int id) {
        return jdbc.queryForObject("SELECT * FROM CERTIFICADO WHERE id = ?", mapper, id);
    }

    public int save(Certificado c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO CERTIFICADO (persona_id, tipo, entidad_id, nombre_original, ruta_archivo, tipo_archivo, fecha_subida) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, c.getPersonaId());
            ps.setString(2, c.getTipo());
            ps.setInt(3, c.getEntidadId());
            ps.setString(4, c.getNombreOriginal());
            ps.setString(5, c.getRutaArchivo());
            ps.setString(6, c.getTipoArchivo());
            ps.setObject(7, c.getFechaSubida());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM CERTIFICADO WHERE id = ?", id);
    }
}
```

- [ ] **Step 3: Crear CertificadoService.java**

```java
package org.seshat.service;

import org.seshat.model.Certificado;
import org.seshat.repository.CertificadoRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoRepository repo;
    private final FileStorageService fileStorage;

    public CertificadoService(CertificadoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    public Certificado guardar(int personaId, String tipo, int entidadId, MultipartFile archivo) {
        String original = archivo.getOriginalFilename();
        if (original == null || original.isBlank()) original = "sin_nombre";
        String extension = "";
        if (original.contains(".")) extension = original.substring(original.lastIndexOf("."));
        String tipoArchivo = switch (extension.toLowerCase()) {
            case ".pdf" -> "application/pdf";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            default -> "application/octet-stream";
        };
        String ruta = fileStorage.guardar(archivo, "certificados");
        Certificado c = new Certificado(0, personaId, tipo, entidadId, original, ruta, tipoArchivo, LocalDate.now());
        int id = repo.save(c);
        c.setId(id);
        return c;
    }

    public List<Certificado> listarPorEntidad(String tipo, int entidadId) {
        return repo.findByTipoAndEntidadId(tipo, entidadId);
    }

    public Certificado buscarPorId(int id) {
        return repo.findById(id);
    }

    public void eliminar(int id) {
        Certificado c = repo.findById(id);
        fileStorage.eliminar(c.getRutaArchivo());
        repo.delete(id);
    }

    public Resource cargarComoResource(int id) {
        Certificado c = repo.findById(id);
        return fileStorage.cargarComoResource(c.getRutaArchivo());
    }
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```
Expected: no output

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/seshat/model/Certificado.java src/main/java/org/seshat/repository/CertificadoRepository.java src/main/java/org/seshat/service/CertificadoService.java
git commit -m "feat: add Certificado model, repository, and service"
```

---
### Task 3: Foto model + repository + service

**Files:**
- Create: `src/main/java/org/seshat/model/Foto.java`
- Create: `src/main/java/org/seshat/repository/FotoRepository.java`
- Create: `src/main/java/org/seshat/service/FotoService.java`

**Interfaces:**
- Consumes: `FileStorageService` from Task 1
- Produces: `FotoService.guardar(int personaId, String descripcion, LocalDate fechaFoto, MultipartFile archivo) → Foto`, `listarPorPersona(int personaId) → List<Foto>`, `buscarPorId(int id) → Foto`, `eliminar(int id)`, `cargarComoResource(int id) → Resource`

- [ ] **Step 1: Crear Foto.java**

```java
package org.seshat.model;

import java.time.LocalDate;

public class Foto {
    private int id;
    private int personaId;
    private String descripcion;
    private String rutaArchivo;
    private String tipoArchivo;
    private LocalDate fechaSubida;
    private LocalDate fechaFoto;

    public Foto() {}

    public Foto(int id, int personaId, String descripcion, String rutaArchivo,
                String tipoArchivo, LocalDate fechaSubida, LocalDate fechaFoto) {
        this.id = id;
        this.personaId = personaId;
        this.descripcion = descripcion;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.fechaSubida = fechaSubida;
        this.fechaFoto = fechaFoto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public LocalDate getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDate fechaSubida) { this.fechaSubida = fechaSubida; }
    public LocalDate getFechaFoto() { return fechaFoto; }
    public void setFechaFoto(LocalDate fechaFoto) { this.fechaFoto = fechaFoto; }
}
```

- [ ] **Step 2: Crear FotoRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Foto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class FotoRepository {

    private final JdbcTemplate jdbc;

    public FotoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Foto> mapper = (rs, rowNum) -> new Foto(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("descripcion"),
            rs.getString("ruta_archivo"),
            rs.getString("tipo_archivo"),
            rs.getDate("fecha_subida").toLocalDate(),
            rs.getDate("fecha_foto") != null ? rs.getDate("fecha_foto").toLocalDate() : null
    );

    public List<Foto> findByPersonaId(int personaId) {
        return jdbc.query("SELECT * FROM FOTO WHERE persona_id = ? ORDER BY fecha_subida DESC",
                mapper, personaId);
    }

    public Foto findById(int id) {
        return jdbc.queryForObject("SELECT * FROM FOTO WHERE id = ?", mapper, id);
    }

    public int save(Foto f) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO FOTO (persona_id, descripcion, ruta_archivo, tipo_archivo, fecha_subida, fecha_foto) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, f.getPersonaId());
            ps.setString(2, f.getDescripcion());
            ps.setString(3, f.getRutaArchivo());
            ps.setString(4, f.getTipoArchivo());
            ps.setObject(5, f.getFechaSubida());
            ps.setObject(6, f.getFechaFoto());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM FOTO WHERE id = ?", id);
    }
}
```

- [ ] **Step 3: Crear FotoService.java**

```java
package org.seshat.service;

import org.seshat.model.Foto;
import org.seshat.repository.FotoRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class FotoService {

    private final FotoRepository repo;
    private final FileStorageService fileStorage;

    public FotoService(FotoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    public Foto guardar(int personaId, String descripcion, LocalDate fechaFoto, MultipartFile archivo) {
        String original = archivo.getOriginalFilename();
        if (original == null || original.isBlank()) original = "sin_nombre";
        String extension = "";
        if (original.contains(".")) extension = original.substring(original.lastIndexOf("."));
        String tipoArchivo = switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
        String ruta = fileStorage.guardar(archivo, "fotos");
        Foto f = new Foto(0, personaId, descripcion, ruta, tipoArchivo, LocalDate.now(), fechaFoto);
        int id = repo.save(f);
        f.setId(id);
        return f;
    }

    public List<Foto> listarPorPersona(int personaId) {
        return repo.findByPersonaId(personaId);
    }

    public Foto buscarPorId(int id) {
        return repo.findById(id);
    }

    public void eliminar(int id) {
        Foto f = repo.findById(id);
        fileStorage.eliminar(f.getRutaArchivo());
        repo.delete(id);
    }

    public Resource cargarComoResource(int id) {
        Foto f = repo.findById(id);
        return fileStorage.cargarComoResource(f.getRutaArchivo());
    }
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/seshat/model/Foto.java src/main/java/org/seshat/repository/FotoRepository.java src/main/java/org/seshat/service/FotoService.java
git commit -m "feat: add Foto model, repository, and service"
```

---
### Task 4: CertificadoController + shared certificate fragment + integration into 3 sacrament forms

**Files:**
- Create: `src/main/java/org/seshat/controller/CertificadoController.java`
- Create: `src/main/resources/templates/fragmentos/certificados.html` (shared HTMX fragment)
- Modify: `src/main/resources/templates/bautizos/formulario.html`
- Modify: `src/main/resources/templates/confirmaciones/formulario.html`
- Modify: `src/main/resources/templates/matrimonios/formulario.html`

**Interfaces:**
- Consumes: `CertificadoService` from Task 2
- Produces: Controller endpoints + HTMX fragment for certificate upload/list/delete

- [ ] **Step 1: Crear CertificadoController.java**

```java
package org.seshat.controller;

import org.seshat.model.Certificado;
import org.seshat.service.CertificadoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoService service;

    public CertificadoController(CertificadoService service) { this.service = service; }

    @PostMapping("/subir")
    public String subir(@RequestParam int personaId, @RequestParam String tipo,
                        @RequestParam int entidadId, @RequestParam MultipartFile archivo,
                        Model model) {
        try {
            service.guardar(personaId, tipo, entidadId, archivo);
        } catch (Exception e) {
            model.addAttribute("error", "Error al subir archivo: " + e.getMessage());
        }
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }

    @GetMapping("/fragmento/{tipo}/{entidadId}")
    public String fragmento(@PathVariable String tipo, @PathVariable int entidadId,
                            @RequestParam int personaId, Model model) {
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable int id) {
        Certificado c = service.buscarPorId(id);
        Resource r = service.cargarComoResource(id);
        String contentType = c.getTipoArchivo() != null ? c.getTipoArchivo() : "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + c.getNombreOriginal() + "\"")
                .body(r);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable int id, @RequestParam String tipo,
                           @RequestParam int entidadId, @RequestParam int personaId,
                           Model model) {
        try {
            service.eliminar(id);
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar archivo");
        }
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }
}
```

- [ ] **Step 2: Crear shared certificate fragment**

File: `src/main/resources/templates/fragmentos/certificados.html`

```html
<div class="certificados-section col-span-2 mt-6 border-t pt-4" th:fragment="certificados" xmlns:th="http://www.thymeleaf.org">
    <h3 class="text-lg font-bold text-granate mb-3">Certificados</h3>
    <div th:if="${error}" class="bg-red-100 text-red-700 px-3 py-2 rounded-lg mb-3" th:text="${error}"></div>
    <div class="space-y-2 mb-4">
        <div th:each="cert : ${certificados}" class="flex items-center justify-between bg-gray-50 px-3 py-2 rounded-lg">
            <div class="flex items-center gap-2">
                <span class="text-gray-700 text-sm" th:text="${cert.nombreOriginal}"></span>
                <span class="text-gray-400 text-xs" th:text="${cert.fechaSubida}"></span>
            </div>
            <div class="flex gap-2">
                <a th:href="@{/certificados/{id}/archivo(id=${cert.id})}" target="_blank"
                   class="text-blue-600 hover:text-blue-800 text-sm">Ver</a>
                <button th:attr="hx-delete=@{/certificados/{id}(id=${cert.id}, tipo=${tipo}, entidadId=${entidadId}, personaId=${personaId})}"
                        hx-target="closest .certificados-section" hx-swap="outerHTML"
                        hx-confirm="¿Eliminar este certificado?"
                        class="text-red-600 hover:text-red-800 text-sm">Eliminar</button>
            </div>
        </div>
        <div th:if="${#lists.isEmpty(certificados)}" class="text-gray-400 text-sm italic">Sin certificados</div>
    </div>
    <form hx-post="/certificados/subir" hx-target="closest .certificados-section" hx-swap="outerHTML"
          enctype="multipart/form-data" class="flex items-end gap-3 flex-wrap">
        <input type="hidden" name="personaId" th:value="${personaId}">
        <input type="hidden" name="tipo" th:value="${tipo}">
        <input type="hidden" name="entidadId" th:value="${entidadId}">
        <div>
            <label class="block text-xs text-gray-500 mb-1">Subir certificado (PDF/imagen)</label>
            <input type="file" name="archivo" accept=".pdf,.jpg,.jpeg,.png" required
                   class="text-sm text-gray-600 file:mr-3 file:py-1 file:px-3 file:rounded-lg file:border-0 file:text-sm file:bg-cuero file:text-white hover:file:bg-cuero-light">
        </div>
        <button type="submit" class="bg-granate text-white px-4 py-1.5 rounded-lg hover:bg-granate-light transition text-sm">Subir</button>
    </form>
</div>
```

- [ ] **Step 3: Add certificate section to bautizos/formulario.html**

After the closing `</form>` on line 50, before the closing `</div>` on line 51:

```html
    <div th:if="${bautizo.id != 0}"
         th:attr="hx-get=@{/certificados/fragmento/BAUTIZO/{id}(id=${bautizo.id}, personaId=${bautizo.persona_id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Step 4: Add certificate section to confirmaciones/formulario.html**

After the closing `</form>` on line 45, before the closing `</div>` on line 46:

```html
    <div th:if="${confirmacion.id != 0}"
         th:attr="hx-get=@{/certificados/fragmento/CONFIRMACION/{id}(id=${confirmacion.id}, personaId=${confirmacion.persona_id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Step 5: Add certificate section to matrimonios/formulario.html**

After the closing `</form>` on line 59, before the closing `</div>` on line 60:

```html
    <div th:if="${matrimonio.id != 0}"
         th:attr="hx-get=@{/certificados/fragmento/MATRIMONIO/{id}(id=${matrimonio.id}, personaId=${matrimonio.persona1_id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Step 6: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/seshat/controller/CertificadoController.java src/main/resources/templates/fragmentos/certificados.html src/main/resources/templates/bautizos/formulario.html src/main/resources/templates/confirmaciones/formulario.html src/main/resources/templates/matrimonios/formulario.html
git commit -m "feat: add certificado upload/list/delete with HTMX fragments"
```

---
### Task 5: FotoController + photo fragment + persona form integration

**Files:**
- Create: `src/main/java/org/seshat/controller/FotoController.java`
- Create: `src/main/resources/templates/fragmentos/fotos.html`
- Modify: `src/main/resources/templates/personas/formulario.html`

**Interfaces:**
- Consumes: `FotoService` from Task 3

- [ ] **Step 1: Crear FotoController.java**

```java
package org.seshat.controller;

import org.seshat.model.Foto;
import org.seshat.service.FotoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fotos")
public class FotoController {

    private final FotoService service;

    public FotoController(FotoService service) { this.service = service; }

    @PostMapping("/subir")
    public String subir(@RequestParam int personaId,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam(required = false) LocalDate fechaFoto,
                        @RequestParam MultipartFile archivo,
                        Model model) {
        try {
            service.guardar(personaId, descripcion, fechaFoto, archivo);
        } catch (Exception e) {
            model.addAttribute("error", "Error al subir foto: " + e.getMessage());
        }
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }

    @GetMapping("/fragmento/{personaId}")
    public String fragmento(@PathVariable int personaId, Model model) {
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> ver(@PathVariable int id) {
        Foto f = service.buscarPorId(id);
        Resource r = service.cargarComoResource(id);
        String contentType = f.getTipoArchivo() != null ? f.getTipoArchivo() : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(r);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable int id, @RequestParam int personaId, Model model) {
        try {
            service.eliminar(id);
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar foto");
        }
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }
}
```

- [ ] **Step 2: Crear photo fragment**

File: `src/main/resources/templates/fragmentos/fotos.html`

```html
<div class="fotos-section col-span-2 mt-6 border-t pt-4" th:fragment="fotos" xmlns:th="http://www.thymeleaf.org">
    <h3 class="text-lg font-bold text-granate mb-3">Fotos</h3>
    <div th:if="${error}" class="bg-red-100 text-red-700 px-3 py-2 rounded-lg mb-3" th:text="${error}"></div>
    <div class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-3 mb-4">
        <div th:each="foto : ${fotos}" class="relative group">
            <a th:href="@{/fotos/{id}/archivo(id=${foto.id})}" target="_blank">
                <img th:src="@{/fotos/{id}/archivo(id=${foto.id})}"
                     class="w-full h-32 object-cover rounded-lg shadow-sm hover:shadow-md transition"
                     th:alt="${foto.descripcion} ?: 'Foto'">
            </a>
            <div th:if="${foto.descripcion}" class="text-xs text-gray-500 mt-1 truncate" th:text="${foto.descripcion}"></div>
            <button th:attr="hx-delete=@{/fotos/{id}(id=${foto.id}, personaId=${personaId})}"
                    hx-target="closest .fotos-section" hx-swap="outerHTML"
                    hx-confirm="¿Eliminar esta foto?"
                    class="absolute top-1 right-1 bg-red-600 text-white rounded-full w-6 h-6 text-xs opacity-0 group-hover:opacity-100 transition flex items-center justify-center">✕</button>
        </div>
    </div>
    <div th:if="${#lists.isEmpty(fotos)}" class="text-gray-400 text-sm italic mb-4">Sin fotos</div>
    <form hx-post="/fotos/subir" hx-target="closest .fotos-section" hx-swap="outerHTML"
          enctype="multipart/form-data" class="flex items-end gap-3 flex-wrap">
        <input type="hidden" name="personaId" th:value="${personaId}">
        <div>
            <label class="block text-xs text-gray-500 mb-1">Descripción</label>
            <input type="text" name="descripcion" class="border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-xs text-gray-500 mb-1">Fecha de la foto</label>
            <input type="date" name="fechaFoto" class="border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-xs text-gray-500 mb-1">Archivo</label>
            <input type="file" name="archivo" accept=".jpg,.jpeg,.png,.gif" required
                   class="text-sm text-gray-600 file:mr-3 file:py-1 file:px-3 file:rounded-lg file:border-0 file:text-sm file:bg-cuero file:text-white hover:file:bg-cuero-light">
        </div>
        <button type="submit" class="bg-granate text-white px-4 py-1.5 rounded-lg hover:bg-granate-light transition text-sm">Subir</button>
    </form>
</div>
```

- [ ] **Step 3: Add photo section to personas/formulario.html**

After the closing `</form>` on line 58, before the closing `</div>` on line 59 (currently the div closes at line 59 in the modified file):

Wait — check the current file. After the form closes on line 58, line 59 is `</div>` (the main wrapping div). Add the photo section between the form and the closing div:

```html
    <div th:if="${persona.id != 0}"
         th:attr="hx-get=@{/fotos/fragmento/{id}(id=${persona.id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/seshat/controller/FotoController.java src/main/resources/templates/fragmentos/fotos.html src/main/resources/templates/personas/formulario.html
git commit -m "feat: add foto upload/list/delete with HTMX fragment in persona form"
```

---
### Task 6: Add directory creation for fragmentos/ to ensure dir exists

Fragments directory `src/main/resources/templates/fragmentos/` doesn't exist yet — it will be created by git when we add files. But let's make sure the directory exists for compilation:

- [ ] **Step 1: Create directory**

```bash
mkdir -p src/main/resources/templates/fragmentos
```

- [ ] **Step 2: Compilar to verify**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit (if fragments directory is empty before steps 4-5)**

Not needed — the directory is created when adding files in Tasks 4 and 5.
