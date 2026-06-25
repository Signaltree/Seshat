# Seshat: Migración a Spring Boot + Thymeleaf + HTMX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate from JavaFX desktop app to Spring Boot web app with modern UI

**Architecture:** Spring Boot 3 + Thymeleaf server-side templates + HTMX for interactivity + Tailwind CSS CDN for styling. PostgreSQL via JdbcTemplate.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring JDBC (JdbcTemplate), Thymeleaf, HTMX (CDN), Tailwind CSS (Play CDN), PostgreSQL, Maven

## Global Constraints

- Java 21, Maven build
- No JavaScript build tooling (npm, webpack, vite — zero)
- All frontend interactivity via HTMX attributes in HTML
- Tailwind via Play CDN (`<script src="https://cdn.tailwindcss.com">`)
- Environment variables for DB: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Schema in `src/main/resources/db/schema.sql` (unchanged)
- Model field names use `snake_case` (matching existing `Persona.java`)

---

### Task 1: Foundation — pom.xml + SeshatApplication + application.properties

**Files:**
- Rewrite: `pom.xml`
- Create: `src/main/java/org/seshat/SeshatApplication.java`
- Create: `src/main/resources/application.properties`
- Delete: `src/main/java/org/seshat/Main.java`
- Delete: `src/main/java/org/seshat/util/DatabaseConnection.java`

**Interfaces:**
- Produces: `SeshatApplication.main(String[])` — entry point, callable via `mvn spring-boot:run` or `java -jar`

- [ ] **Step 1: Rewrite pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>org.example</groupId>
    <artifactId>Seshat</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Create SeshatApplication.java**

```java
package org.seshat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeshatApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeshatApplication.class, args);
    }
}
```

- [ ] **Step 3: Create application.properties**

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
server.port=8080
```

- [ ] **Step 4: Delete old entry point and DB utility**

Run:
```
rm src/main/java/org/seshat/Main.java
rm src/main/java/org/seshat/util/DatabaseConnection.java
```

- [ ] **Step 5: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "feat: migrate to Spring Boot web app"
```

---

### Task 2: Models — Bautizo, Confirmacion, Matrimonio

**Files:**
- Create: `src/main/java/org/seshat/model/Bautizo.java`
- Create: `src/main/java/org/seshat/model/Confirmacion.java`
- Create: `src/main/java/org/seshat/model/Matrimonio.java`
- Delete: `src/main/java/org/seshat/model/bautizo.java`
- Delete: `src/main/java/org/seshat/model/confirmacion.java`
- Delete: `src/main/java/org/seshat/model/matrimonio.java`

**Interfaces:**
- Produces: `model.Bautizo`, `model.Confirmacion`, `model.Matrimonio` POJOs with fields matching DB columns

- [ ] **Step 1: Create Bautizo.java**

```java
package org.seshat.model;

import java.time.LocalDate;

public class Bautizo {
    private int id;
    private int persona_id;
    private String padre;
    private String madre;
    private LocalDate fecha_bautizo;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;

    public Bautizo() {}

    public Bautizo(int id, int persona_id, String padre, String madre, LocalDate fecha_bautizo,
                   String n_libro, String n_folio, String parroquia, String ruta_imagen) {
        this.id = id;
        this.persona_id = persona_id;
        this.padre = padre;
        this.madre = madre;
        this.fecha_bautizo = fecha_bautizo;
        this.n_libro = n_libro;
        this.n_folio = n_folio;
        this.parroquia = parroquia;
        this.ruta_imagen = ruta_imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersona_id() { return persona_id; }
    public void setPersona_id(int persona_id) { this.persona_id = persona_id; }
    public String getPadre() { return padre; }
    public void setPadre(String padre) { this.padre = padre; }
    public String getMadre() { return madre; }
    public void setMadre(String madre) { this.madre = madre; }
    public LocalDate getFecha_bautizo() { return fecha_bautizo; }
    public void setFecha_bautizo(LocalDate fecha_bautizo) { this.fecha_bautizo = fecha_bautizo; }
    public String getN_libro() { return n_libro; }
    public void setN_libro(String n_libro) { this.n_libro = n_libro; }
    public String getN_folio() { return n_folio; }
    public void setN_folio(String n_folio) { this.n_folio = n_folio; }
    public String getParroquia() { return parroquia; }
    public void setParroquia(String parroquia) { this.parroquia = parroquia; }
    public String getRuta_imagen() { return ruta_imagen; }
    public void setRuta_imagen(String ruta_imagen) { this.ruta_imagen = ruta_imagen; }
}
```

- [ ] **Step 2: Create Confirmacion.java**

```java
package org.seshat.model;

import java.time.LocalDate;

public class Confirmacion {
    private int id;
    private int persona_id;
    private LocalDate fecha_confirmacion;
    private String guia;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;

    public Confirmacion() {}

    public Confirmacion(int id, int persona_id, LocalDate fecha_confirmacion, String guia,
                        String n_libro, String n_folio, String parroquia, String ruta_imagen) {
        this.id = id;
        this.persona_id = persona_id;
        this.fecha_confirmacion = fecha_confirmacion;
        this.guia = guia;
        this.n_libro = n_libro;
        this.n_folio = n_folio;
        this.parroquia = parroquia;
        this.ruta_imagen = ruta_imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersona_id() { return persona_id; }
    public void setPersona_id(int persona_id) { this.persona_id = persona_id; }
    public LocalDate getFecha_confirmacion() { return fecha_confirmacion; }
    public void setFecha_confirmacion(LocalDate fecha_confirmacion) { this.fecha_confirmacion = fecha_confirmacion; }
    public String getGuia() { return guia; }
    public void setGuia(String guia) { this.guia = guia; }
    public String getN_libro() { return n_libro; }
    public void setN_libro(String n_libro) { this.n_libro = n_libro; }
    public String getN_folio() { return n_folio; }
    public void setN_folio(String n_folio) { this.n_folio = n_folio; }
    public String getParroquia() { return parroquia; }
    public void setParroquia(String parroquia) { this.parroquia = parroquia; }
    public String getRuta_imagen() { return ruta_imagen; }
    public void setRuta_imagen(String ruta_imagen) { this.ruta_imagen = ruta_imagen; }
}
```

