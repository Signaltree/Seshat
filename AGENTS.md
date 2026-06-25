# Seshat — AGENTS.md

## Project

JavaFX + PostgreSQL + Maven desktop app for parish sacraments registry (Baptism, Confirmation, Marriage, First Communion). Parroquia María Misionera de Renca.

- **Java 21, Maven, JavaFX 21.0.2, PostgreSQL 42.7.3**
- **Main class:** `org.seshat.Main` (does NOT extend `Application` — currently only tests DB connection and exits)
- **No `module-info.java`** (classpath-based, not modular)

## Commands

```sh
mvn compile               # compiles (no tests configured)
mvn javafx:run            # tries to run Main (will test DB and exit — not a real GUI launch)
```

No test framework configured (JUnit not in `pom.xml`). No tests exist yet.

## Database

- **Environment variables required:** `DB_URL`, `DB_USER`, `DB_PASSWORD` (read by `DatabaseConnection.getConnection()`, no fallbacks)
- Schema in `src/main/resources/db/schema.sql` — 7 tables (`PERSONA`, `PADRINO`, `BAUTIZO`, `CONFIRMACION`, `MATRIMONIO`, plus 3 join tables for godparents)
- No `PRIMERA_COMUNION` table exists (controller/service/repo stub exists in Java but has no DB counterpart)

## State of the code

**17 of 24 Java files are empty stubs** (methods, no fields). Only working code:

| Layer | Files with real code | Stubs |
|-------|---------------------|-------|
| model | `Persona.java`, `Padrino.java` | `bautizo.java`, `confirmacion.java`, `matrimonio.java` |
| repository | — | all 5 (empty) |
| service | — | all 5 (empty) |
| ui | — | all 6 controllers (empty) |
| util | `DatabaseConnection.java` | — |

### Naming convention mess

Existing code uses inconsistent naming. Some classes have lowercase first letter:
- `bautizo.java`, `confirmacion.java`, `matrimonio.java`
- `bautizoRepository.java`, `personaRepository.java`, `primera_comunionRepository.java`, etc.
- `bautizoService.java`, `primera_comunionservice.java`, etc.

**Existing pattern in `Persona.java` uses `snake_case`** for fields: `fecha_nacimiento`, `fecha_registro`, etc. Follow this snake_case convention in model fields for consistency until told otherwise.

### FXML files

`src/main/resources/fxml/main.fxml` and `bautizo.fxml` are both **empty (0 bytes)** — not valid XML.

## Architecture

```
org.seshat
├── Main.java                        ← entry point (doesn't launch FX)
├── model/                           ← Java records/POJOs
├── repository/                      ← JDBC DAOs (all stubs)
├── service/                         ← business logic (all stubs)
├── ui/                              ← FX controllers (all stubs)
└── util/DatabaseConnection.java     ← env-var-based connection
```

## Missing / to-do

- [ ] Implement `model.bautizo`, `confirmacion`, `matrimonio`, `PrimeraComunion` (the last has no DB table yet)
- [ ] Fill all repository, service, and controller stubs
- [ ] Create FXML layouts
- [ ] Convert `Main.java` to extend `Application` and launch the GUI
- [ ] Add `PRIMERA_COMUNION` table to schema if kept
- [ ] Standardize naming (currently inconsistent)
