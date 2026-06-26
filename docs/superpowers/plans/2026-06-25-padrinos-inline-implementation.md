# Padrinos Inline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add inline godparent (padrino) management to bautizo, confirmacion, and matrimonio forms using HTMX fragments.

**Architecture:** Same pattern as existing certificados-section/fotos-section. A PadrinoController handles HTMX endpoints (listar, agregar, eliminar). PadrinoService wraps business logic + RUT validation. Changes to sacrament services ensure join records are cleaned up on sacrament deletion.

**Tech Stack:** Java 21, Spring Boot 3.2.5, JdbcTemplate, Thymeleaf, HTMX CDN

## Global Constraints

- Model fields: snake_case in DB, camelCase in Java POJOs
- RUT validation via `ValidacionUtil.validarRut(rut)`: returns `Map<String, String>` with key "rut" on error, empty map on success
- CSRF disabled
- `new String[]{"id"}` required for `RETURN_GENERATED_KEYS` in PostgreSQL (not `Statement.RETURN_GENERATED_KEYS`)
- Tema eclesiástico: granate #5B2C3E, cuero #8B4513, crema #F5F0EB, dorado #C9A84C

---

### Task 1: Extend PadrinoRepository

**Files:**
- Modify: `src/main/java/org/seshat/repository/PadrinoRepository.java`

**Interfaces:**
- Produces: sacrament-specific query methods consumed by PadrinoService (Task 2)

- [ ] **Read file to see current state**

- [ ] **Replace PadrinoRepository.java with extended version**

```java
package org.seshat.repository;

import org.seshat.model.Padrino;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class PadrinoRepository {
    private final JdbcTemplate jdbc;

    public PadrinoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Padrino> mapper = (rs, rowNum) -> new Padrino(
            rs.getInt("id"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("rut")
    );

    public List<Padrino> findAll() {
        return jdbc.query("SELECT * FROM PADRINO ORDER BY apellidos, nombres", mapper);
    }

    public Padrino findById(int id) {
        return jdbc.queryForObject("SELECT * FROM PADRINO WHERE id = ?", mapper, id);
    }

    public int save(Padrino p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PADRINO (nombres, apellidos, rut) VALUES (?,?,?)",
                    new String[]{"id"});
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getRut());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Padrino p) {
        jdbc.update("UPDATE PADRINO SET nombres=?, apellidos=?, rut=? WHERE id=?",
                p.getNombres(), p.getApellidos(), p.getRut(), p.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM PADRINO WHERE id=?", id);
    }

    // --- Sacrament queries ---

    public List<Padrino> findByBautizoId(int bautizoId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN BAUTIZO_PADRINO bp ON p.id = bp.padrino_id WHERE bp.bautizo_id = ? ORDER BY bp.rol", mapper, bautizoId);
    }

    public List<Padrino> findByConfirmacionId(int confirmacionId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN CONFIRMACION_PADRINO cp ON p.id = cp.padrino_id WHERE cp.confirmacion_id = ? ORDER BY cp.rol", mapper, confirmacionId);
    }

    public List<Padrino> findByMatrimonioId(int matrimonioId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN MATRIMONIO_PADRINO mp ON p.id = mp.padrino_id WHERE mp.matrimonio_id = ? ORDER BY mp.rol", mapper, matrimonioId);
    }

    // --- Join table management ---

    public void insertarBautizoPadrino(int bautizoId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO BAUTIZO_PADRINO (bautizo_id, padrino_id, rol) VALUES (?,?,?)", bautizoId, padrinoId, rol);
    }

    public void insertarConfirmacionPadrino(int confirmacionId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO CONFIRMACION_PADRINO (confirmacion_id, padrino_id, rol) VALUES (?,?,?)", confirmacionId, padrinoId, rol);
    }

    public void insertarMatrimonioPadrino(int matrimonioId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO MATRIMONIO_PADRINO (matrimonio_id, padrino_id, rol) VALUES (?,?,?)", matrimonioId, padrinoId, rol);
    }

    public void eliminarBautizoPadrino(int padrinoId, int bautizoId) {
        jdbc.update("DELETE FROM BAUTIZO_PADRINO WHERE padrino_id = ? AND bautizo_id = ?", padrinoId, bautizoId);
    }

    public void eliminarConfirmacionPadrino(int padrinoId, int confirmacionId) {
        jdbc.update("DELETE FROM CONFIRMACION_PADRINO WHERE padrino_id = ? AND confirmacion_id = ?", padrinoId, confirmacionId);
    }

    public void eliminarMatrimonioPadrino(int padrinoId, int matrimonioId) {
        jdbc.update("DELETE FROM MATRIMONIO_PADRINO WHERE padrino_id = ? AND matrimonio_id = ?", padrinoId, matrimonioId);
    }

    public void eliminarBautizoPadrinosPorBautizo(int bautizoId) {
        jdbc.update("DELETE FROM BAUTIZO_PADRINO WHERE bautizo_id = ?", bautizoId);
    }

    public void eliminarConfirmacionPadrinosPorConfirmacion(int confirmacionId) {
        jdbc.update("DELETE FROM CONFIRMACION_PADRINO WHERE confirmacion_id = ?", confirmacionId);
    }

    public void eliminarMatrimonioPadrinosPorMatrimonio(int matrimonioId) {
        jdbc.update("DELETE FROM MATRIMONIO_PADRINO WHERE matrimonio_id = ?", matrimonioId);
    }

    public boolean padrinoEstaReferenciado(int padrinoId) {
        Integer count = jdbc.queryForObject(
            "SELECT (SELECT COUNT(*) FROM BAUTIZO_PADRINO WHERE padrino_id = ?) + " +
            "(SELECT COUNT(*) FROM CONFIRMACION_PADRINO WHERE padrino_id = ?) + " +
            "(SELECT COUNT(*) FROM MATRIMONIO_PADRINO WHERE padrino_id = ?)",
            Integer.class, padrinoId, padrinoId, padrinoId);
        return count != null && count > 0;
    }

    // --- Role queries ---

    public String obtenerRolBautizo(int padrinoId, int bautizoId) {
        return jdbc.queryForObject("SELECT rol FROM BAUTIZO_PADRINO WHERE padrino_id = ? AND bautizo_id = ?", String.class, padrinoId, bautizoId);
    }

    public String obtenerRolConfirmacion(int padrinoId, int confirmacionId) {
        return jdbc.queryForObject("SELECT rol FROM CONFIRMACION_PADRINO WHERE padrino_id = ? AND confirmacion_id = ?", String.class, padrinoId, confirmacionId);
    }

    public String obtenerRolMatrimonio(int padrinoId, int matrimonioId) {
        return jdbc.queryForObject("SELECT rol FROM MATRIMONIO_PADRINO WHERE padrino_id = ? AND matrimonio_id = ?", String.class, padrinoId, matrimonioId);
    }
}
```

