package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/turismo_arredondo";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);

    }
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conexión exitosa a MySQL");
        } catch (SQLException e) {
            System.out.println("❌ Error de conexión: " + e.getMessage());
        }
    }
}

