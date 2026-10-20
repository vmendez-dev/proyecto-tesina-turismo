CREATE DATABASE IF NOT EXISTS turismo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE turismo;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    apellido VARCHAR(120) NOT NULL DEFAULT '',
    usuario VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL,
    rol ENUM('Operador','Inspector','Director','Administrador') NOT NULL,
    dependencia VARCHAR(120) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    estado ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contrasena VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_usuarios_rol (rol),
    INDEX idx_usuarios_estado (estado),
    INDEX idx_usuarios_dependencia (dependencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mensaje VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_logs_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO usuarios (id,nombre,apellido,usuario,email,rol,dependencia,fecha_nacimiento,estado,contrasena) VALUES
(1,'Administrador','del Sistema','admin','admin@municipalidad.gob.ar','Administrador','Sistemas','1980-01-01','Activo','admin123'),
(2,'María Laura','Gómez','mgomez','mgomez@municipalidad.gob.ar','Operador','Mesa de Entradas','1988-05-14','Activo','123456'),
(3,'Juan Pablo','Pérez','jperez','jperez@municipalidad.gob.ar','Inspector','Inspección General','1985-08-20','Activo','123456'),
(4,'Sofía Andrea','López','slopez','slopez@municipalidad.gob.ar','Administrador','Sistemas','1979-11-02','Activo','123456'),
(5,'Néstor','Lezcano','nlezcano','nlezcano@municipalidad.gob.ar','Director','Modernización','1976-03-11','Activo','123456'),
(6,'Carlos','Ruiz','cruiz','cruiz@municipalidad.gob.ar','Operador','Tesorería','1990-07-09','Inactivo','123456')
ON DUPLICATE KEY UPDATE id=id;

CREATE TABLE IF NOT EXISTS paises (
    id_pais INT AUTO_INCREMENT PRIMARY KEY,
    nombre_pais VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS provincias (
    id_provincia INT AUTO_INCREMENT PRIMARY KEY,
    nombre_provincia VARCHAR(100) NOT NULL,
    id_pais INT NOT NULL,
    UNIQUE KEY uq_provincia_pais(nombre_provincia,id_pais),
    CONSTRAINT fk_provincia_pais FOREIGN KEY(id_pais) REFERENCES paises(id_pais) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tipos_documento (
    id_tipo_documento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS actividades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    descripcion VARCHAR(255) NOT NULL DEFAULT '',
    duracion VARCHAR(100) NOT NULL DEFAULT '',
    precio VARCHAR(100) NOT NULL DEFAULT '',
    horario VARCHAR(100) NOT NULL DEFAULT '',
    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS puntos_turisticos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    tipo VARCHAR(100) NOT NULL DEFAULT '',
    ubicacion VARCHAR(180) NOT NULL DEFAULT '',
    descripcion VARCHAR(255) NOT NULL DEFAULT '',
    horario VARCHAR(100) NOT NULL DEFAULT '',
    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tipo_establecimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gastronomia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    tipo_cocina VARCHAR(100) NOT NULL DEFAULT '',
    especialidad VARCHAR(255) NOT NULL DEFAULT '',
    tipo_id INT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS servicios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    tipo VARCHAR(100) NOT NULL DEFAULT '',
    direccion VARCHAR(180) NOT NULL DEFAULT '',
    telefono VARCHAR(40) NOT NULL DEFAULT '',
    horario VARCHAR(100) NOT NULL DEFAULT '',
    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS eventos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    fecha VARCHAR(40) NOT NULL DEFAULT '',
    horario VARCHAR(100) NOT NULL DEFAULT '',
    lugar VARCHAR(180) NOT NULL DEFAULT '',
    descripcion VARCHAR(255) NOT NULL DEFAULT '',
    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