- [ ] **Verify compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add src/main/java/org/seshat/repository/PadrinoRepository.java
git commit -m "feat: extend PadrinoRepository with sacrament queries and join table methods"
```

---

### Task 2: Create PadrinoService

**Files:**
- Create: `src/main/java/org/seshat/service/PadrinoService.java`

**Interfaces:**
- Consumes: `PadrinoRepository` methods from Task 1
- Produces: methods consumed by PadrinoController (Task 4)

- [ ] **Create PadrinoService.java**

```java
package org.seshat.service;

import org.seshat.model.Padrino;
import org.seshat.repository.PadrinoRepository;
import org.seshat.util.ValidacionUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PadrinoService {
    private final PadrinoRepository repo;

    public PadrinoService(PadrinoRepository repo) {
        this.repo = repo;
    }

    public List<Padrino> listarPorSacramento(String tipo, int sacramentoId) {
        return switch (tipo) {
            case "BAUTIZO" -> repo.findByBautizoId(sacramentoId);
            case "CONFIRMACION" -> repo.findByConfirmacionId(sacramentoId);
            case "MATRIMONIO" -> repo.findByMatrimonioId(sacramentoId);
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }

    public Map<String, String> agregar(String tipo, int entidadId, String nombres, String apellidos, String rut, String rol) {
        Map<String, String> errores = new HashMap<>();
        if (nombres == null || nombres.isBlank()) errores.put("nombres", "El nombre es obligatorio");
        if (apellidos == null || apellidos.isBlank()) errores.put("apellidos", "Los apellidos son obligatorios");
        if (rol == null || rol.isBlank()) errores.put("rol", "El rol es obligatorio");
        if (rut != null && !rut.isBlank()) {
            Map<String, String> rutErrores = ValidacionUtil.validarRut(rut);
            if (!rutErrores.isEmpty()) errores.putAll(rutErrores);
        }
        if (!errores.isEmpty()) return errores;

        Padrino p = new Padrino(0, nombres.trim(), apellidos.trim(), rut != null ? rut.trim() : null);
        int padrinoId = repo.save(p);

        switch (tipo) {
            case "BAUTIZO" -> repo.insertarBautizoPadrino(entidadId, padrinoId, rol.trim());
            case "CONFIRMACION" -> repo.insertarConfirmacionPadrino(entidadId, padrinoId, rol.trim());
            case "MATRIMONIO" -> repo.insertarMatrimonioPadrino(entidadId, padrinoId, rol.trim());
        }
        return errores;
    }

    public void eliminar(String tipo, int padrinoId, int sacramentoId) {
        switch (tipo) {
            case "BAUTIZO" -> repo.eliminarBautizoPadrino(padrinoId, sacramentoId);
            case "CONFIRMACION" -> repo.eliminarConfirmacionPadrino(padrinoId, sacramentoId);
            case "MATRIMONIO" -> repo.eliminarMatrimonioPadrino(padrinoId, sacramentoId);
        }
        if (!repo.padrinoEstaReferenciado(padrinoId)) {
            repo.delete(padrinoId);
        }
    }

    public String obtenerRol(String tipo, int padrinoId, int sacramentoId) {
        return switch (tipo) {
            case "BAUTIZO" -> repo.obtenerRolBautizo(padrinoId, sacramentoId);
            case "CONFIRMACION" -> repo.obtenerRolConfirmacion(padrinoId, sacramentoId);
            case "MATRIMONIO" -> repo.obtenerRolMatrimonio(padrinoId, sacramentoId);
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }
}
```

- [ ] **Verify compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add src/main/java/org/seshat/service/PadrinoService.java
git commit -m "feat: add PadrinoService with business logic and RUT validation"
```

---

### Task 3: Update Sacrament Services to Clean Up Padrino Join Records

**Files:**
- Modify: `src/main/java/org/seshat/service/BautizoService.java`
- Modify: `src/main/java/org/seshat/service/ConfirmacionService.java`
- Modify: `src/main/java/org/seshat/service/MatrimonioService.java`

**Interfaces:**
- Consumes: `PadrinoRepository.eliminarBautizoPadrinosPorBautizo(id)` (and equivalents)

- [ ] **Update BautizoService with PadrinoRepository dependency**

```java
package org.seshat.service;

import org.seshat.model.Bautizo;
import org.seshat.repository.BautizoRepository;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BautizoService {
    private final BautizoRepository repo;
    private final PadrinoRepository padrinoRepo;

    public BautizoService(BautizoRepository repo, PadrinoRepository padrinoRepo) {
        this.repo = repo;
        this.padrinoRepo = padrinoRepo;
    }

    public List<Bautizo> listar() { return repo.findAll(); }
    public Bautizo obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Bautizo b) { return repo.save(b); }
    public void actualizar(Bautizo b) { repo.update(b); }
    public void eliminar(int id) {
        padrinoRepo.eliminarBautizoPadrinosPorBautizo(id);
        repo.delete(id);
    }
}
```

- [ ] **Update ConfirmacionService with PadrinoRepository dependency**

```java
package org.seshat.service;

import org.seshat.model.Confirmacion;
import org.seshat.repository.ConfirmacionRepository;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfirmacionService {
    private final ConfirmacionRepository repo;
    private final PadrinoRepository padrinoRepo;

    public ConfirmacionService(ConfirmacionRepository repo, PadrinoRepository padrinoRepo) {
        this.repo = repo;
        this.padrinoRepo = padrinoRepo;
    }

    public List<Confirmacion> listar() { return repo.findAll(); }
    public Confirmacion obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Confirmacion c) { return repo.save(c); }
    public void actualizar(Confirmacion c) { repo.update(c); }
    public void eliminar(int id) {
        padrinoRepo.eliminarConfirmacionPadrinosPorConfirmacion(id);
        repo.delete(id);
    }
}
```

- [ ] **Update MatrimonioService with PadrinoRepository dependency**

```java
package org.seshat.service;

import org.seshat.model.Matrimonio;
import org.seshat.repository.MatrimonioRepository;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatrimonioService {
    private final MatrimonioRepository repo;
    private final PadrinoRepository padrinoRepo;

    public MatrimonioService(MatrimonioRepository repo, PadrinoRepository padrinoRepo) {
        this.repo = repo;
        this.padrinoRepo = padrinoRepo;
    }

    public List<Matrimonio> listar() { return repo.findAll(); }
    public Matrimonio obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Matrimonio m) { return repo.save(m); }
    public void actualizar(Matrimonio m) { repo.update(m); }
    public void eliminar(int id) {
        padrinoRepo.eliminarMatrimonioPadrinosPorMatrimonio(id);
        repo.delete(id);
    }
}
```

- [ ] **Verify compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add src/main/java/org/seshat/service/BautizoService.java src/main/java/org/seshat/service/ConfirmacionService.java src/main/java/org/seshat/service/MatrimonioService.java
git commit -m "feat: clean up padrino join records on sacrament deletion"
```

---

### Task 4: Create PadrinoController

**Files:**
- Create: `src/main/java/org/seshat/controller/PadrinoController.java`

**Interfaces:**
- Consumes: `PadrinoService` methods from Task 2
- Produces: HTMX fragment HTML for padrinos section, consumed by sacrament form templates (Task 6)

- [ ] **Create PadrinoController.java**

```java
package org.seshat.controller;

import org.seshat.model.Padrino;
import org.seshat.service.PadrinoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/padrinos")
public class PadrinoController {
    private final PadrinoService padrinoService;

    public PadrinoController(PadrinoService padrinoService) {
        this.padrinoService = padrinoService;
    }

    @GetMapping("/fragmento/{tipo}/{id}")
    public String fragmento(@PathVariable String tipo, @PathVariable int id, Model model) {
        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, id);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), id));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", id);
        return "fragmentos/padrinos";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam String tipo, @RequestParam int entidadId,
                          @RequestParam String nombres, @RequestParam String apellidos,
                          @RequestParam(required = false) String rut,
                          @RequestParam String rol,
                          @RequestParam(required = false) String rolOtro,
                          Model model) {
        String rolFinal = "Otro".equals(rol) && rolOtro != null && !rolOtro.isBlank() ? rolOtro.trim() : rol;
        Map<String, String> errores = padrinoService.agregar(tipo, entidadId, nombres, apellidos, rut, rolFinal);

        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, entidadId);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), entidadId));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("erroresPadrino", errores);
        return "fragmentos/padrinos";
    }

    @DeleteMapping("/{id}/{tipo}/{sacramentoId}")
    public String eliminar(@PathVariable int id, @PathVariable String tipo, @PathVariable int sacramentoId, Model model) {
        padrinoService.eliminar(tipo, id, sacramentoId);

        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, sacramentoId);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), sacramentoId));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", sacramentoId);
        return "fragmentos/padrinos";
    }
}
```

- [ ] **Verify compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add src/main/java/org/seshat/controller/PadrinoController.java
git commit -m "feat: add PadrinoController with HTMX fragment endpoints"
```

