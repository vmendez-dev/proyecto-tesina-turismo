package com.usuarios.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public final class DatabaseConnection {
    private static DatabaseConnection instance;

    private final String host;
    private final String port;
    private final String database;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        host = obtener("MYSQL_HOST", "localhost");
        port = obtener("MYSQL_PORT", "3306");
        database = obtener("MYSQL_DATABASE", "turismo");
        user = obtener("MYSQL_USER", "root");
        password = obtener("MYSQL_PASSWORD", "adminjota17");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static String obtener(String variable, String defecto) {
        String valor = System.getenv(variable);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(variable);
        }
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    public String getDatabaseName() {
        return database;
    }

    
    public void inicializarBaseDeDatos() throws SQLException {
        Class.forNameSafe();
        String serverUrl = "jdbc:mysql://" + host + ":" + port
                + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America%2FArgentina%2FCordoba";

        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + database.replace("`", "")
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            try { stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN fecha_nacimiento DATE NULL"); } catch (SQLException ignored) {}
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS busquedas_actividades (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    termino VARCHAR(140) NOT NULL,
                    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    INDEX idx_busquedas_actividades_fecha(fecha),
                    INDEX idx_busquedas_actividades_termino(termino)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
            migrarBusquedasActividades(conn, stmt);
            try { stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN apellido VARCHAR(120) NOT NULL DEFAULT ''"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN fecha_eliminacion DATETIME NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("UPDATE usuarios SET fecha_eliminacion=fecha_registro WHERE estado='Inactivo' AND fecha_eliminacion IS NULL"); } catch (SQLException ignored) {}
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS logs (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    mensaje VARCHAR(500) NOT NULL,
                    PRIMARY KEY (id),
                    INDEX idx_logs_fecha (fecha)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
        }

        SchemaInitializer.init(this);
        insertarDatosIniciales();
    }

    private void migrarBusquedasActividades(Connection conn, Statement stmt) throws SQLException {
        try {
            stmt.executeUpdate("ALTER TABLE busquedas_actividades ADD COLUMN fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        } catch (SQLException ignored) {}
        if (existeColumna(conn, "busquedas_actividades", "actividad_id")) {
            try {
                stmt.executeUpdate("ALTER TABLE busquedas_actividades MODIFY COLUMN actividad_id INT NULL DEFAULT NULL");
            } catch (SQLException ignored) {
                quitarClavesForaneasActividad(conn, stmt);
                try {
                    stmt.executeUpdate("ALTER TABLE busquedas_actividades MODIFY COLUMN actividad_id INT NULL DEFAULT NULL");
                } catch (SQLException ignored2) {
                    try {
                        stmt.executeUpdate("ALTER TABLE busquedas_actividades ALTER COLUMN actividad_id SET DEFAULT NULL");
                    } catch (SQLException ignored3) {}
                }
            }
        }
        try {
            stmt.executeUpdate("ALTER TABLE busquedas_actividades ADD INDEX idx_busquedas_actividades_fecha(fecha)");
        } catch (SQLException ignored) {}
        try {
            stmt.executeUpdate("ALTER TABLE busquedas_actividades ADD INDEX idx_busquedas_actividades_termino(termino)");
        } catch (SQLException ignored) {}
    }

    private void quitarClavesForaneasActividad(Connection conn, Statement stmt) throws SQLException {
        try (ResultSet r = conn.getMetaData().getImportedKeys(conn.getCatalog(), null, "busquedas_actividades")) {
            while (r.next()) {
                String columna = r.getString("FKCOLUMN_NAME");
                String nombre = r.getString("FK_NAME");
                if ("actividad_id".equalsIgnoreCase(columna) && nombre != null && !nombre.isBlank()) {
                    stmt.executeUpdate("ALTER TABLE busquedas_actividades DROP FOREIGN KEY `" + nombre.replace("`", "``") + "`");
                }
            }
        }
    }

    private boolean existeColumna(Connection conn, String tabla, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND COLUMN_NAME=?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, database);
            p.setString(2, tabla);
            p.setString(3, columna);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1) > 0;
            }
        }
    }

    private void insertarDatosIniciales() throws SQLException {
        String sql = """
            INSERT INTO usuarios
                (id, nombre, apellido, usuario, email, rol, dependencia, fecha_nacimiento, estado, contrasena)
            VALUES
                (1, 'Administrador', 'del Sistema', 'admin', 'admin@municipalidad.gob.ar', 'Administrador', 'Sistemas', '1980-01-01', 'Activo', 'admin123'),
                (2, 'María Laura', 'Gómez', 'mgomez', 'mgomez@municipalidad.gob.ar', 'Operador', 'Mesa de Entradas', '1988-05-14', 'Activo', '123456'),
                (3, 'Juan Pablo', 'Pérez', 'jperez', 'jperez@municipalidad.gob.ar', 'Inspector', 'Inspección General', '1985-08-20', 'Activo', '123456'),
                (4, 'Sofía Andrea', 'López', 'slopez', 'slopez@municipalidad.gob.ar', 'Administrador', 'Sistemas', '1979-11-02', 'Activo', '123456'),
                (5, 'Néstor', 'Lezcano', 'nlezcano', 'nlezcano@municipalidad.gob.ar', 'Director', 'Modernización', '1976-03-11', 'Activo', '123456'),
                (6, 'Carlos', 'Ruiz', 'cruiz', 'cruiz@municipalidad.gob.ar', 'Operador', 'Tesorería', '1990-07-09', 'Inactivo', '123456')
            ON DUPLICATE KEY UPDATE contrasena = IF(usuario = 'admin', 'admin123', contrasena), rol = IF(usuario = 'admin', 'Administrador', rol), estado = IF(usuario = 'admin', 'Activo', estado)
            """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.executeUpdate("UPDATE usuarios SET fecha_eliminacion=COALESCE(fecha_eliminacion,CURRENT_TIMESTAMP) WHERE estado='Inactivo'");
        }
    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America%2FArgentina%2FCordoba"
                + "&characterEncoding=utf8";
        return DriverManager.getConnection(url, user, password);
    }

    public void guardarConfiguracion(String clave, String valor) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO configuracion(clave,valor) VALUES(?,?) ON DUPLICATE KEY UPDATE valor=VALUES(valor)")) {
            p.setString(1, clave); p.setString(2, valor); p.executeUpdate();
        }
    }

    public String leerConfiguracion(String clave, String defecto) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("SELECT valor FROM configuracion WHERE clave=?")) {
            p.setString(1, clave);
            try (ResultSet r=p.executeQuery()) { return r.next()?r.getString(1):defecto; }
        }
    }

    public void cerrar() {
        
    }

    private static final class Class {
        private static void forNameSafe() throws SQLException {
            try {
                java.lang.Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontró MySQL Connector/J en el classpath.", e);
            }
        }
    }
}
