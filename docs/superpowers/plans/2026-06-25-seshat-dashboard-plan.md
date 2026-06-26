# Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a dashboard page with annual/monthly counts (baptisms, confirmations, marriages, people) with Chart.js bar charts and HTMX-driven filter controls.

**Architecture:** DashboardController receives `?anio=&mes=` query params, DashboardService runs PostgreSQL aggregate queries, returns stats to Thymeleaf template with Chart.js for bar charts.

**Tech Stack:** Java 21, Spring Boot 3.2.5, Thymeleaf, HTMX, Chart.js (CDN), Tailwind (CDN)

## Global Constraints

- All new endpoints under `/dashboard` prefix
- Chart.js loaded from CDN (no npm)
- Filter dropdowns use HTMX to reload only the stats section (no full page reload)
- Template uses same Tailwind color palette: granate #5B2C3E, cuero #8B4513, dorado #C9A84C, crema #F5F0EB
- No new Maven dependencies

---

### Task 1: DashboardService + StatsDTO

**Files:**
- Create: `src/main/java/org/seshat/dto/StatsDashboard.java`
- Create: `src/main/java/org/seshat/service/DashboardService.java`

**Interfaces:**
- Produces: `DashboardService` with methods `contarPersonas(Integer anio, Integer mes)`, `contarBautizos(...)`, `contarConfirmaciones(...)`, `contarMatrimonios(...)` all return `long`; `obtenerResumenAnual()` returns `List<Map<String, Object>>` with columns `anio`, `bautizos`, `confirmaciones`, `matrimonios`

- [ ] **Step 1: Create StatsDashboard DTO**

Write `src/main/java/org/seshat/dto/StatsDashboard.java`:
```java
package org.seshat.dto;

import java.util.List;
import java.util.Map;

public class StatsDashboard {
    private long totalPersonas;
    private long totalBautizos;
    private long totalConfirmaciones;
    private long totalMatrimonios;
    private List<Map<String, Object>> resumenAnual;

    public StatsDashboard() {}

    public long getTotalPersonas() { return totalPersonas; }
    public void setTotalPersonas(long v) { totalPersonas = v; }
    public long getTotalBautizos() { return totalBautizos; }
    public void setTotalBautizos(long v) { totalBautizos = v; }
    public long getTotalConfirmaciones() { return totalConfirmaciones; }
    public void setTotalConfirmaciones(long v) { totalConfirmaciones = v; }
    public long getTotalMatrimonios() { return totalMatrimonios; }
    public void setTotalMatrimonios(long v) { totalMatrimonios = v; }
    public List<Map<String, Object>> getResumenAnual() { return resumenAnual; }
    public void setResumenAnual(List<Map<String, Object>> v) { resumenAnual = v; }
}
```

- [ ] **Step 2: Create DashboardService**

