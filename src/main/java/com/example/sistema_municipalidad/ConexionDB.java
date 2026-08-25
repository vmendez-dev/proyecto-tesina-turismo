package com.example.sistema_municipalidad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/practica_turismo";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "admin";

    public static Connection conectar() {

        try {
            Connection conexion = DriverManager.getConnection(
                    URL,
                    USUARIO,
                    PASSWORD
            );
            return conexion;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

