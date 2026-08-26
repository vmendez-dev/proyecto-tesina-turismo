-- 1. Crear y seleccionar la base de datos
CREATE DATABASE IF NOT EXISTS turismo_db;
USE turismo_db;

-- 2. Crear la tabla de Alojamientos
CREATE TABLE IF NOT EXISTS alojamientos (
    id_alojamiento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,              -- Cabaña, Hotel, Hostal, Posada
    categoria VARCHAR(50) NOT NULL,         -- 1 Estrella, 2 Estrellas, ..., 5 Estrellas
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(50),
    capacidad INT NOT NULL,                 -- Plazas / Capacidad
    nombre_dueno VARCHAR(150),
    dni_dueno VARCHAR(20),
    descripcion TEXT,
    foto_url VARCHAR(255),                  -- Ruta o nombre del archivo de imagen
    estado VARCHAR(20) DEFAULT 'Activo',    -- 'Activo' o 'Inactivo'
    fecha_registro DATE DEFAULT (CURRENT_DATE)
);

-- 3. Cargar datos de prueba iniciales
INSERT INTO alojamientos (nombre, tipo, categoria, direccion, telefono, capacidad, nombre_dueno, dni_dueno, descripcion, estado, fecha_registro)
VALUES 
('Portal del Sol', 'Cabaña', '3 Estrellas', 'Calle Los Alerces, Córdoba', '(3541) 123-4567', 25, 'Juan Pérez García', '22.333.444', 'Hermosas cabañas frente al río', 'Activo', '2026-08-12'),
('Cabañas Las Sierras', 'Cabaña', '4 Estrellas', 'Av. Libertador, Buenos Aires', '(3541) 987-6543', 30, 'María Gómez', '30.111.222', 'Cabañas totalmente equipadas', 'Activo', '2026-08-11'),
('Hotel San Antonio', 'Hotel', '3 Estrellas', 'Bv. Pellegrini, Santa Fe', '(3541) 555-1122', 80, 'Carlos Ruiz', '28.444.555', 'Hotel céntrico con desayuno incluido', 'Inactivo', '2026-08-10'),
('Posada de la Villa', 'Hostal', '2 Estrellas', 'Calle Mitre, Córdoba', '(3541) 444-3322', 30, 'Ana Torres', '33.555.666', 'Posada acogedora en zona tranquila', 'Activo', '2026-08-09'),
('Hostal El Refugio', 'Hostal', '2 Estrellas', 'Ruta 20, Córdoba', '(3541) 222-1111', 124, 'Pedro López', '25.666.777', 'Gran capacidad para contingentes', 'Activo', '2026-08-08');
