package com.example.sistema_municipalidad.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {

    public static boolean mostrarConfirmacion(
            String titulo,
            String mensaje
    ) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Optional<ButtonType> resultado =
                alert.showAndWait();

        return resultado.isPresent()
                && resultado.get() == ButtonType.OK;
    }

    public static void mostrarInformacion(
            String titulo,
            String mensaje
    ) {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                mensaje,
                ButtonType.OK
        );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void mostrarError(String mensaje) {
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
