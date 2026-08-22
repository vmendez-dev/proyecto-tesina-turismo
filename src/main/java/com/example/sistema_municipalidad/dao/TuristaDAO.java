package com.example.sistema_municipalidad.dao;

import com.example.sistema_municipalidad.ConexionDB;
import com.example.sistema_municipalidad.model.Turista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TuristaDAO {

    // =====================================================
    // ALTA
    // =====================================================

    public boolean guardar(Turista turista) {

        String sql = """
                INSERT INTO turistas (
                    nombre,
                    apellido,
                    id_tipo_documento,
                    numero_documento,
                    fecha_nacimiento,
                    id_provincia,
                    id_pais,
                    telefono,
                    email,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, turista.getNombre());
            statement.setString(2, turista.getApellido());
            statement.setInt(3, turista.getIdTipoDocumento());
            statement.setString(4, turista.getNumeroDocumento());

            if (turista.getFechaNacimiento() != null) {
                statement.setDate(
                        5,
                        Date.valueOf(turista.getFechaNacimiento())
                );
            } else {
                statement.setNull(5, java.sql.Types.DATE);
            }

            if (turista.getIdProvincia() != null) {
                statement.setInt(6, turista.getIdProvincia());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }

            if (turista.getIdPais() != null) {
                statement.setInt(7, turista.getIdPais());
            } else {
                statement.setNull(7, java.sql.Types.INTEGER);
            }

            statement.setString(8, turista.getTelefono());
            statement.setString(9, turista.getEmail());
            statement.setString(10, turista.getObservaciones());

            statement.executeUpdate();

            System.out.println("Turista guardado correctamente.");

            return true;

        } catch (SQLException e) {

            System.out.println("Error al guardar el turista.");
            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // LISTAR TODOS LOS TURISTAS ACTIVOS
    // =====================================================

    public List<Turista> listar() {

        List<Turista> turistas = new ArrayList<>();

        String sql = """
                SELECT
                    id_turista,
                    nombre,
                    apellido,
                    id_tipo_documento,
                    numero_documento,
                    fecha_nacimiento,
                    id_provincia,
                    id_pais,
                    telefono,
                    email,
                    observaciones,
                    fecha_registro,
                    activo
                FROM turistas
                WHERE activo = TRUE
                ORDER BY apellido, nombre
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {

                turistas.add(convertirTurista(resultado));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar los turistas.");
            e.printStackTrace();
        }

        return turistas;
    }


    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    public Turista buscarPorId(int idTurista) {

        String sql = """
                SELECT
                    id_turista,
                    nombre,
                    apellido,
                    id_tipo_documento,
                    numero_documento,
                    fecha_nacimiento,
                    id_provincia,
                    id_pais,
                    telefono,
                    email,
                    observaciones,
                    fecha_registro,
                    activo
                FROM turistas
                WHERE id_turista = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, idTurista);

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return convertirTurista(resultado);
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al buscar el turista.");
            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // MODIFICAR
    // =====================================================

    public boolean modificar(Turista turista) {

        String sql = """
                UPDATE turistas
                SET
                    nombre = ?,
                    apellido = ?,
                    id_tipo_documento = ?,
                    numero_documento = ?,
                    fecha_nacimiento = ?,
                    id_provincia = ?,
                    id_pais = ?,
                    telefono = ?,
                    email = ?,
                    observaciones = ?
                WHERE id_turista = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, turista.getNombre());
            statement.setString(2, turista.getApellido());
            statement.setInt(3, turista.getIdTipoDocumento());
            statement.setString(4, turista.getNumeroDocumento());

            if (turista.getFechaNacimiento() != null) {
                statement.setDate(
                        5,
                        Date.valueOf(turista.getFechaNacimiento())
                );
            } else {
                statement.setNull(5, java.sql.Types.DATE);
            }

            if (turista.getIdProvincia() != null) {
                statement.setInt(6, turista.getIdProvincia());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }

            if (turista.getIdPais() != null) {
                statement.setInt(7, turista.getIdPais());
            } else {
                statement.setNull(7, java.sql.Types.INTEGER);
            }

            statement.setString(8, turista.getTelefono());
            statement.setString(9, turista.getEmail());
            statement.setString(10, turista.getObservaciones());

            statement.setInt(11, turista.getIdTurista());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Turista modificado correctamente.");
                return true;
            }

        } catch (SQLException e) {

            System.out.println("Error al modificar el turista.");
            e.printStackTrace();
        }

        return false;
    }


    // =====================================================
    // BAJA LÓGICA
    // =====================================================

    public boolean eliminar(int idTurista) {

        String sql = """
                UPDATE turistas
                SET activo = FALSE
                WHERE id_turista = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, idTurista);

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Turista eliminado correctamente.");
                return true;
            }

        } catch (SQLException e) {

            System.out.println("Error al eliminar el turista.");
            e.printStackTrace();
        }

        return false;
    }


    // =====================================================
    // MÉTODO AUXILIAR
    // =====================================================

    private Turista convertirTurista(ResultSet resultado) throws SQLException {

        Turista turista = new Turista();

        turista.setIdTurista(
                resultado.getInt("id_turista")
        );

        turista.setNombre(
                resultado.getString("nombre")
        );

        turista.setApellido(
                resultado.getString("apellido")
        );

        turista.setIdTipoDocumento(
                resultado.getInt("id_tipo_documento")
        );

        turista.setNumeroDocumento(
                resultado.getString("numero_documento")
        );


        // Fecha de nacimiento

        Date fechaNacimiento =
                resultado.getDate("fecha_nacimiento");

        if (fechaNacimiento != null) {

            turista.setFechaNacimiento(
                    fechaNacimiento.toLocalDate()
            );
        }


        // Provincia

        int idProvincia =
                resultado.getInt("id_provincia");

        if (!resultado.wasNull()) {

            turista.setIdProvincia(idProvincia);
        }


        // País

        int idPais =
                resultado.getInt("id_pais");

        if (!resultado.wasNull()) {

            turista.setIdPais(idPais);
        }


        turista.setTelefono(
                resultado.getString("telefono")
        );

        turista.setEmail(
                resultado.getString("email")
        );

        turista.setObservaciones(
                resultado.getString("observaciones")
        );


        // Fecha de registro

        Date fechaRegistro =
                resultado.getDate("fecha_registro");

        if (fechaRegistro != null) {

            turista.setFechaRegistro(
                    fechaRegistro.toLocalDate()
            );
        }

        turista.setActivo(
                resultado.getBoolean("activo")
        );

        return turista;
    }

    public boolean existeDocumento(int idTipoDocumento, String numeroDocumento) {

        String sql = """
            SELECT 1
            FROM turistas
            WHERE id_tipo_documento = ?
              AND numero_documento = ?
              AND activo = TRUE
            LIMIT 1
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, idTipoDocumento);
            statement.setString(2, numeroDocumento);

            try (ResultSet resultado = statement.executeQuery()) {

                return resultado.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public boolean existeDocumentoExceptoId(int idTipoDocumento, String numeroDocumento, int idTurista) {

        String sql = """
            SELECT 1
            FROM turistas
            WHERE id_tipo_documento = ?
              AND numero_documento = ?
              AND id_turista <> ?
              AND activo = TRUE
            LIMIT 1
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, idTipoDocumento);
            statement.setString(2, numeroDocumento);
            statement.setInt(3, idTurista);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}