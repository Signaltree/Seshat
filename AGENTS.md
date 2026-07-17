# Seshat — AGENTS.md

## Project

Spring Boot + Thymeleaf + HTMX + Tailwind (CDN) web app for parish sacraments registry (Baptism, Confirmation, Marriage). Parroquia María Misionera de Renca.

- **Java 21, Spring Boot 3.2.5, Thymeleaf, PostgreSQL 42.7.3**
- **Main class:** `org.seshat.SeshatApplication`

## Commands

```sh
mvn compile               # compiles
mvn spring-boot:run       # runs the web app
```

No test framework configured (JUnit not in `pom.xml`). No tests exist yet.

## Database

- **Environment variables required:** `DB_URL`, `DB_USER`, `DB_PASSWORD` (read by `application.properties`, no fallbacks)
- Schema in `src/main/resources/db/schema.sql` — 5 tables (`PERSONA`, `PADRINO`, `BAUTIZO`, `CONFIRMACION`, `MATRIMONIO`, plus 3 join tables for godparents)

## Auth

- **Spring Security** with form login
- Default credentials: `admin` / `admin123`
- Override via env vars: `ADMIN_USER`, `ADMIN_PASS`
- CSRF disabled (single-user app)

## Validation

- RUT validation with check digit (formato: XX.XXX.XXX-X)
- Email format validation
- Chilean phone format validation (+56 9 XXXX XXXX)
- All in `org.seshat.util.ValidacionUtil`

## Skills Discovery (vercel-labs/skills ecosystem)

Use `npx skills` CLI to discover, evaluate, and install agent skills.

### How to use

```sh
npx skills find <query>             # search skills by keyword
npx skills find <query> --owner <owner>  # scope to a GitHub owner
npx skills add <owner/repo@skill> -g -y  # install globally (copies SKILL.md)
npx skills list -g                  # list installed global skills
npx skills update                   # update all installed skills
```

### When to use

- User says "find a skill for X" or "can you do X"
- User asks "how do I do X" where X might be a common task
- Before implementing a complex feature (check if a skill exists first)
- To extend agent capabilities for specific domains

### Installed skills (from this ecosystem)

| Skill | Install | Purpose |
|-------|---------|---------|
| `find-skills` | 2.5M | Meta-skill: discover & install other skills |
| `java-springboot` | 18K | Java/Spring Boot development patterns |
| `postgresql-code-review` | 11.2K | PostgreSQL schema & query review |
| `documentation-and-adrs` | 13.1K | Documentation & ADR writing |

### Verification before recommending

1. Check install count (prefer 1K+, cautious under 100)
2. Check source reputation (vercel-labs, anthropics, github trusted)
3. Check skills.sh page before suggesting installation

## Architecture

```
org.seshat
├── SeshatApplication.java           ← entry point (Spring Boot)
├── config/SecurityConfig.java       ← Spring Security config
├── model/                           ← POJO model classes
├── repository/                      ← JdbcTemplate repositories
├── service/                         ← business logic + validation
├── controller/                      ← Spring MVC controllers
└── util/ValidacionUtil.java         ← RUT, email, phone validators
```
