# Modelo de base de datos — Seshat

## Descripción general

Seshat utiliza PostgreSQL como motor de base de datos. El modelo está diseñado para registrar los sacramentos de la parroquia María Misionera, cumpliendo con los principios ACID y las reglas de normalización.

Los sacramentos que generan certificado son:
- **Bautizo** — único por persona
- **Confirmación** — único por persona
- **Matrimonio** — puede repetirse en la vida de una persona

---

## Tablas

### PERSONA
Centro del modelo. Toda persona puede existir en el sistema sin tener ningún sacramento registrado en la parroquia.

| Columna          | Tipo    | Restricción | Descripción                        |
|------------------|---------|-------------|------------------------------------|
| id               | INTEGER | PK          | Identificador único                |
| nombres          | TEXT    | NOT NULL    | Nombres de la persona              |
| apellidos        | TEXT    | NOT NULL    | Apellidos de la persona            |
| rut              | TEXT    | UNIQUE      | RUT (puede ser nulo si es extranjero) |
| fecha_nacimiento | DATE    |             | Fecha de nacimiento                |
| direccion        | TEXT    |             | Dirección actual                   |
| telefono         | TEXT    |             | Teléfono de contacto               |
| email            | TEXT    |             | Correo electrónico                 |
| fecha_registro   | DATE    | NOT NULL    | Fecha en que se ingresó al sistema |

---

### BAUTIZO
Un bautizo pertenece a exactamente una persona. Una persona puede tener a lo más un bautizo.

| Columna          | Tipo    | Restricción | Descripción                                      |
|------------------|---------|-------------|--------------------------------------------------|
| id               | INTEGER | PK          | Identificador único                              |
| persona_id       | INTEGER | FK → PERSONA| Persona que recibió el sacramento                |
| padre            | TEXT    |             | Nombre del padre (texto libre)                   |
| madre            | TEXT    |             | Nombre de la madre (texto libre)                 |
| fecha_bautizo    | DATE    | NOT NULL    | Fecha en que se celebró el bautizo               |
| n_libro          | TEXT    |             | Número del libro físico parroquial               |
| n_folio          | TEXT    |             | Número de folio dentro del libro                 |
| parroquia        | TEXT    |             | Parroquia donde se celebró (puede ser externa)   |
| ruta_imagen      | TEXT    |             | Ruta al archivo escaneado del certificado físico |

---

### CONFIRMACION
Una confirmación pertenece a exactamente una persona. Una persona puede tener a lo más una confirmación.

| Columna              | Tipo    | Restricción  | Descripción                                      |
|----------------------|---------|--------------|--------------------------------------------------|
| id                   | INTEGER | PK           | Identificador único                              |
| persona_id           | INTEGER | FK → PERSONA | Persona que recibió el sacramento                |
| fecha_confirmacion   | DATE    | NOT NULL     | Fecha en que se celebró la confirmación          |
| guia                 | TEXT    |              | Persona que guió la preparación                  |
| n_libro              | TEXT    |              | Número del libro físico parroquial               |
| n_folio              | TEXT    |              | Número de folio dentro del libro                 |
| parroquia            | TEXT    |              | Parroquia donde se celebró (puede ser externa)   |
| ruta_imagen          | TEXT    |              | Ruta al archivo escaneado del certificado físico |

---

### MATRIMONIO
Un matrimonio involucra exactamente dos personas (contrayente 1 y contrayente 2). Una persona puede tener más de un matrimonio registrado.

| Columna          | Tipo    | Restricción  | Descripción                                      |
|------------------|---------|--------------|--------------------------------------------------|
| id               | INTEGER | PK           | Identificador único                              |
| persona1_id      | INTEGER | FK → PERSONA | Primer contrayente                               |
| persona2_id      | INTEGER | FK → PERSONA | Segundo contrayente                              |
| sacerdote        | TEXT    |              | Nombre del sacerdote celebrante                  |
| fecha_matrimonio | DATE    | NOT NULL     | Fecha en que se celebró el matrimonio            |
| direccion        | TEXT    |              | Lugar de celebración                             |
| n_libro          | TEXT    |              | Número del libro físico parroquial               |
| n_folio          | TEXT    |              | Número de folio dentro del libro                 |
| parroquia        | TEXT    |              | Parroquia donde se celebró (puede ser externa)   |
| ruta_imagen      | TEXT    |              | Ruta al archivo escaneado del certificado físico |