- [ ] **Step 3: Create Matrimonio.java**

```java
package org.seshat.model;

import java.time.LocalDate;

public class Matrimonio {
    private int id;
    private int persona1_id;
    private int persona2_id;
    private String sacerdote;
    private LocalDate fecha_matrimonio;
    private String direccion;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;

    public Matrimonio() {}

    public Matrimonio(int id, int persona1_id, int persona2_id, String sacerdote,
                      LocalDate fecha_matrimonio, String direccion, String n_libro,
                      String n_folio, String parroquia, String ruta_imagen) {
        this.id = id;
        this.persona1_id = persona1_id;
        this.persona2_id = persona2_id;
        this.sacerdote = sacerdote;
        this.fecha_matrimonio = fecha_matrimonio;
        this.direccion = direccion;
        this.n_libro = n_libro;
        this.n_folio = n_folio;
        this.parroquia = parroquia;
        this.ruta_imagen = ruta_imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersona1_id() { return persona1_id; }
    public void setPersona1_id(int persona1_id) { this.persona1_id = persona1_id; }
    public int getPersona2_id() { return persona2_id; }
    public void setPersona2_id(int persona2_id) { this.persona2_id = persona2_id; }
    public String getSacerdote() { return sacerdote; }
    public void setSacerdote(String sacerdote) { this.sacerdote = sacerdote; }
    public LocalDate getFecha_matrimonio() { return fecha_matrimonio; }
    public void setFecha_matrimonio(LocalDate fecha_matrimonio) { this.fecha_matrimonio = fecha_matrimonio; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getN_libro() { return n_libro; }
    public void setN_libro(String n_libro) { this.n_libro = n_libro; }
    public String getN_folio() { return n_folio; }
    public void setN_folio(String n_folio) { this.n_folio = n_folio; }
    public String getParroquia() { return parroquia; }
    public void setParroquia(String parroquia) { this.parroquia = parroquia; }
    public String getRuta_imagen() { return ruta_imagen; }
    public void setRuta_imagen(String ruta_imagen) { this.ruta_imagen = ruta_imagen; }
}
```

- [ ] **Step 4: Delete old stub files**

Run:
```
rm src/main/java/org/seshat/model/bautizo.java
rm src/main/java/org/seshat/model/confirmacion.java
rm src/main/java/org/seshat/model/matrimonio.java
```

- [ ] **Step 5: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS, no warnings about bautizo/confirmacion/matrimonio

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "feat: add Bautizo, Confirmacion, Matrimonio models"
```

---

### Task 3: Repositories — JdbcTemplate implementations

**Files:**
- Create: `src/main/java/org/seshat/repository/PersonaRepository.java`
- Create: `src/main/java/org/seshat/repository/PadrinoRepository.java`
- Create: `src/main/java/org/seshat/repository/BautizoRepository.java`
- Create: `src/main/java/org/seshat/repository/ConfirmacionRepository.java`
- Create: `src/main/java/org/seshat/repository/MatrimonioRepository.java`
- Delete: `src/main/java/org/seshat/repository/personaRepository.java`
- Delete: `src/main/java/org/seshat/repository/bautizoRepository.java`
- Delete: `src/main/java/org/seshat/repository/confirmacionRepository.java`
- Delete: `src/main/java/org/seshat/repository/matrimonioRepository.java`
- Delete: `src/main/java/org/seshat/repository/primera_comunionRepository.java`

**Interfaces:**
- Consumes: `model.Persona`, `model.Padrino`, `model.Bautizo`, `model.Confirmacion`, `model.Matrimonio`, `org.springframework.jdbc.core.JdbcTemplate`
- Produces: `PersonaRepository`, `PadrinoRepository`, `BautizoRepository`, `ConfirmacionRepository`, `MatrimonioRepository` — each with `findAll()`, `findById(int)`, `save(T)`, `update(T)`, `delete(int)` where T is the entity type.

- [ ] **Step 1: Create PersonaRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Persona;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PersonaRepository {
    private final JdbcTemplate jdbc;

    public PersonaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Persona> mapper = (rs, rowNum) -> new Persona(
            rs.getInt("id"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("rut"),
            rs.getString("email"),
            rs.getString("direccion"),
            rs.getString("telefono"),
            rs.getDate("fecha_registro").toLocalDate(),
            rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null
    );

    public List<Persona> findAll() {
        return jdbc.query("SELECT * FROM PERSONA ORDER BY apellidos, nombres", mapper);
    }

    public Persona findById(int id) {
        return jdbc.queryForObject("SELECT * FROM PERSONA WHERE id = ?", mapper, id);
    }

    public int save(Persona p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PERSONA (nombres, apellidos, rut, fecha_nacimiento, direccion, telefono, email, fecha_registro) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getRut());
            ps.setObject(4, p.getFecha_nacimiento());
            ps.setString(5, p.getDireccion());
            ps.setString(6, p.getTelefono());
            ps.setString(7, p.getEmail());
            ps.setObject(8, p.getFecha_registro());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Persona p) {
        jdbc.update("UPDATE PERSONA SET nombres=?, apellidos=?, rut=?, fecha_nacimiento=?, direccion=?, telefono=?, email=? WHERE id=?",
                p.getNombres(), p.getApellidos(), p.getRut(), p.getFecha_nacimiento(),
                p.getDireccion(), p.getTelefono(), p.getEmail(), p.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM PERSONA WHERE id=?", id);
    }
}
```

