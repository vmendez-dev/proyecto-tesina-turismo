package com.example.sistema_municipalidad.dao;

import com.example.sistema_municipalidad.ConexionDB;
import com.example.sistema_municipalidad.model.Pais;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaisDAO {

    // ============================================================
    // LISTAR PAÍSES
    // ============================================================

    public List<Pais> listar() {

        List<Pais> paises = new ArrayList<>();

        String sql = """
                SELECT id_pais, nombre_pais
                FROM paises
                ORDER BY nombre_pais
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {

                Pais pais = new Pais(
                        resultado.getInt("id_pais"),
                        resultado.getString("nombre_pais")
                );

                paises.add(pais);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return paises;
    }


    // ============================================================
    // GUARDAR PAÍS
    // ============================================================

    public boolean guardar(Pais pais) {

        String sql = """
                INSERT INTO paises (nombre_pais)
                VALUES (?)
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, pais.getNombrePais().trim());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas == 0) {
                return false;
            }

            try (ResultSet clavesGeneradas = statement.getGeneratedKeys()) {

                if (clavesGeneradas.next()) {
                    pais.setIdPais(clavesGeneradas.getInt(1));
                }
            }

            return true;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // MODIFICAR PAÍS
    // ============================================================

    public boolean modificar(Pais pais) {

        String sql = """
                UPDATE paises
                SET nombre_pais = ?
                WHERE id_pais = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, pais.getNombrePais().trim());
            statement.setInt(2, pais.getIdPais());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // ELIMINAR PAÍS
    // ============================================================

    public boolean eliminar(Pais pais) {

        // No permitir eliminar Argentina
        if (pais.getNombrePais() != null
                && pais.getNombrePais().trim().equalsIgnoreCase("Argentina")) {

            return false;
        }

        // No permitir eliminar si tiene turistas asociados
        if (tieneTuristas(pais.getIdPais())) {

            return false;
        }

        // No permitir eliminar si tiene provincias asociadas
        if (tieneProvincias(pais.getIdPais())) {

            return false;
        }

        String sql = """
                DELETE FROM paises
                WHERE id_pais = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, pais.getIdPais());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // VERIFICAR SI EXISTE UN PAÍS CON ESE NOMBRE
    // ============================================================

    public boolean existeNombre(String nombrePais) {

        if (nombrePais == null || nombrePais.trim().isEmpty()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM paises
                WHERE LOWER(TRIM(nombre_pais)) = LOWER(TRIM(?))
                LIMIT 1
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, nombrePais.trim());

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // VERIFICAR SI EXISTE OTRO PAÍS CON ESE NOMBRE
    // ============================================================

    public boolean existeNombreExceptoId(String nombrePais, int idPais) {

        if (nombrePais == null || nombrePais.trim().isEmpty()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM paises
                WHERE LOWER(TRIM(nombre_pais)) = LOWER(TRIM(?))
                  AND id_pais <> ?
                LIMIT 1
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, nombrePais.trim());
            statement.setInt(2, idPais);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // VERIFICAR SI EL PAÍS TIENE TURISTAS
    // ============================================================

    public boolean tieneTuristas(int idPais) {

        String sql = """
                SELECT 1
                FROM turistas
                WHERE id_pais = ?
                LIMIT 1
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, idPais);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // VERIFICAR SI EL PAÍS TIENE PROVINCIAS
    // ============================================================

    public boolean tieneProvincias(int idPais) {

        String sql = """
                SELECT 1
                FROM provincias
                WHERE id_pais = ?
                LIMIT 1
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, idPais);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }
}