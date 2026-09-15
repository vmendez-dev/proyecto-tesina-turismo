package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.model.Turista;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

        // --- NUEVA LÓGICA DE OBSERVACIONES ("Ver más...") ---
        String observaciones = turista.getObservaciones();

        if (observaciones != null && !observaciones.isBlank()) {
            if (observaciones.length() > 25) {
                // Si es texto largo: Cortamos, ponemos link azul y evento de clic
                lblObservaciones.setText(observaciones.substring(0, 25) + " (Ver más...)");
                lblObservaciones.setStyle("-fx-text-fill: #1a73e8; -fx-cursor: hand; -fx-underline: true;");
                lblObservaciones.setOnMouseClicked(event -> mostrarObservacionCompleta(observaciones));
            } else {
                // Si es texto corto: Lo mostramos normal (negro, sin clic)
                lblObservaciones.setText(observaciones);
                lblObservaciones.setStyle("-fx-text-fill: #333333; -fx-cursor: default; -fx-underline: false;");
                lblObservaciones.setOnMouseClicked(null);
            }
        } else {
            // Si está vacío
            lblObservaciones.setText("-");
            lblObservaciones.setStyle("-fx-text-fill: #333333; -fx-cursor: default; -fx-underline: false;");
            lblObservaciones.setOnMouseClicked(null);
        }
    }

    // --- NUEVO MÉTODO PARA LA VENTANA EMERGENTE ---
    private void mostrarObservacionCompleta(String textoCompleto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Observaciones del Turista");
        alerta.setHeaderText("Detalle completo de las observaciones:");

        TextArea textArea = new TextArea(textoCompleto);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setPrefSize(400, 200);

        alerta.getDialogPane().setContent(textArea);
        alerta.showAndWait();
    }

    @FXML
    private void cerrar() {
        Stage ventana = (Stage) lblNombre.getScene().getWindow();
        ventana.close();
    }
}