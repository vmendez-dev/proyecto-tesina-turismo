package com.usuarios.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class NotificationManager {
    private static NotificationManager instance;
    private Popup popup;

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    public void mostrarNotificacion(String mensaje, String tipo) {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }

        popup = new Popup();
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + getColor(tipo) + "; -fx-padding: 15; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        
        Label label = new Label(mensaje);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().add(label);

        popup.getContent().add(box);
        
        Stage stage = (Stage) javafx.stage.Window.getWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage) w).isShowing())
            .findFirst().orElse(null);
        
        if (stage != null) {
            popup.show(stage, stage.getX() + stage.getWidth()/2 - 150, 
                      stage.getY() + stage.getHeight()/2 - 50);
        }

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (popup != null && popup.isShowing()) {
                        popup.hide();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getColor(String tipo) {
        switch (tipo.toLowerCase()) {
            case "exito": return "#22c55e";
            case "error": return "#ef4444";
            case "advertencia": return "#f59e0b";
            default: return "#0284c7";
        }
    }
}