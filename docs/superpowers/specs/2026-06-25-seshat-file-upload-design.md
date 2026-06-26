# Seshat — Subida de Certificados y Fotos

## Contexto

Registro parroquial de sacramentos (BAUTIZO, CONFIRMACION, MATRIMONIO) y personas. Se necesita:
1. Subir certificados escaneados (PDF/imagen) vinculados a cada registro de sacramento, permitiendo múltiples archivos por registro.
2. Subir fotos de actividades vinculadas a personas, también múltiples.

## Modelo de Datos

Dos nuevas tablas en PostgreSQL:

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

- `CERTIFICADO.tipo` almacena `'BAUTIZO'`, `'CONFIRMACION'` o `'MATRIMONIO'`
- `CERTIFICADO.entidad_id` es el FK hacia el registro específico en la tabla correspondiente
- Dos POJOs: `Certificado.java`, `Foto.java`
- Las columnas `ruta_imagen` existentes en BAUTIZO/CONFIRMACION/MATRIMONIO se dejan intactas (no se usan más, pero no se eliminan para evitar migraciones)

## Almacenamiento en Disco

- Propiedad configurable: `seshat.upload-dir=./uploads`
- Subdirectorios: `certificados/`, `fotos/`
- Archivos guardados como `UUID.ext` (UUID + extensión original)
- Tamaño máximo: 10MB por archivo
- Servidos via `ResourceHandler` de Spring Boot (`/uploads/**` → `file:./uploads/`)
- `FileStorageService` con métodos: `save()`, `delete()`, `getPath()`

## UX / UI

### Certificados
- Sección dentro del formulario de cada sacramento (bautizos/formulario.html, etc.)
- Lista de archivos subidos: nombre original + fecha + botones descargar/eliminar
- Formulario HTMX para subir: input file + botón "Subir"
- Subida reemplaza la lista via `hx-target`

### Fotos
- Sección dentro del formulario/detalle de persona (personas/formulario.html)
- Grid de fotos con `object-fit: cover`
- Click para ver en tamaño completo
- Subida vía HTMX similar a certificados
- Campo opcional: descripción + fecha de la foto

## Arquitectura

```
org.seshat
├── model/
│   ├── Certificado.java    ← POJO
│   └── Foto.java           ← POJO
├── repository/
│   ├── CertificadoRepository.java
│   └── FotoRepository.java
├── service/
│   ├── CertificadoService.java
│   ├── FotoService.java
│   └── FileStorageService.java    ← maneja disco
├── controller/
│   ├── CertificadoController.java
│   └── FotoController.java
├── config/
│   └── WebConfig.java             ← ResourceHandler para uploads
└── resources/
    └── templates/
        ├── bautizos/formulario.html   ← sección certificados
        ├── confirmaciones/formulario.html
        ├── matrimonios/formulario.html
        └── personas/
            ├── formulario.html        ← sección fotos
            └── fragmento-fotos.html   ← fragmento HTMX
```

## Dependencias

Ninguna nueva. Spring Boot ya incluye soporte multipart. Solo se requiere configurar en `application.properties`:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
seshat.upload-dir=./uploads
```

## Archivos a modificar

- `schema.sql` — agregar tablas CERTIFICADO, FOTO
- `application.properties` — agregar config multipart + upload-dir
- `pom.xml` — sin cambios
- `SecurityConfig.java` — permitir `/uploads/**` sin autenticación (archivos servidos)
- `application.properties` — agregar `spring.web.resources.static-locations` si es necesario
