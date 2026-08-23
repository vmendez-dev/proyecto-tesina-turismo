package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.model.Turista;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class ConsultaTuristaController {

    @FXML private Label lblNombre;
    @FXML private Label lblApellido;
    @FXML private Label lblDocumento;
    @FXML private Label lblFechaNacimiento;
    @FXML private Label lblProcedencia;
    @FXML private Label lblPais;
    @FXML private Label lblTelefono;
    @FXML private Label lblEmail;
    @FXML private Label lblObservaciones;

    public void setTurista(Turista turista, String nombreProvincia, String nombrePais) {

        lblNombre.setText(turista.getNombre());
        lblApellido.setText(turista.getApellido());
        lblDocumento.setText(turista.getNumeroDocumento());

        if (turista.getFechaNacimiento() != null) {

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblFechaNacimiento.setText(turista.getFechaNacimiento().format(formato));

        } else {
            lblFechaNacimiento.setText("-");
        }

        lblProcedencia.setText(nombreProvincia != null ? nombreProvincia : "-");
        lblPais.setText(nombrePais != null ? nombrePais : "-");

        lblTelefono.setText(
                turista.getTelefono() != null &&
                        !turista.getTelefono().isBlank()
                        ? turista.getTelefono()
                        : "-"
        );

        lblEmail.setText(
                turista.getEmail() != null &&
                        !turista.getEmail().isBlank()
                        ? turista.getEmail()
                        : "-"
        );

        lblObservaciones.setText(
                turista.getObservaciones() != null &&
                        !turista.getObservaciones().isBlank()
                        ? turista.getObservaciones()
                        : "-"
        );
    }

    @FXML
    private void cerrar() {
        Stage ventana = (Stage) lblNombre.getScene().getWindow();
        ventana.close();
    }
}