Write `src/main/java/org/seshat/service/DashboardService.java`:
```java
package org.seshat.service;

import org.seshat.dto.StatsDashboard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StatsDashboard obtenerStats(Integer anio, Integer mes) {
        StatsDashboard s = new StatsDashboard();
        s.setTotalPersonas(contar("PERSONA", "fecha_registro", anio, mes));
        s.setTotalBautizos(contar("BAUTIZO", "fecha_bautizo", anio, mes));
        s.setTotalConfirmaciones(contar("CONFIRMACION", "fecha_confirmacion", anio, mes));
        s.setTotalMatrimonios(contar("MATRIMONIO", "fecha_matrimonio", anio, mes));
        s.setResumenAnual(obtenerResumenAnual());
        return s;
    }

    private long contar(String tabla, String columna, Integer anio, Integer mes) {
        if (anio == null) return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Long.class);
        if (mes == null) return jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + tabla + " WHERE EXTRACT(YEAR FROM " + columna + ") = ?", Long.class, anio);
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + tabla + " WHERE EXTRACT(YEAR FROM " + columna + ") = ? AND EXTRACT(MONTH FROM " + columna + ") = ?",
            Long.class, anio, mes);
    }

    private List<Map<String, Object>> obtenerResumenAnual() {
        return jdbc.queryForList("""
            SELECT
                COALESCE(b.anio, c.anio, m.anio) as anio,
                COALESCE(b.total, 0) as bautizos,
                COALESCE(c.total, 0) as confirmaciones,
                COALESCE(m.total, 0) as matrimonios
            FROM (SELECT EXTRACT(YEAR FROM fecha_bautizo) as anio, COUNT(*) as total FROM BAUTIZO GROUP BY anio) b
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_confirmacion) as anio, COUNT(*) as total FROM CONFIRMACION GROUP BY anio) c ON b.anio = c.anio
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_matrimonio) as anio, COUNT(*) as total FROM MATRIMONIO GROUP BY anio) m ON COALESCE(b.anio, c.anio) = m.anio
            ORDER BY anio
            """);
    }

    public List<Integer> obtenerAniosDisponibles() {
        return jdbc.queryForList("""
            SELECT DISTINCT EXTRACT(YEAR FROM fecha) as anio FROM (
                SELECT fecha_bautizo as fecha FROM BAUTIZO
                UNION
                SELECT fecha_confirmacion FROM CONFIRMACION
                UNION
                SELECT fecha_matrimonio FROM MATRIMONIO
                UNION
                SELECT fecha_registro FROM PERSONA
            ) todas ORDER BY anio DESC
            """, Integer.class);
    }
}
```

- [ ] **Step 3: Compile**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/seshat/dto/StatsDashboard.java src/main/java/org/seshat/service/DashboardService.java
git commit -m "feat: add DashboardService and StatsDashboard DTO"
```

---

### Task 2: DashboardController

**Files:**
- Create: `src/main/java/org/seshat/controller/DashboardController.java`

**Interfaces:**
- Consumes: `DashboardService.obtenerStats(anio, mes)`, `DashboardService.obtenerAniosDisponibles()`
- Produces: `GET /dashboard` returning `dashboard/index` template with model attributes: `stats` (StatsDashboard), `anio` (Integer), `mes` (Integer), `anios` (List<Integer>)

- [ ] **Step 1: Create DashboardController**

Write `src/main/java/org/seshat/controller/DashboardController.java`:
```java
package org.seshat.controller;

