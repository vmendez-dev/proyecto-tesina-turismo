package com.example.sistema_municipalidad.dao;

import com.example.sistema_municipalidad.ConexionDB;
import com.example.sistema_municipalidad.model.TipoDocumento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumentoDAO {

    public List<TipoDocumento> listar() {

        List<TipoDocumento> tiposDocumento = new ArrayList<>();

        String sql = """
                SELECT id_tipo_documento, nombre_tipo
                FROM tipos_documento
                ORDER BY nombre_tipo
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {

                TipoDocumento tipoDocumento = new TipoDocumento(
                        resultado.getInt("id_tipo_documento"),
                        resultado.getString("nombre_tipo")
                );

                tiposDocumento.add(tipoDocumento);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar tipos de documento.");
            e.printStackTrace();
        }

        return tiposDocumento;
    }
}
