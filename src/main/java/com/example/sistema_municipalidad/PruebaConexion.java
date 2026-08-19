package com.example.sistema_municipalidad;

import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.dao.ProvinciaDAO;
import com.example.sistema_municipalidad.dao.TipoDocumentoDAO;
import com.example.sistema_municipalidad.dao.TuristaDAO;
import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.model.TipoDocumento;
import com.example.sistema_municipalidad.model.Turista;

import java.time.LocalDate;

public class PruebaConexion {
    public static void main(String[] args) {

//        Turista primerTurista = new Turista(
//                "Juan",
//                "Perez",
//                1,
//                "12345678",
//                LocalDate.of(1994, 9, 19),
//                5,
//                1,
//                "3515551234",
//                "juanperez@gmail.com",
//                "Sin observaciones."
//        );
//

        TuristaDAO turistaDAO = new TuristaDAO();

        System.out.println("=== TURISTAS ===");

        for (Turista turista : turistaDAO.listar()) {

            System.out.println(
                    turista.getIdTurista()
                            + " | "
                            + turista.getNombre()
                            + " "
                            + turista.getApellido()
                            + " | Documento: "
                            + turista.getNumeroDocumento()
            );
        }

    }
}
