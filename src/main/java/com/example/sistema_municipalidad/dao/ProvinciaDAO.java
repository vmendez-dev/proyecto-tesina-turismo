package com.example.sistema_municipalidad.dao;

import com.example.sistema_municipalidad.ConexionDB;
import com.example.sistema_municipalidad.model.Provincia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProvinciaDAO {

    public List<Provincia> listarTodas() {

        List<Provincia> provincias = new ArrayList<>();

        String sql = """
                SELECT id_provincia, nombre_provincia, id_pais
                FROM provincias
                ORDER BY nombre_provincia
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {

                Provincia provincia = new Provincia(
                        resultado.getInt("id_provincia"),
                        resultado.getString("nombre_provincia"),
                        resultado.getInt("id_pais")
                );

                provincias.add(provincia);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar provincias.");
            e.printStackTrace();
        }

        return provincias;
    }

    public List<Provincia> listarPorPais(int idPais) {

        List<Provincia> provincias = new ArrayList<>();

        String sql = """
                SELECT id_provincia, nombre_provincia, id_pais
                FROM provincias
                WHERE id_pais = ?
                ORDER BY nombre_provincia
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, idPais);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    Provincia provincia = new Provincia(
                            resultado.getInt("id_provincia"),
                            resultado.getString("nombre_provincia"),
                            resultado.getInt("id_pais")
                    );

                    provincias.add(provincia);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar provincias del país.");
            e.printStackTrace();
        }

        return provincias;
    }
}
