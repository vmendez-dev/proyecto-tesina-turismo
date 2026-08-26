package app.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlojamientoDAO implements CRUD<Alojamiento> {

    private static final String URL = "jdbc:mysql://localhost:3306/turismo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "adminjota17";

    @Override
    public boolean insertar(Alojamiento a) {
        String sql = "INSERT INTO alojamientos (nombre, tipo, categoria, direccion, telefono, capacidad, " +
                "nombre_dueno, dni_dueno, descripcion, foto_url, estado, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getNombre());
            stmt.setString(2, a.getTipo());
            stmt.setString(3, a.getCategoria());
            stmt.setString(4, a.getDireccion());
            stmt.setString(5, a.getTelefono());
            stmt.setInt(6, a.getCapacidad());
            stmt.setString(7, a.getNombreDueno());
            stmt.setString(8, a.getDniDueno());
            stmt.setString(9, a.getDescripcion());
            stmt.setString(10, a.getFotoUrl());
            stmt.setString(11, a.getEstado());
            stmt.setDate(12, a.getFechaRegistro() != null ? Date.valueOf(a.getFechaRegistro()) : Date.valueOf(java.time.LocalDate.now()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Alojamiento a) {
        String sql = "UPDATE alojamientos SET nombre=?, tipo=?, categoria=?, direccion=?, telefono=?, " +
                "capacidad=?, nombre_dueno=?, dni_dueno=?, descripcion=?, foto_url=?, estado=? " +
                "WHERE id_alojamiento=?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getNombre());
            stmt.setString(2, a.getTipo());
            stmt.setString(3, a.getCategoria());
            stmt.setString(4, a.getDireccion());
            stmt.setString(5, a.getTelefono());
            stmt.setInt(6, a.getCapacidad());
            stmt.setString(7, a.getNombreDueno());
            stmt.setString(8, a.getDniDueno());
            stmt.setString(9, a.getDescripcion());
            stmt.setString(10, a.getFotoUrl());
            stmt.setString(11, a.getEstado());
            stmt.setInt(12, a.getIdAlojamiento());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        // Baja Lógica: Actualiza el estado a Inactivo
        String sql = "UPDATE alojamientos SET estado = 'Inactivo' WHERE id_alojamiento = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al dar de baja: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Alojamiento> listarTodos() {
        List<Alojamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM alojamientos ORDER BY id_alojamiento DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Alojamiento a = new Alojamiento(
                        rs.getInt("id_alojamiento"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("categoria"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getInt("capacidad"),
                        rs.getString("nombre_dueno"),
                        rs.getString("dni_dueno"),
                        rs.getString("descripcion"),
                        rs.getString("foto_url"),
                        rs.getString("estado"),
                        rs.getDate("fecha_registro") != null ? rs.getDate("fecha_registro").toLocalDate() : null
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Consulta auxiliar para la grilla de registros recientes.
     */
    public List<Alojamiento> listarUltimosRegistrados(int limite) {
        List<Alojamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM alojamientos ORDER BY fecha_registro DESC, id_alojamiento DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Alojamiento a = new Alojamiento(
                            rs.getInt("id_alojamiento"),
                            rs.getString("nombre"),
                            rs.getString("tipo"),
                            rs.getString("categoria"),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getInt("capacidad"),
                            rs.getString("nombre_dueno"),
                            rs.getString("dni_dueno"),
                            rs.getString("descripcion"),
                            rs.getString("foto_url"),
                            rs.getString("estado"),
                            rs.getDate("fecha_registro") != null ? rs.getDate("fecha_registro").toLocalDate() : null
                    );
                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar últimos alojamientos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}