---

### Task 5: Create Padrinos Fragment Template

**Files:**
- Create: `src/main/resources/templates/fragmentos/padrinos.html`

- [ ] **Create padrinos.html** — same pattern as certificados-section/fotos-section, with padrino list + add form

```html
<div class="padrinos-section col-span-2 mt-6 border-t pt-4" th:fragment="padrinos" xmlns:th="http://www.thymeleaf.org">
    <h3 class="text-lg font-bold text-granate mb-3">Padrinos</h3>
    <div th:if="${erroresPadrino != null and !erroresPadrino.isEmpty()}" class="bg-red-100 text-red-700 px-3 py-2 rounded-lg mb-3">
        <ul class="list-disc list-inside">
            <li th:each="err : ${erroresPadrino}" th:text="${err.value}"></li>
        </ul>
    </div>
    <div class="space-y-2 mb-4">
        <div th:each="padrino : ${padrinos}" class="flex items-center justify-between bg-gray-50 px-4 py-2 rounded-lg">
            <div>
                <span class="font-medium" th:text="${padrino.nombres} + ' ' + ${padrino.apellidos}"></span>
                <span th:if="${padrino.rut}" class="text-gray-500 text-sm ml-2" th:text="'(' + ${padrino.rut} + ')'"></span>
                <span class="inline-block bg-granate text-white text-xs px-2 py-0.5 rounded-full ml-2" th:text="${roles[padrino.id]}"></span>
            </div>
            <button th:attr="hx-delete=@{/padrinos/{id}/{tipo}/{sacramentoId}(id=${padrino.id}, tipo=${tipo}, sacramentoId=${entidadId})}"
                    hx-target="closest .padrinos-section" hx-swap="outerHTML"
                    hx-confirm="¿Eliminar este padrino?"
                    class="text-red-600 hover:text-red-800 text-sm font-medium">Eliminar</button>
        </div>
    </div>
    <div th:if="${#lists.isEmpty(padrinos)}" class="text-gray-400 text-sm italic mb-4">Sin padrinos registrados</div>
    <form th:attr="hx-post=@{/padrinos/agregar}" hx-target="closest .padrinos-section" hx-swap="outerHTML"
          class="grid grid-cols-2 md:grid-cols-4 gap-3 p-4 bg-gray-50 rounded-lg">
        <input type="hidden" name="tipo" th:value="${tipo}">
        <input type="hidden" name="entidadId" th:value="${entidadId}">
        <div>
            <label class="block text-xs text-gray-500 mb-1">Nombres</label>
            <input type="text" name="nombres" required
                   class="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-xs text-gray-500 mb-1">Apellidos</label>
            <input type="text" name="apellidos" required
                   class="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-xs text-gray-500 mb-1">RUT</label>
            <input type="text" name="rut" placeholder="XX.XXX.XXX-X"
                   class="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-xs text-gray-500 mb-1">Rol</label>
            <select name="rol" required
                    class="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate"
                    onchange="var el=this.parentElement.querySelector('input[name=rolOtro]');if(el)el.style.display=this.value==='Otro'?'block':'none'">
                <option value="">Seleccione</option>
                <option value="Padrino">Padrino</option>
                <option value="Madrina">Madrina</option>
                <option value="Testigo">Testigo</option>
                <option value="Otro">Otro...</option>
            </select>
            <input type="text" name="rolOtro" placeholder="Especifique rol"
                   style="display:none"
                   class="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:border-granate mt-1">
        </div>
        <div class="col-span-2 md:col-span-4 flex justify-end mt-2">
            <button type="submit" class="bg-cuero text-white px-6 py-1.5 rounded-lg hover:bg-cuero-light transition text-sm">+ Agregar Padrino</button>
        </div>
    </form>
</div>
```

