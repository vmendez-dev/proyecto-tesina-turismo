package com.example.sistema_municipalidad.dao;

import com.example.sistema_municipalidad.ConexionDB;
import com.example.sistema_municipalidad.model.Pais;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaisDAO {

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
            System.out.println("Error al listar países.");
            e.printStackTrace();
        }

        return paises;
    }
}
