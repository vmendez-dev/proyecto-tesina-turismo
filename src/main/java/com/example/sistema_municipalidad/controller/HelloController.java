package com.example.sistema_municipalidad.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class HelloController {

    // Enlaza el AnchorPane en Scene Builder
    @FXML private AnchorPane panelContenido;

    // Método para cambiar la pantalla en el fondo blanco
    private void cambiarPantalla(String nombreArchivoFxml) {
        try {
            // Se busca y se carga el archivo fxml secundario:
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_municipalidad/" + nombreArchivoFxml));
            Parent nuevaVista = loader.load();

            // Se limpia el fondo blanco actual y se carga el nuevo:
            panelContenido.getChildren().clear();
            panelContenido.getChildren().add(nuevaVista);

            // Se ajusta la subpantalla para que cubra todo el espacio disponible
            AnchorPane.setTopAnchor(nuevaVista, 0.0);
            AnchorPane.setBottomAnchor(nuevaVista, 0.0);
            AnchorPane.setLeftAnchor(nuevaVista, 0.0);
            AnchorPane.setRightAnchor(nuevaVista, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error crítico: No se pudo cargar el archivo " + nombreArchivoFxml);
        }
    }

    @FXML
    private void irATuristas() {
        cambiarPantalla("turistas-view.fxml");
    }

}