- [ ] **Step 2: Create PadrinoRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Padrino;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
                    Statement.RETURN_GENERATED_KEYS);
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
}
```

- [ ] **Step 3: Create BautizoRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Bautizo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class BautizoRepository {
    private final JdbcTemplate jdbc;

    public BautizoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Bautizo> mapper = (rs, rowNum) -> new Bautizo(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("padre"),
            rs.getString("madre"),
            rs.getDate("fecha_bautizo").toLocalDate(),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Bautizo> findAll() {
        return jdbc.query("SELECT * FROM BAUTIZO ORDER BY fecha_bautizo DESC", mapper);
    }

    public Bautizo findById(int id) {
        return jdbc.queryForObject("SELECT * FROM BAUTIZO WHERE id = ?", mapper, id);
    }

    public int save(Bautizo b) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO BAUTIZO (persona_id, padre, madre, fecha_bautizo, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, b.getPersona_id());
            ps.setString(2, b.getPadre());
            ps.setString(3, b.getMadre());
            ps.setObject(4, b.getFecha_bautizo());
            ps.setString(5, b.getN_libro());
            ps.setString(6, b.getN_folio());
            ps.setString(7, b.getParroquia());
            ps.setString(8, b.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Bautizo b) {
        jdbc.update("UPDATE BAUTIZO SET persona_id=?, padre=?, madre=?, fecha_bautizo=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                b.getPersona_id(), b.getPadre(), b.getMadre(), b.getFecha_bautizo(),
                b.getN_libro(), b.getN_folio(), b.getParroquia(), b.getRuta_imagen(), b.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM BAUTIZO WHERE id=?", id);
    }
}
```