import org.seshat.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) Integer mes,
                            Model model) {
        model.addAttribute("stats", service.obtenerStats(anio, mes));
        model.addAttribute("anio", anio);
        model.addAttribute("mes", mes);
        model.addAttribute("anios", service.obtenerAniosDisponibles());
        return "dashboard/index";
    }
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/seshat/controller/DashboardController.java
git commit -m "feat: add DashboardController"
```

---

### Task 3: Dashboard Template

**Files:**
- Create: `src/main/resources/templates/dashboard/index.html`

- [ ] **Step 1: Create the Thymeleaf template**

Write `src/main/resources/templates/dashboard/index.html`:
```html
<div th:fragment="dashboard" class="max-w-5xl mx-auto">
    <h2 class="text-2xl font-bold text-granate mb-6">Dashboard</h2>

    <!-- Filtros -->
    <form hx-get="/dashboard" hx-target="#dashboard-content" class="flex gap-4 mb-6 items-end">
        <div>
            <label class="block text-gray-700 text-sm font-medium mb-1">Año</label>
            <select name="anio" class="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate"
                    hx-get="/dashboard" hx-target="#dashboard-content" hx-trigger="change">
                <option value="">Todos</option>
                <option th:each="a : ${anios}" th:value="${a}" th:text="${a}" th:selected="${a == anio}"></option>
            </select>
        </div>
        <div>
            <label class="block text-gray-700 text-sm font-medium mb-1">Mes</label>
            <select name="mes" class="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate"
                    hx-get="/dashboard" hx-target="#dashboard-content" hx-trigger="change">
                <option value="">Todos</option>
                <option value="1" th:selected="${mes == 1}">Enero</option>
                <option value="2" th:selected="${mes == 2}">Febrero</option>
                <option value="3" th:selected="${mes == 3}">Marzo</option>
                <option value="4" th:selected="${mes == 4}">Abril</option>
                <option value="5" th:selected="${mes == 5}">Mayo</option>
                <option value="6" th:selected="${mes == 6}">Junio</option>
                <option value="7" th:selected="${mes == 7}">Julio</option>
                <option value="8" th:selected="${mes == 8}">Agosto</option>
                <option value="9" th:selected="${mes == 9}">Septiembre</option>
                <option value="10" th:selected="${mes == 10}">Octubre</option>
                <option value="11" th:selected="${mes == 11}">Noviembre</option>
                <option value="12" th:selected="${mes == 12}">Diciembre</option>
            </select>
        </div>
    </form>

    <!-- Tarjetas -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div class="bg-white rounded-xl shadow-md p-6 text-center">
            <p class="text-4xl font-bold text-granate" th:text="${stats.totalPersonas}">0</p>
            <p class="text-gray-600 mt-1">Personas</p>
        </div>
        <div class="bg-white rounded-xl shadow-md p-6 text-center">
            <p class="text-4xl font-bold text-cuero" th:text="${stats.totalBautizos}">0</p>
            <p class="text-gray-600 mt-1">Bautizos</p>
        </div>
        <div class="bg-white rounded-xl shadow-md p-6 text-center">
            <p class="text-4xl font-bold text-dorado" th:text="${stats.totalConfirmaciones}">0</p>
            <p class="text-gray-600 mt-1">Confirmaciones</p>
        </div>
        <div class="bg-white rounded-xl shadow-md p-6 text-center">
            <p class="text-4xl font-bold text-granate-dark" th:text="${stats.totalMatrimonios}">0</p>
            <p class="text-gray-600 mt-1">Matrimonios</p>
        </div>
    </div>

    <!-- Gráfico -->
    <div class="bg-white rounded-xl shadow-md p-6">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">Evolución Anual</h3>
        <canvas id="chart" height="100"></canvas>
    </div>

    <script>
        var datos = [[${stats.resumenAnual}]];
        var el = document.getElementById('chart');
        if (window._chart) window._chart.destroy();
        window._chart = new Chart(el.getContext('2d'), {
            type: 'bar',
            data: {
                labels: datos.map(function(r) { return r.anio; }),
                datasets: [
                    { label: 'Bautizos', data: datos.map(function(r) { return r.bautizos; }), backgroundColor: '#8B4513' },
                    { label: 'Confirmaciones', data: datos.map(function(r) { return r.confirmaciones; }), backgroundColor: '#C9A84C' },
                    { label: 'Matrimonios', data: datos.map(function(r) { return r.matrimonios; }), backgroundColor: '#5B2C3E' }
                ]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'top' } },
                scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
            }
        });
    </script>
</div>
```

- [ ] **Step 2: Compile**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/dashboard/index.html
git commit -m "feat: add dashboard template with Chart.js"
```

---

### Task 4: Add Dashboard Link to Sidebar + Chart.js CDN

**Files:**
- Modify: `src/main/resources/templates/index.html`

- [ ] **Step 1: Add Chart.js CDN and dashboard link**

Edit `src/main/resources/templates/index.html`:

Add Chart.js CDN after HTMX script:
```html
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
```

Add dashboard nav link before Personas:
```html
            <a href="/dashboard" hx-get="/dashboard" hx-target="#main-content"
               class="block px-4 py-3 rounded-lg bg-granate-light hover:bg-granate-dark transition text-white text-lg font-medium text-center">
                📊 Dashboard
            </a>
```

- [ ] **Step 2: Compile**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/index.html
git commit -m "feat: add dashboard link and Chart.js CDN"
```
