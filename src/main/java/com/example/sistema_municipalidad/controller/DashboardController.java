package com.example.sistema_municipalidad.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private AnchorPane panelContenido;

    @FXML
    private Button btnLogout;

    @FXML
    private void cerrarSesion() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Cerrar sesión");
        confirm.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirm.setContentText(
                "Todos los cambios guardados permanecerán."
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void abrirTuristas() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/sistema_municipalidad/turistas-view.fxml"
                    )
            );

            Parent vistaTuristas = loader.load();

            AnchorPane.setTopAnchor(vistaTuristas, 0.0);
            AnchorPane.setRightAnchor(vistaTuristas, 0.0);
            AnchorPane.setBottomAnchor(vistaTuristas, 0.0);
            AnchorPane.setLeftAnchor(vistaTuristas, 0.0);

            panelContenido.getChildren().setAll(vistaTuristas);

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo cargar la pantalla de Gestión de Turistas."
            );
        }
    }

    private void mostrarError(String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                mensaje,
                ButtonType.OK
        );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}