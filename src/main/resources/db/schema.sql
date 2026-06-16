CREATE TABLE PERSONA  (
  id        SERIAL PRIMARY KEY,
  nombres   TEXT NOT NULL,
  apellidos TEXT NOT NULL,
  rut       TEXT UNIQUE,
  fecha_nacimiento DATE,
  direccion TEXT,
  telefono  TEXT,
  email     TEXT,
  fecha_registro DATE NOT NULL
);

CREATE TABLE PADRINO (
 id        SERIAL PRIMARY KEY,
 nombres   TEXT NOT NULL,
 apellidos TEXT NOT NULL,
 rut       TEXT
);

CREATE TABLE BAUTIZO (
  id          SERIAL PRIMARY KEY,
  persona_id  INTEGER REFERENCES PERSONA(id),
  padre       TEXT,
  madre       TEXT,
  fecha_bautizo DATE NOT NULL,
  n_libro     TEXT,
  n_folio     TEXT,
  parroquia   TEXT,
  ruta_imagen TEXT
);

CREATE TABLE CONFIRMACION(
  id         SERIAL PRIMARY KEY,
  persona_id INTEGER REFERENCES PERSONA(id),
  fecha_confirmacion DATE NOT NULL,
  guia       TEXT,
  n_libro    TEXT,
  n_folio    TEXT,
  parroquia  TEXT,
  ruta_imagen TEXT
);

CREATE TABLE MATRIMONIO (
  id            SERIAL PRIMARY KEY,
  persona1_id   INTEGER REFERENCES PERSONA(id),
  persona2_id   INTEGER REFERENCES PERSONA(id),
  sacerdote     TEXT NOT NULL,
  fecha_matrimonio DATE NOT NULL,
  direccion     TEXT,
  n_libro       TEXT,
  n_folio       TEXT,
  parroquia     TEXT,
  ruta_imagen   TEXT
);

CREATE TABLE BAUTIZO_PADRINO(
  bautizo_id  INTEGER REFERENCES BAUTIZO(id),
  padrino_id  INTEGER REFERENCES  PADRINO(id),
  rol         TEXT NOT NULL
);

CREATE TABLE CONFIRMACION_PADRINO(
    confirmacion_id  INTEGER REFERENCES CONFIRMACION(id),
    padrino_id  INTEGER REFERENCES  PADRINO(id),
    rol         TEXT NOT NULL
);

CREATE TABLE MATRIMONIO_PADRINO(
    matrimonio_id  INTEGER REFERENCES MATRIMONIO(id),
    padrino_id  INTEGER REFERENCES  PADRINO(id),
    rol         TEXT NOT NULL
);
