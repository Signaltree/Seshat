# Seshat Dashboard Design

## Goal

Add a dashboard page with analytics (counts per year/month) for the parish sacraments registry. The user's mother needs to quickly see how many baptisms, confirmations, marriages, and registered people there are, with time-based filtering.

## Architecture

### Backend

**New files:**
- `DashboardController.java` — `GET /dashboard` endpoint, accepts query params `?anio=&mes=`
- `DashboardService.java` — query methods returning counts and yearly aggregations
- `StatsDTO.java` — simple POJO to hold stats data (or use `Map<String, Object>`)

**Queries:**

```sql
-- Total personas (all-time or filtered by fecha_registro)
SELECT COUNT(*) FROM PERSONA
-- Optionally WHERE EXTRACT(YEAR FROM fecha_registro) = :anio AND EXTRACT(MONTH FROM fecha_registro) = :mes

-- Per-sacrament counts (same pattern)
SELECT COUNT(*) FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = :anio AND EXTRACT(MONTH FROM fecha_bautizo) = :mes

-- Yearly breakdown for charts
SELECT EXTRACT(YEAR FROM fecha_bautizo) as anio, COUNT(*) as total
FROM BAUTIZO GROUP BY anio ORDER BY anio
```

When no filter is specified (`anio=0` or empty), return all-time totals. When month is specified but year is not, treat as invalid or default to current year.

### Frontend

**New template:** `templates/dashboard.html`

Uses Tailwind (CDN) + Chart.js (CDN).

**Layout:**
- Page title "Dashboard"
- Filter bar: year dropdown + month dropdown + "Todos" option
- 4 stat cards in a grid: Personas, Bautizos, Confirmaciones, Matrimonios
- Chart.js bar chart (grouped by year) showing all 4 sacraments per year

All wrapped in a Thymeleaf fragment so HTMX can reload just the stats area when filters change.

### Interaction

1. Page load: `GET /dashboard` with no filters → shows all-time totals + yearly chart
2. User changes year/month dropdown → HTMX sends `GET /dashboard?anio=2026&mes=6` to `/dashboard`, target `#dashboard-content`
3. Server re-renders the fragment with filtered data

## Implementation Plan

### Task 1: DashboardService + StatsDTO
- Methods: `contarPersonas()`, `contarBautizos()`, `contarMatrimonios()`, `contarConfirmaciones()`
- Methods accept optional `Integer anio`, `Integer mes`
- Method: `obtenerResumenAnual()` returns list of per-year aggregates for chart

### Task 2: DashboardController
- `GET /dashboard` with `@RequestParam(required=false) Integer anio, Integer mes`
- Passes stats + chart data to model
- Returns `dashboard/index` template

### Task 3: Dashboard template
- `src/main/resources/templates/dashboard/index.html`
- Stat cards grid
- Chart.js initialization with chart data passed from server
- HTMX form for filter dropdowns

### Task 4: Add dashboard link to sidebar
- In `index.html`, add a "📊 Dashboard" link in the sidebar nav

## Dependencies

- Chart.js via CDN (`https://cdn.jsdelivr.net/npm/chart.js`)
- No new Maven dependencies (HTMX + Thymeleaf + Spring already in place)