- [ ] **Verify file exists**

- [ ] **Commit**

```bash
git add src/main/resources/templates/fragmentos/padrinos.html
git commit -m "feat: add padrinos fragment template"
```

---

### Task 6: Add Padrinos Section to Sacrament Form Templates

**Files:**
- Modify: `src/main/resources/templates/bautizos/formulario.html` — insert after `</form>` (line 50) before certificados section (line 51)
- Modify: `src/main/resources/templates/confirmaciones/formulario.html` — insert after `</form>` (line 44) before certificados (line 46)
- Modify: `src/main/resources/templates/matrimonios/formulario.html` — insert after `</form>` (line 58) before certificados (line 60)

- [ ] **Add padrinos section to bautizos/formulario.html** — between `</form>` and `<!-- certificados -->`:

```html
    <div th:if="${bautizo.id != 0}"
         th:attr="hx-get=@{/padrinos/fragmento/BAUTIZO/{id}(id=${bautizo.id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Add padrinos section to confirmaciones/formulario.html** — between `</form>` and certificados:

```html
    <div th:if="${confirmacion.id != 0}"
         th:attr="hx-get=@{/padrinos/fragmento/CONFIRMACION/{id}(id=${confirmacion.id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Add padrinos section to matrimonios/formulario.html** — between `</form>` and certificados:

```html
    <div th:if="${matrimonio.id != 0}"
         th:attr="hx-get=@{/padrinos/fragmento/MATRIMONIO/{id}(id=${matrimonio.id})}"
         hx-trigger="load"
         hx-target="this"
         hx-swap="outerHTML">
    </div>
```

- [ ] **Verify compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Verify runtime** — start the app (requires DB connection), then check:
  1. Edit an existing bautizo — padrinos section appears with "Sin padrinos registrados"
  2. Click "+ Agregar Padrino" — padrino appears in list
  3. Click "Eliminar" on a padrino — padrino is removed
  4. Same for confirmacion and matrimonio forms
  5. Delete a sacrament that has padrinos — succeeds without FK violation

- [ ] **Commit**

```bash
git add src/main/resources/templates/bautizos/formulario.html src/main/resources/templates/confirmaciones/formulario.html src/main/resources/templates/matrimonios/formulario.html
git commit -m "feat: add padrinos HTMX section to sacrament forms"
```