---

### PADRINO
Tabla independiente de PERSONA. Un padrino no necesita tener registro como feligrés de la parroquia ni tener sacramentos registrados en ella.

| Columna   | Tipo    | Restricción | Descripción            |
|-----------|---------|-------------|------------------------|
| id        | INTEGER | PK          | Identificador único    |
| nombres   | TEXT    | NOT NULL    | Nombres del padrino    |
| apellidos | TEXT    | NOT NULL    | Apellidos del padrino  |
| rut       | TEXT    |             | RUT (opcional)         |

---

### BAUTIZO_PADRINO
Tabla intermedia que resuelve la relación muchos a muchos entre BAUTIZO y PADRINO.

| Columna    | Tipo    | Restricción    | Descripción                          |
|------------|---------|----------------|--------------------------------------|
| bautizo_id | INTEGER | FK → BAUTIZO   | Bautizo al que pertenece             |
| padrino_id | INTEGER | FK → PADRINO   | Padrino que participa                |
| rol        | TEXT    | NOT NULL       | Rol: `padrino`, `madrina`            |

---

### CONFIRMACION_PADRINO
Tabla intermedia que resuelve la relación muchos a muchos entre CONFIRMACION y PADRINO.

| Columna          | Tipo    | Restricción        | Descripción                          |
|------------------|---------|--------------------|--------------------------------------|
| confirmacion_id  | INTEGER | FK → CONFIRMACION  | Confirmación a la que pertenece      |
| padrino_id       | INTEGER | FK → PADRINO       | Padrino que participa                |
| rol              | TEXT    | NOT NULL           | Rol: `padrino`, `madrina`            |

---

### MATRIMONIO_PADRINO
Tabla intermedia que resuelve la relación muchos a muchos entre MATRIMONIO y PADRINO.

| Columna        | Tipo    | Restricción      | Descripción                                    |
|----------------|---------|------------------|------------------------------------------------|
| matrimonio_id  | INTEGER | FK → MATRIMONIO  | Matrimonio al que pertenece                    |
| padrino_id     | INTEGER | FK → PADRINO     | Padrino/testigo que participa                  |
| rol            | TEXT    | NOT NULL         | Rol: `padrino`, `madrina`, `testigo`           |

---

## Relaciones

| Relación                        | Cardinalidad | Descripción                                              |
|---------------------------------|--------------|----------------------------------------------------------|
| PERSONA → BAUTIZO               | 1 a 0..1     | Una persona puede tener a lo más un bautizo              |
| PERSONA → CONFIRMACION          | 1 a 0..1     | Una persona puede tener a lo más una confirmación        |
| PERSONA → MATRIMONIO            | 1 a 0..*     | Una persona puede tener múltiples matrimonios            |
| BAUTIZO ↔ PADRINO               | N a N        | Resuelto por BAUTIZO_PADRINO                             |
| CONFIRMACION ↔ PADRINO          | N a N        | Resuelto por CONFIRMACION_PADRINO                        |
| MATRIMONIO ↔ PADRINO            | N a N        | Resuelto por MATRIMONIO_PADRINO                          |

---

## Notas de diseño

- El campo `parroquia` en cada sacramento permite registrar sacramentos celebrados fuera de María Misionera. Solo se puede emitir certificado si `parroquia = 'María Misionera'`.
- El campo `ruta_imagen` almacena la ruta relativa al archivo escaneado en el sistema de archivos, no el binario de la imagen directamente.
- Ambos contrayentes en MATRIMONIO deben existir previamente en PERSONA. Si uno viene de otra parroquia, se crea primero en PERSONA y luego se registra el matrimonio.
- La tabla PADRINO es independiente de PERSONA intencionalmente, ya que un padrino no necesita ser feligrés de la parroquia.

---

## Estado

- [x] Modelo diseñado
- [ ] SQL implementado en `schema.sql`
- [ ] Tablas creadas en PostgreSQL
