package com.example.sistema_municipalidad.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class HelloController {

    // 1. Esto enlaza el AnchorPane en Scene Builder
    @FXML
    private AnchorPane panelContenido;

    // 2. Método para cambiar la pantalla en el fondo blanco
    private void cambiarPantalla(String nombreArchivoFxml) {
        try {
            // Buscamos y cargamos el archivo fxml secundario
            // Nota: Si tus FXML secundarios están en carpetas, ajusta la ruta (ej. "/com/example/.../turistas-view.fxml")
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_municipalidad/" + nombreArchivoFxml));
            Parent nuevaVista = loader.load();

            // Limpiamos el fondo blanco actual e inyectamos el nuevo
            panelContenido.getChildren().clear();
            panelContenido.getChildren().add(nuevaVista);

            // Ajustamos la subpantalla para que cubra todo el espacio disponible
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
