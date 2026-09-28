-- ==========================================================
-- SISTEMA DE GESTIÓN TURÍSTICA - MÓDULO ALOJAMIENTOS
-- ==========================================================

-- 1. Crear y seleccionar la base de datos
CREATE DATABASE IF NOT EXISTS turismo_db;
USE turismo_db;

-- 2. Crear la tabla de Alojamientos
CREATE TABLE IF NOT EXISTS alojamientos (
    id_alojamiento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,              -- Cabaña, Hotel, Hostal, Posada, Apart
    categoria VARCHAR(50) NOT NULL,         -- 1 Estrella, 2 Estrellas, ..., 5 Estrellas
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    capacidad INT NOT NULL,                 -- Plazas totales
    nombre_dueno VARCHAR(150),
    dni_dueno VARCHAR(20),
    descripcion TEXT,
    amenities VARCHAR(255),                 -- Wifi, Cochera, Pileta, etc.)
    foto_url VARCHAR(255),                  
    estado VARCHAR(20) DEFAULT 'Activo',    -- 'Activo' o 'Inactivo'
    fecha_registro DATE DEFAULT (CURRENT_DATE)
    );

-- 3. Cargar datos de prueba iniciales con amenities
INSERT INTO alojamientos (
    nombre, tipo, categoria, direccion, telefono, capacidad, 
    nombre_dueno, dni_dueno, descripcion, amenities, foto_url, estado, fecha_registro
) VALUES 
('Portal del Sol', 'Cabaña', '3 Estrellas', 'Calle Los Alerces 142, Córdoba', '(3541) 123-4567', 25, 
 'Juan Pérez García', '22.333.444', 'Hermosas cabañas frente al río con bajada propia.', 
 'Wifi, Cochera, Pileta, Climatización', NULL, 'Activo', '2026-08-12'),

('Cabañas Las Sierras', 'Cabaña', '4 Estrellas', 'Av. Libertador 850, Córdoba', '(3541) 987-6543', 30, 
 'María Gómez', '30.111.222', 'Cabañas premium totalmente equipadas en zona de montaña.', 
 'Wifi, Cochera, Pileta, Desayuno, Climatización', NULL, 'Activo', '2026-08-11'),

('Hotel San Antonio', 'Hotel', '3 Estrellas', 'Bv. Pellegrini 420, Santa Fe', '(3541) 555-1122', 80, 
 'Carlos Ruiz', '28.444.555', 'Hotel céntrico ideal para turismo corporativo.', 
 'Wifi, Desayuno, Climatización', NULL, 'Inactivo', '2026-08-10'),

('Posada de la Villa', 'Hostal', '2 Estrellas', 'Calle Mitre 95, Córdoba', '(3541) 444-3322', 30, 
 'Ana Torres', '33.555.666', 'Posada acogedora en el casco histórico de la villa.', 
 'Wifi, Desayuno', NULL, 'Activo', '2026-08-09'),

('Hostal El Refugio', 'Hostal', '2 Estrellas', 'Ruta 20 Km 15, Córdoba', '(3541) 222-1111', 124, 
 'Pedro López', '25.666.777', 'Gran capacidad para contingentes y grupos estudiantiles.', 
 'Wifi, Cochera', NULL, 'Activo', '2026-08-08');
