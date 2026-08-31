-- =============================================
-- BASE DE DATOS - SISTEMA DE GESTIÓN TURÍSTICA
-- =============================================

-- TABLA USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    usuario TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL,
    rol TEXT NOT NULL,
    dependencia TEXT,
    estado TEXT DEFAULT 'Activo',
    fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP,
    contrasena TEXT NOT NULL
);

-- DATOS DE PRUEBA
INSERT OR IGNORE INTO usuarios (id, nombre, usuario, email, rol, dependencia, estado, contrasena)
VALUES 
    (1, 'Administrador', 'admin', 'admin@municipalidad.gob.ar', 'Administrador', 'Sistemas', 'Activo', 'admin123'),
    (2, 'María Laura Gómez', 'mgomez', 'mgomez@municipalidad.gob.ar', 'Operador', 'Mesa de Entradas', 'Activo', '123456'),
    (3, 'Juan Pablo Pérez', 'jperez', 'jperez@municipalidad.gob.ar', 'Inspector', 'Inspección General', 'Activo', '123456'),
    (4, 'Sofía Andrea López', 'slopez', 'slopez@municipalidad.gob.ar', 'Administrador', 'Sistemas', 'Activo', '123456'),
    (5, 'Néstor Lezcano', 'nlezcano', 'nlezcano@municipalidad.gob.ar', 'Director', 'Modernización', 'Activo', '123456'),
    (6, 'Carlos Ruiz', 'cruiz', 'cruiz@municipalidad.gob.ar', 'Operador', 'Tesorería', 'Inactivo', '123456');

-- ÍNDICES PARA OPTIMIZACIÓN
CREATE INDEX IF NOT EXISTS idx_usuarios_usuario ON usuarios(usuario);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);
CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios(estado);