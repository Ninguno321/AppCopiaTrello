DROP TABLE IF EXISTS tablero;
DROP TABLE IF EXISTS lista;
DROP TABLE IF EXISTS tarjeta;
DROP TABLE IF EXISTS tarea;
DROP TABLE IF EXISTS checklist;
DROP TABLE IF EXISTS item_checklist;
DROP TABLE IF EXISTS etiqueta;
DROP TABLE IF EXISTS tarjeta_etiqueta;
DROP TABLE IF EXISTS traza;
DROP TABLE IF EXISTS tarjeta_completada;



CREATE TABLE tablero (
    id                  UUID            NOT NULL,
    nombre              VARCHAR(50)     NOT NULL,
    email_propietario   VARCHAR(255)    NOT NULL,
    bloqueado           BOOLEAN         NOT NULL,

    CONSTRAINT tablero_pk PRIMARY KEY (id)
);

CREATE TABLE lista (
    id              UUID            NOT NULL,
    nombre          VARCHAR(50)     NOT NULL,
    tablero_id      UUID            NOT NULL,

    CONSTRAINT lista_pk PRIMARY KEY (id),
    CONSTRAINT lista_tablero_fk FOREIGN KEY (tablero_id)
        REFERENCES tablero(id)
);

CREATE TABLE tarjeta (
    id              UUID            NOT NULL,
    titulo          VARCHAR(255)    NOT NULL,
    descripcion     VARCHAR(1000),
    completada      BOOLEAN         NOT NULL,
    lista_id        UUID            NOT NULL,

    CONSTRAINT tarjeta_pk PRIMARY KEY (id),
    CONSTRAINT tarjeta_lista_fk FOREIGN KEY (lista_id)
        REFERENCES lista(id)
);

CREATE TABLE tarea (
    id              UUID            NOT NULL,
    tarjeta_id      UUID            NOT NULL,
    titulo          VARCHAR(255),
    descripcion     VARCHAR(1000),
    fecha_limite    DATE,
    estado          VARCHAR(50),

    CONSTRAINT tarea_pk PRIMARY KEY (id),
    CONSTRAINT tarea_tarjeta_fk FOREIGN KEY (tarjeta_id)
        REFERENCES tarjeta(id)
);

CREATE TABLE checklist (
    id              UUID            NOT NULL,
    nombre          VARCHAR(255)    NOT NULL,
    tarjeta_id      UUID            NOT NULL,

    CONSTRAINT checklist_pk PRIMARY KEY (id),
    CONSTRAINT checklist_tarjeta_fk FOREIGN KEY (tarjeta_id)
        REFERENCES tarjeta(id)
);

CREATE TABLE item_checklist (
    id              UUID            NOT NULL,
    descripcion     VARCHAR(255)    NOT NULL,
    completado      BOOLEAN         NOT NULL,
    checklist_id    UUID            NOT NULL,

    CONSTRAINT item_checklist_pk PRIMARY KEY (id),
    CONSTRAINT item_checklist_checklist_fk FOREIGN KEY (checklist_id)
        REFERENCES checklist(id)
);

CREATE TABLE etiqueta (
    id          UUID            NOT NULL,
    nombre      VARCHAR(100)    NOT NULL,
    color       VARCHAR(50)     NOT NULL,

    CONSTRAINT etiqueta_pk PRIMARY KEY (id)
);

CREATE TABLE tarjeta_etiqueta (
    tarjeta_id  UUID    NOT NULL,
    etiqueta_id UUID    NOT NULL,

    CONSTRAINT tarjeta_etiqueta_pk PRIMARY KEY (tarjeta_id, etiqueta_id),
    CONSTRAINT tarjeta_etiqueta_tarjeta_fk FOREIGN KEY (tarjeta_id)
        REFERENCES tarjeta(id),
    CONSTRAINT tarjeta_etiqueta_etiqueta_fk FOREIGN KEY (etiqueta_id)
        REFERENCES etiqueta(id)
);

CREATE TABLE traza (
    id              UUID            NOT NULL,
    descripcion     VARCHAR(500)    NOT NULL,
    timestamp       TIMESTAMP       NOT NULL,
    tablero_id      UUID            NOT NULL,

    CONSTRAINT traza_pk PRIMARY KEY (id),
    CONSTRAINT traza_tablero_fk FOREIGN KEY (tablero_id)
        REFERENCES tablero(id)
);

CREATE TABLE tarjeta_completada (
    tablero_id  UUID    NOT NULL,
    tarjeta_id  UUID    NOT NULL,

    CONSTRAINT tarjeta_completada_pk PRIMARY KEY (tablero_id, tarjeta_id),
    CONSTRAINT tarjeta_completada_tablero_fk FOREIGN KEY (tablero_id)
        REFERENCES tablero(id),
    CONSTRAINT tarjeta_completada_tarjeta_fk FOREIGN KEY (tarjeta_id)
        REFERENCES tarjeta(id)
);