package com.example.sistema_municipalidad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Dashboard extends Application {

    // Variables para guardar la posición del mouse al hacer clic
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("dashboard-view.fxml"));

        // 1. Guardamos el root en una variable (Region engloba a AnchorPane, VBox, BorderPane, etc.)
        Region root = fxmlLoader.load();

        // 2. Pasamos el root a la escena
        Scene scene = new Scene(root, 1140, 728);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Sistema de Turismo");

        // 3. Evento para capturar las coordenadas iniciales del clic
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // 4. Evento opcional pero necesario para mover la ventana sin bordes
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.setScene(scene);
        stage.show();
    }
}
