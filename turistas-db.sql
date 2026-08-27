CREATE DATABASE IF NOT EXISTS practica_turismo;

USE practica_turismo;


-- =========================================================
-- TABLA: PAISES
-- =========================================================

CREATE TABLE paises (
    id_pais INT AUTO_INCREMENT PRIMARY KEY,
    nombre_pais VARCHAR(100) NOT NULL,

    CONSTRAINT uq_pais_nombre
        UNIQUE (nombre_pais)
);


-- =========================================================
-- TABLA: PROVINCIAS
-- =========================================================

CREATE TABLE provincias (
    id_provincia INT AUTO_INCREMENT PRIMARY KEY,
    nombre_provincia VARCHAR(100) NOT NULL,
    id_pais INT NOT NULL,

    CONSTRAINT uq_provincia_pais
        UNIQUE (nombre_provincia, id_pais),

    CONSTRAINT fk_provincia_pais
        FOREIGN KEY (id_pais)
        REFERENCES paises(id_pais)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =========================================================
-- TABLA: TIPOS DE DOCUMENTO
-- =========================================================

CREATE TABLE tipos_documento (
    id_tipo_documento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(50) NOT NULL,

    CONSTRAINT uq_tipo_documento_nombre
        UNIQUE (nombre_tipo)
);


-- =========================================================
-- TABLA: TURISTAS
-- =========================================================

CREATE TABLE turistas (
    id_turista INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL,

    apellido VARCHAR(50) NOT NULL,

    id_tipo_documento INT NOT NULL,

    numero_documento VARCHAR(30) NOT NULL,

    fecha_nacimiento DATE NULL,

    id_provincia INT NULL,

    id_pais INT NULL,

    telefono VARCHAR(30) NULL,

    email VARCHAR(100) NULL,

    observaciones TEXT NULL,

    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    activo BOOLEAN NOT NULL DEFAULT TRUE,


    -- =====================================================
    -- RESTRICCIONES
    -- =====================================================

    CONSTRAINT uq_documento
        UNIQUE (id_tipo_documento, numero_documento),


    -- =====================================================
    -- CLAVES FORÁNEAS
    -- =====================================================

    CONSTRAINT fk_turista_tipo_documento
        FOREIGN KEY (id_tipo_documento)
        REFERENCES tipos_documento(id_tipo_documento)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_turista_provincia
        FOREIGN KEY (id_provincia)
        REFERENCES provincias(id_provincia)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_turista_pais
        FOREIGN KEY (id_pais)
        REFERENCES paises(id_pais)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);