- [ ] **Step 4: Create ConfirmacionRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Confirmacion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ConfirmacionRepository {
    private final JdbcTemplate jdbc;

    public ConfirmacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Confirmacion> mapper = (rs, rowNum) -> new Confirmacion(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getDate("fecha_confirmacion").toLocalDate(),
            rs.getString("guia"),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Confirmacion> findAll() {
        return jdbc.query("SELECT * FROM CONFIRMACION ORDER BY fecha_confirmacion DESC", mapper);
    }

    public Confirmacion findById(int id) {
        return jdbc.queryForObject("SELECT * FROM CONFIRMACION WHERE id = ?", mapper, id);
    }

    public int save(Confirmacion c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CONFIRMACION (persona_id, fecha_confirmacion, guia, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, c.getPersona_id());
            ps.setObject(2, c.getFecha_confirmacion());
            ps.setString(3, c.getGuia());
            ps.setString(4, c.getN_libro());
            ps.setString(5, c.getN_folio());
            ps.setString(6, c.getParroquia());
            ps.setString(7, c.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Confirmacion c) {
        jdbc.update("UPDATE CONFIRMACION SET persona_id=?, fecha_confirmacion=?, guia=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                c.getPersona_id(), c.getFecha_confirmacion(), c.getGuia(),
                c.getN_libro(), c.getN_folio(), c.getParroquia(), c.getRuta_imagen(), c.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM CONFIRMACION WHERE id=?", id);
    }
}
```

- [ ] **Step 5: Create MatrimonioRepository.java**

```java
package org.seshat.repository;

import org.seshat.model.Matrimonio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class MatrimonioRepository {
    private final JdbcTemplate jdbc;

    public MatrimonioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Matrimonio> mapper = (rs, rowNum) -> new Matrimonio(
            rs.getInt("id"),
            rs.getInt("persona1_id"),
            rs.getInt("persona2_id"),
            rs.getString("sacerdote"),
            rs.getDate("fecha_matrimonio").toLocalDate(),
            rs.getString("direccion"),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Matrimonio> findAll() {
        return jdbc.query("SELECT * FROM MATRIMONIO ORDER BY fecha_matrimonio DESC", mapper);
    }

    public Matrimonio findById(int id) {
        return jdbc.queryForObject("SELECT * FROM MATRIMONIO WHERE id = ?", mapper, id);
    }

    public int save(Matrimonio m) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO MATRIMONIO (persona1_id, persona2_id, sacerdote, fecha_matrimonio, direccion, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, m.getPersona1_id());
            ps.setInt(2, m.getPersona2_id());
            ps.setString(3, m.getSacerdote());
            ps.setObject(4, m.getFecha_matrimonio());
            ps.setString(5, m.getDireccion());
            ps.setString(6, m.getN_libro());
            ps.setString(7, m.getN_folio());
            ps.setString(8, m.getParroquia());
            ps.setString(9, m.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Matrimonio m) {
        jdbc.update("UPDATE MATRIMONIO SET persona1_id=?, persona2_id=?, sacerdote=?, fecha_matrimonio=?, direccion=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                m.getPersona1_id(), m.getPersona2_id(), m.getSacerdote(), m.getFecha_matrimonio(),
                m.getDireccion(), m.getN_libro(), m.getN_folio(), m.getParroquia(), m.getRuta_imagen(), m.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM MATRIMONIO WHERE id=?", id);
    }
}
```

- [ ] **Step 6: Delete old repository stubs**

Run:
```
rm src/main/java/org/seshat/repository/personaRepository.java
rm src/main/java/org/seshat/repository/bautizoRepository.java
rm src/main/java/org/seshat/repository/confirmacionRepository.java
rm src/main/java/org/seshat/repository/matrimonioRepository.java
rm src/main/java/org/seshat/repository/primera_comunionRepository.java
```

- [ ] **Step 7: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```
git add -A && git commit -m "feat: add JdbcTemplate repositories for all entities"
```

---

### Task 4: Services — @Service layer

**Files:**
- Rewrite: `src/main/java/org/seshat/service/PersonaService.java`
- Create: `src/main/java/org/seshat/service/BautizoService.java`
- Create: `src/main/java/org/seshat/service/ConfirmacionService.java`
- Create: `src/main/java/org/seshat/service/MatrimonioService.java`
- Delete: `src/main/java/org/seshat/service/bautizoService.java`
- Delete: `src/main/java/org/seshat/service/confirmacionService.java`
- Delete: `src/main/java/org/seshat/service/matrimonioService.java`
- Delete: `src/main/java/org/seshat/service/primera_comunionservice.java`

**Interfaces:**
- Consumes: all repositories
- Produces: `PersonaService`, `BautizoService`, `ConfirmacionService`, `MatrimonioService` — each with `listar()`, `obtenerPorId(int)`, `guardar(T)`, `actualizar(T)`, `eliminar(int)`

- [ ] **Step 1: Create PersonaService.java**

```java
package org.seshat.service;

import org.seshat.model.Persona;
import org.seshat.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PersonaService {
    private final PersonaRepository repo;

    public PersonaService(PersonaRepository repo) {
        this.repo = repo;
    }

    public List<Persona> listar() { return repo.findAll(); }

    public Persona obtenerPorId(int id) { return repo.findById(id); }

    public int guardar(Persona p) {
        if (p.getFecha_registro() == null) p.setFecha_registro(LocalDate.now());
        return repo.save(p);
    }

    public void actualizar(Persona p) { repo.update(p); }

    public void eliminar(int id) { repo.delete(id); }
}
```

- [ ] **Step 2: Create BautizoService.java**

```java
package org.seshat.service;

import org.seshat.model.Bautizo;
import org.seshat.repository.BautizoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BautizoService {
    private final BautizoRepository repo;

    public BautizoService(BautizoRepository repo) { this.repo = repo; }

    public List<Bautizo> listar() { return repo.findAll(); }
    public Bautizo obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Bautizo b) { return repo.save(b); }
    public void actualizar(Bautizo b) { repo.update(b); }
    public void eliminar(int id) { repo.delete(id); }
}
```

- [ ] **Step 3: Create ConfirmacionService.java**

```java
package org.seshat.service;

import org.seshat.model.Confirmacion;
import org.seshat.repository.ConfirmacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfirmacionService {
    private final ConfirmacionRepository repo;

    public ConfirmacionService(ConfirmacionRepository repo) { this.repo = repo; }

    public List<Confirmacion> listar() { return repo.findAll(); }
    public Confirmacion obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Confirmacion c) { return repo.save(c); }
    public void actualizar(Confirmacion c) { repo.update(c); }
    public void eliminar(int id) { repo.delete(id); }
}
```

- [ ] **Step 4: Create MatrimonioService.java**

```java
package org.seshat.service;

import org.seshat.model.Matrimonio;
import org.seshat.repository.MatrimonioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatrimonioService {
    private final MatrimonioRepository repo;

    public MatrimonioService(MatrimonioRepository repo) { this.repo = repo; }

    public List<Matrimonio> listar() { return repo.findAll(); }
    public Matrimonio obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Matrimonio m) { return repo.save(m); }
    public void actualizar(Matrimonio m) { repo.update(m); }
    public void eliminar(int id) { repo.delete(id); }
}
```

- [ ] **Step 5: Delete old service stubs**

Run:
```
rm src/main/java/org/seshat/service/bautizoService.java
rm src/main/java/org/seshat/service/confirmacionService.java
rm src/main/java/org/seshat/service/matrimonioService.java
rm src/main/java/org/seshat/service/primera_comunionservice.java
```

- [ ] **Step 6: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```
git add -A && git commit -m "feat: add service layer for all entities"
```

---

### Task 5: Web Controllers — Personas CRUD

**Files:**
- Create: `src/main/java/org/seshat/controller/HomeController.java`
- Create: `src/main/java/org/seshat/controller/PersonaController.java`

**Interfaces:**
- Consumes: `PersonaService`
- Produces: HTTP endpoints: `GET /` (index), `GET /personas` (list fragment), `GET /personas/nuevo` (form), `POST /personas` (save), `GET /personas/{id}/editar` (form), `PUT /personas/{id}` (update), `DELETE /personas/{id}`

- [ ] **Step 1: Create HomeController.java**

```java
package org.seshat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() { return "index"; }
}
```

- [ ] **Step 2: Create PersonaController.java**

```java
package org.seshat.controller;

import org.seshat.model.Persona;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/personas")
public class PersonaController {
    private final PersonaService service;

    public PersonaController(PersonaService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("persona", new Persona());
        return "personas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Persona p, Model model) {
        if (id > 0) { p.setId(id); service.actualizar(p); }
        else service.guardar(p);
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("persona", service.obtenerPorId(id));
        return "personas/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        service.eliminar(id);
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
git add -A && git commit -m "feat: add HomeController and PersonaController"
```

---

### Task 6: Web Controllers — Sacramentos CRUD

**Files:**
- Create: `src/main/java/org/seshat/controller/BautizoController.java`
- Create: `src/main/java/org/seshat/controller/ConfirmacionController.java`
- Create: `src/main/java/org/seshat/controller/MatrimonioController.java`

- [ ] **Step 1: Create BautizoController.java**

```java
package org.seshat.controller;

import org.seshat.model.Bautizo;
import org.seshat.service.BautizoService;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bautizos")
public class BautizoController {
    private final BautizoService service;
    private final PersonaService personaService;

    public BautizoController(BautizoService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("bautizo", new Bautizo());
        model.addAttribute("personas", personaService.listar());
        return "bautizos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Bautizo b, Model model) {
        if (id > 0) { b.setId(id); service.actualizar(b); }
        else service.guardar(b);
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("bautizo", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "bautizos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        service.eliminar(id);
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }
}
```

- [ ] **Step 2: Create ConfirmacionController.java**

```java
package org.seshat.controller;

import org.seshat.model.Confirmacion;
import org.seshat.service.ConfirmacionService;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/confirmaciones")
public class ConfirmacionController {
    private final ConfirmacionService service;
    private final PersonaService personaService;

    public ConfirmacionController(ConfirmacionService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("confirmacion", new Confirmacion());
        model.addAttribute("personas", personaService.listar());
        return "confirmaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Confirmacion c, Model model) {
        if (id > 0) { c.setId(id); service.actualizar(c); }
        else service.guardar(c);
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("confirmacion", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "confirmaciones/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        service.eliminar(id);
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }
}
```

- [ ] **Step 3: Create MatrimonioController.java**

```java
package org.seshat.controller;

import org.seshat.model.Matrimonio;
import org.seshat.service.MatrimonioService;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/matrimonios")
public class MatrimonioController {
    private final MatrimonioService service;
    private final PersonaService personaService;

    public MatrimonioController(MatrimonioService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("matrimonio", new Matrimonio());
        model.addAttribute("personas", personaService.listar());
        return "matrimonios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Matrimonio m, Model model) {
        if (id > 0) { m.setId(id); service.actualizar(m); }
        else service.guardar(m);
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("matrimonio", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "matrimonios/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        service.eliminar(id);
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```
git add -A && git commit -m "feat: add Bautizo, Confirmacion, Matrimonio controllers"
```

---

### Task 7: Thymeleaf Templates — Layout + Personas

**Files:**
- Create: `src/main/resources/templates/index.html`
- Create: `src/main/resources/templates/personas/listar.html`
- Create: `src/main/resources/templates/personas/formulario.html`
- Delete: `src/main/resources/fxml/main.fxml`
- Delete: `src/main/resources/fxml/bautizo.fxml`

**Template styling:**
- Warm palette: bg `#F5F0EB`, header `#5B2C3E`, accent `#C9A84C`
- Tailwind Play CDN (`<script src="https://cdn.tailwindcss.com">`)
- HTMX CDN (`<script src="https://unpkg.com/htmx.org">`)

- [ ] **Step 1: Create index.html (main layout)**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seshat — Parroquia María Misionera</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/htmx.org"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        granate: { DEFAULT: '#5B2C3E', light: '#7A3F55', dark: '#3D1D2A' },
                        cuero: { DEFAULT: '#8B4513', light: '#A0522D' },
                        dorado: '#C9A84C',
                        crema: '#F5F0EB'
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-crema min-h-screen flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-granate text-white min-h-screen p-4 flex flex-col">
        <h1 class="text-2xl font-bold mb-8 text-center">Seshat</h1>
        <p class="text-sm text-center text-granate-light mb-6">Parroquia María Misionera</p>
        <nav class="flex flex-col gap-3">
            <a href="/personas" hx-get="/personas" hx-target="#main-content"
               class="block px-4 py-3 rounded-lg bg-granate-light hover:bg-granate-dark transition text-white text-lg font-medium text-center">
                👥 Personas
            </a>
            <a href="/bautizos" hx-get="/bautizos" hx-target="#main-content"
               class="block px-4 py-3 rounded-lg bg-granate-light hover:bg-granate-dark transition text-white text-lg font-medium text-center">
                ✝️ Bautizos
            </a>
            <a href="/confirmaciones" hx-get="/confirmaciones" hx-target="#main-content"
               class="block px-4 py-3 rounded-lg bg-granate-light hover:bg-granate-dark transition text-white text-lg font-medium text-center">
                🔥 Confirmaciones
            </a>
            <a href="/matrimonios" hx-get="/matrimonios" hx-target="#main-content"
               class="block px-4 py-3 rounded-lg bg-granate-light hover:bg-granate-dark transition text-white text-lg font-medium text-center">
                💍 Matrimonios
            </a>
        </nav>
    </aside>
    <!-- Main content -->
    <main id="main-content" class="flex-1 p-8">
        <div class="text-center text-gray-400 mt-20">
            <p class="text-2xl">Bienvenido a Seshat</p>
            <p class="mt-2">Seleccione una opción del menú</p>
        </div>
    </main>
</body>
</html>
```

- [ ] **Step 2: Create personas/listar.html**

```html
<div class="bg-white rounded-xl shadow-md p-6" th:fragment="listar">
    <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-granate">Personas</h2>
        <a href="/personas/nuevo" hx-get="/personas/nuevo" hx-target="#main-content"
           class="bg-cuero text-white px-6 py-2 rounded-lg hover:bg-cuero-light transition text-lg">
            + Nueva Persona
        </a>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full text-left">
            <thead>
                <tr class="border-b-2 border-gray-200 text-gray-600">
                    <th class="py-3 px-4">RUT</th>
                    <th class="py-3 px-4">Nombres</th>
                    <th class="py-3 px-4">Apellidos</th>
                    <th class="py-3 px-4">Teléfono</th>
                    <th class="py-3 px-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="p : ${personas}" class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="py-3 px-4" th:text="${p.rut}">12.345.678-9</td>
                    <td class="py-3 px-4" th:text="${p.nombres}">Juan</td>
                    <td class="py-3 px-4" th:text="${p.apellidos}">Pérez</td>
                    <td class="py-3 px-4" th:text="${p.telefono}">+56 9 1234 5678</td>
                    <td class="py-3 px-4 flex gap-2">
                        <a th:href="@{'/personas/' + ${p.id} + '/editar'}"
                           hx-get="@{'/personas/' + ${p.id} + '/editar'}" hx-target="#main-content"
                           class="text-granate hover:text-granate-light underline">Editar</a>
                        <button hx-post="@{'/personas/eliminar/' + ${p.id}}" hx-target="#main-content"
                                onclick="return confirm('¿Eliminar esta persona?')"
                                class="text-red-600 hover:text-red-800 underline ml-2 cursor-pointer">Eliminar</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 3: Create personas/formulario.html**

```html
<div class="bg-white rounded-xl shadow-md p-6 max-w-2xl mx-auto" th:fragment="formulario">
    <h2 class="text-2xl font-bold text-granate mb-6" th:if="${persona.id == 0}">Nueva Persona</h2>
    <h2 class="text-2xl font-bold text-granate mb-6" th:unless="${persona.id == 0}">Editar Persona</h2>
    <form hx-post="/personas/guardar" hx-target="#main-content" class="grid grid-cols-2 gap-4">
        <input type="hidden" name="id" th:value="${persona.id}">
        <div class="col-span-2 sm:col-span-1">
            <label class="block text-gray-700 font-medium mb-1">Nombres</label>
            <input type="text" name="nombres" th:value="${persona.nombres}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2 sm:col-span-1">
            <label class="block text-gray-700 font-medium mb-1">Apellidos</label>
            <input type="text" name="apellidos" th:value="${persona.apellidos}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">RUT</label>
            <input type="text" name="rut" th:value="${persona.rut}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Fecha Nacimiento</label>
            <input type="date" name="fecha_nacimiento" th:value="${persona.fecha_nacimiento}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Teléfono</label>
            <input type="text" name="telefono" th:value="${persona.telefono}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Email</label>
            <input type="email" name="email" th:value="${persona.email}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2">
            <label class="block text-gray-700 font-medium mb-1">Dirección</label>
            <input type="text" name="direccion" th:value="${persona.direccion}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2 flex gap-4 mt-4">
            <button type="submit" class="bg-cuero text-white px-8 py-2 rounded-lg hover:bg-cuero-light transition text-lg">
                Guardar
            </button>
            <a href="/personas" hx-get="/personas" hx-target="#main-content"
               class="bg-gray-300 text-gray-700 px-8 py-2 rounded-lg hover:bg-gray-400 transition text-lg text-center">
                Cancelar
            </a>
        </div>
    </form>
</div>
```

- [ ] **Step 4: Delete FXML files**

Run:
```
rm src/main/resources/fxml/main.fxml
rm src/main/resources/fxml/bautizo.fxml
```

- [ ] **Step 5: Create directories for remaining templates**

Run:
```
mkdir -p src/main/resources/templates/bautizos
mkdir -p src/main/resources/templates/confirmaciones
mkdir -p src/main/resources/templates/matrimonios
```

- [ ] **Step 6: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```
git add -A && git commit -m "feat: add main layout and personas templates"
```

---

### Task 8: Thymeleaf Templates — Sacramentos

**Files:**
- Create: `src/main/resources/templates/bautizos/listar.html`
- Create: `src/main/resources/templates/bautizos/formulario.html`
- Create: `src/main/resources/templates/confirmaciones/listar.html`
- Create: `src/main/resources/templates/confirmaciones/formulario.html`
- Create: `src/main/resources/templates/matrimonios/listar.html`
- Create: `src/main/resources/templates/matrimonios/formulario.html`

- [ ] **Step 1: Create bautizos/listar.html**

```html
<div class="bg-white rounded-xl shadow-md p-6" th:fragment="listar">
    <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-granate">Bautizos</h2>
        <a href="/bautizos/nuevo" hx-get="/bautizos/nuevo" hx-target="#main-content"
           class="bg-cuero text-white px-6 py-2 rounded-lg hover:bg-cuero-light transition text-lg">
            + Nuevo Bautizo
        </a>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full text-left">
            <thead>
                <tr class="border-b-2 border-gray-200 text-gray-600">
                    <th class="py-3 px-4">ID</th>
                    <th class="py-3 px-4">Persona ID</th>
                    <th class="py-3 px-4">Fecha</th>
                    <th class="py-3 px-4">Parroquia</th>
                    <th class="py-3 px-4">Libro</th>
                    <th class="py-3 px-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="b : ${bautizos}" class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="py-3 px-4" th:text="${b.id}">1</td>
                    <td class="py-3 px-4" th:text="${b.persona_id}">1</td>
                    <td class="py-3 px-4" th:text="${b.fecha_bautizo}">2024-01-15</td>
                    <td class="py-3 px-4" th:text="${b.parroquia}">María Misionera</td>
                    <td class="py-3 px-4" th:text="${b.n_libro} + '/' + ${b.n_folio}">1/5</td>
                    <td class="py-3 px-4 flex gap-2">
                        <a th:href="@{'/bautizos/' + ${b.id} + '/editar'}"
                           hx-get="@{'/bautizos/' + ${b.id} + '/editar'}" hx-target="#main-content"
                           class="text-granate hover:text-granate-light underline">Editar</a>
                        <button hx-post="@{'/bautizos/eliminar/' + ${b.id}}" hx-target="#main-content"
                                onclick="return confirm('¿Eliminar este bautizo?')"
                                class="text-red-600 hover:text-red-800 underline ml-2 cursor-pointer">Eliminar</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 2: Create bautizos/formulario.html**

```html
<div class="bg-white rounded-xl shadow-md p-6 max-w-2xl mx-auto" th:fragment="formulario">
    <h2 class="text-2xl font-bold text-granate mb-6" th:if="${bautizo.id == 0}">Nuevo Bautizo</h2>
    <h2 class="text-2xl font-bold text-granate mb-6" th:unless="${bautizo.id == 0}">Editar Bautizo</h2>
    <form hx-post="/bautizos/guardar" hx-target="#main-content" class="grid grid-cols-2 gap-4">
        <input type="hidden" name="id" th:value="${bautizo.id}">
        <div class="col-span-2">
            <label class="block text-gray-700 font-medium mb-1">Persona</label>
            <select name="persona_id" required
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
                <option value="">Seleccione una persona</option>
                <option th:each="p : ${personas}" th:value="${p.id}" th:text="${p.nombres} + ' ' + ${p.apellidos}"
                        th:selected="${bautizo.persona_id == p.id}"></option>
            </select>
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Padre</label>
            <input type="text" name="padre" th:value="${bautizo.padre}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Madre</label>
            <input type="text" name="madre" th:value="${bautizo.madre}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Fecha Bautizo</label>
            <input type="date" name="fecha_bautizo" th:value="${bautizo.fecha_bautizo}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Parroquia</label>
            <input type="text" name="parroquia" th:value="${bautizo.parroquia}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Libro</label>
            <input type="text" name="n_libro" th:value="${bautizo.n_libro}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Folio</label>
            <input type="text" name="n_folio" th:value="${bautizo.n_folio}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2 flex gap-4 mt-4">
            <button type="submit" class="bg-cuero text-white px-8 py-2 rounded-lg hover:bg-cuero-light transition text-lg">Guardar</button>
            <a href="/bautizos" hx-get="/bautizos" hx-target="#main-content"
               class="bg-gray-300 text-gray-700 px-8 py-2 rounded-lg hover:bg-gray-400 transition text-lg text-center">Cancelar</a>
        </div>
    </form>
</div>
```

- [ ] **Step 3: Create confirmaciones/listar.html**

```html
<div class="bg-white rounded-xl shadow-md p-6" th:fragment="listar">
    <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-granate">Confirmaciones</h2>
        <a href="/confirmaciones/nuevo" hx-get="/confirmaciones/nuevo" hx-target="#main-content"
           class="bg-cuero text-white px-6 py-2 rounded-lg hover:bg-cuero-light transition text-lg">
            + Nueva Confirmación
        </a>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full text-left">
            <thead>
                <tr class="border-b-2 border-gray-200 text-gray-600">
                    <th class="py-3 px-4">ID</th>
                    <th class="py-3 px-4">Persona ID</th>
                    <th class="py-3 px-4">Fecha</th>
                    <th class="py-3 px-4">Guía</th>
                    <th class="py-3 px-4">Libro</th>
                    <th class="py-3 px-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="c : ${confirmaciones}" class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="py-3 px-4" th:text="${c.id}">1</td>
                    <td class="py-3 px-4" th:text="${c.persona_id}">1</td>
                    <td class="py-3 px-4" th:text="${c.fecha_confirmacion}">2024-06-01</td>
                    <td class="py-3 px-4" th:text="${c.guia}">Padre Juan</td>
                    <td class="py-3 px-4" th:text="${c.n_libro} + '/' + ${c.n_folio}">1/5</td>
                    <td class="py-3 px-4 flex gap-2">
                        <a th:href="@{'/confirmaciones/' + ${c.id} + '/editar'}"
                           hx-get="@{'/confirmaciones/' + ${c.id} + '/editar'}" hx-target="#main-content"
                           class="text-granate hover:text-granate-light underline">Editar</a>
                        <button hx-post="@{'/confirmaciones/eliminar/' + ${c.id}}" hx-target="#main-content"
                                onclick="return confirm('¿Eliminar esta confirmación?')"
                                class="text-red-600 hover:text-red-800 underline ml-2 cursor-pointer">Eliminar</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 4: Create confirmaciones/formulario.html**

```html
<div class="bg-white rounded-xl shadow-md p-6 max-w-2xl mx-auto" th:fragment="formulario">
    <h2 class="text-2xl font-bold text-granate mb-6" th:if="${confirmacion.id == 0}">Nueva Confirmación</h2>
    <h2 class="text-2xl font-bold text-granate mb-6" th:unless="${confirmacion.id == 0}">Editar Confirmación</h2>
    <form hx-post="/confirmaciones/guardar" hx-target="#main-content" class="grid grid-cols-2 gap-4">
        <input type="hidden" name="id" th:value="${confirmacion.id}">
        <div class="col-span-2">
            <label class="block text-gray-700 font-medium mb-1">Persona</label>
            <select name="persona_id" required
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
                <option value="">Seleccione una persona</option>
                <option th:each="p : ${personas}" th:value="${p.id}" th:text="${p.nombres} + ' ' + ${p.apellidos}"
                        th:selected="${confirmacion.persona_id == p.id}"></option>
            </select>
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Fecha Confirmación</label>
            <input type="date" name="fecha_confirmacion" th:value="${confirmacion.fecha_confirmacion}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Guía</label>
            <input type="text" name="guia" th:value="${confirmacion.guia}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Parroquia</label>
            <input type="text" name="parroquia" th:value="${confirmacion.parroquia}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Libro</label>
            <input type="text" name="n_libro" th:value="${confirmacion.n_libro}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Folio</label>
            <input type="text" name="n_folio" th:value="${confirmacion.n_folio}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2 flex gap-4 mt-4">
            <button type="submit" class="bg-cuero text-white px-8 py-2 rounded-lg hover:bg-cuero-light transition text-lg">Guardar</button>
            <a href="/confirmaciones" hx-get="/confirmaciones" hx-target="#main-content"
               class="bg-gray-300 text-gray-700 px-8 py-2 rounded-lg hover:bg-gray-400 transition text-lg text-center">Cancelar</a>
        </div>
    </form>
</div>
```

- [ ] **Step 5: Create matrimonios/listar.html**

```html
<div class="bg-white rounded-xl shadow-md p-6" th:fragment="listar">
    <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-granate">Matrimonios</h2>
        <a href="/matrimonios/nuevo" hx-get="/matrimonios/nuevo" hx-target="#main-content"
           class="bg-cuero text-white px-6 py-2 rounded-lg hover:bg-cuero-light transition text-lg">
            + Nuevo Matrimonio
        </a>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full text-left">
            <thead>
                <tr class="border-b-2 border-gray-200 text-gray-600">
                    <th class="py-3 px-4">ID</th>
                    <th class="py-3 px-4">Contrayente 1</th>
                    <th class="py-3 px-4">Contrayente 2</th>
                    <th class="py-3 px-4">Fecha</th>
                    <th class="py-3 px-4">Sacerdote</th>
                    <th class="py-3 px-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="m : ${matrimonios}" class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="py-3 px-4" th:text="${m.id}">1</td>
                    <td class="py-3 px-4" th:text="${m.persona1_id}">1</td>
                    <td class="py-3 px-4" th:text="${m.persona2_id}">2</td>
                    <td class="py-3 px-4" th:text="${m.fecha_matrimonio}">2024-03-10</td>
                    <td class="py-3 px-4" th:text="${m.sacerdote}">Padre Pedro</td>
                    <td class="py-3 px-4 flex gap-2">
                        <a th:href="@{'/matrimonios/' + ${m.id} + '/editar'}"
                           hx-get="@{'/matrimonios/' + ${m.id} + '/editar'}" hx-target="#main-content"
                           class="text-granate hover:text-granate-light underline">Editar</a>
                        <button hx-post="@{'/matrimonios/eliminar/' + ${m.id}}" hx-target="#main-content"
                                onclick="return confirm('¿Eliminar este matrimonio?')"
                                class="text-red-600 hover:text-red-800 underline ml-2 cursor-pointer">Eliminar</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 6: Create matrimonios/formulario.html**

```html
<div class="bg-white rounded-xl shadow-md p-6 max-w-2xl mx-auto" th:fragment="formulario">
    <h2 class="text-2xl font-bold text-granate mb-6" th:if="${matrimonio.id == 0}">Nuevo Matrimonio</h2>
    <h2 class="text-2xl font-bold text-granate mb-6" th:unless="${matrimonio.id == 0}">Editar Matrimonio</h2>
    <form hx-post="/matrimonios/guardar" hx-target="#main-content" class="grid grid-cols-2 gap-4">
        <input type="hidden" name="id" th:value="${matrimonio.id}">
        <div>
            <label class="block text-gray-700 font-medium mb-1">Contrayente 1</label>
            <select name="persona1_id" required
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
                <option value="">Seleccione</option>
                <option th:each="p : ${personas}" th:value="${p.id}" th:text="${p.nombres} + ' ' + ${p.apellidos}"
                        th:selected="${matrimonio.persona1_id == p.id}"></option>
            </select>
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Contrayente 2</label>
            <select name="persona2_id" required
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
                <option value="">Seleccione</option>
                <option th:each="p : ${personas}" th:value="${p.id}" th:text="${p.nombres} + ' ' + ${p.apellidos}"
                        th:selected="${matrimonio.persona2_id == p.id}"></option>
            </select>
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Fecha Matrimonio</label>
            <input type="date" name="fecha_matrimonio" th:value="${matrimonio.fecha_matrimonio}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Sacerdote</label>
            <input type="text" name="sacerdote" th:value="${matrimonio.sacerdote}" required
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Parroquia</label>
            <input type="text" name="parroquia" th:value="${matrimonio.parroquia}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Libro</label>
            <input type="text" name="n_libro" th:value="${matrimonio.n_libro}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">N° Folio</label>
            <input type="text" name="n_folio" th:value="${matrimonio.n_folio}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div>
            <label class="block text-gray-700 font-medium mb-1">Dirección (lugar)</label>
            <input type="text" name="direccion" th:value="${matrimonio.direccion}"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:border-granate">
        </div>
        <div class="col-span-2 flex gap-4 mt-4">
            <button type="submit" class="bg-cuero text-white px-8 py-2 rounded-lg hover:bg-cuero-light transition text-lg">Guardar</button>
            <a href="/matrimonios" hx-get="/matrimonios" hx-target="#main-content"
               class="bg-gray-300 text-gray-700 px-8 py-2 rounded-lg hover:bg-gray-400 transition text-lg text-center">Cancelar</a>
        </div>
    </form>
</div>
```

- [ ] **Step 7: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```
git add -A && git commit -m "feat: add Thymeleaf templates for all sacramentos"
```

---

### Task 9: Cleanup — Remove legacy files + final compile

**Files:**
- Delete: `src/main/java/org/seshat/ui/BautizoController.java`
- Delete: `src/main/java/org/seshat/ui/ConfirmacionController.java`
- Delete: `src/main/java/org/seshat/ui/MainController.java`
- Delete: `src/main/java/org/seshat/ui/MatrimonioController.java`
- Delete: `src/main/java/org/seshat/ui/PersonaController.java`
- Delete: `src/main/java/org/seshat/ui/PrimeraComunionController.java`
- Update: `AGENTS.md`

- [ ] **Step 1: Delete all legacy ui controllers**

Run:
```
rm src/main/java/org/seshat/ui/BautizoController.java
rm src/main/java/org/seshat/ui/ConfirmacionController.java
rm src/main/java/org/seshat/ui/MainController.java
rm src/main/java/org/seshat/ui/MatrimonioController.java
rm src/main/java/org/seshat/ui/PersonaController.java
rm src/main/java/org/seshat/ui/PrimeraComunionController.java
```

- [ ] **Step 2: Update AGENTS.md**

Rewrite to reflect new stack. Remove PrimeraComunion references.

- [ ] **Step 3: Final compile**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
git add -A && git commit -m "chore: cleanup legacy JavaFX files, update AGENTS.md"
```
