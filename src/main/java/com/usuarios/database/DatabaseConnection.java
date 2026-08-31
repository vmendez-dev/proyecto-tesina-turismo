package com.usuarios.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final String url = "jdbc:sqlite:data/local.db";
    private boolean isLocalMode = true;

    private DatabaseConnection() {
        inicializarConexion();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    private void inicializarConexion() {
        try {
            Class.forName("org.sqlite.JDBC");
            crearTablasLocales();
            System.out.println("Base de datos local SQLite inicializada");
        } catch (Exception e) {
            System.err.println("Error al inicializar base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void crearTablasLocales() {
        String[] schemas = {
            "CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, usuario TEXT UNIQUE, email TEXT, rol TEXT, dependencia TEXT, estado TEXT, fecha_registro TEXT, contrasena TEXT)"
        };

        try (Connection conn = DriverManager.getConnection(url)) {
            for (String sql : schemas) {
                try (var stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public boolean isLocalMode() {
        return isLocalMode;
    }

    public void cerrar() {
    }
}