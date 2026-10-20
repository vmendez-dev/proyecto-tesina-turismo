package com.usuarios.utils;

import com.usuarios.database.DatabaseConnection;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class NotificationManager {
    private static NotificationManager instance;
    private Popup popup;

    private NotificationManager() {
    }

    public static NotificationManager getInstance() {
        if (instance == null) synchronized (NotificationManager.class) {
            if (instance == null) instance = new NotificationManager();
        }
        return instance;
    }

    public void mostrarNotificacion(String mensaje, String tipo) {
        if (popup != null && popup.isShowing()) popup.hide();
        popup = new Popup();
        boolean dark = false;
        String access = "normal";
        try {
            var db = DatabaseConnection.getInstance();
            dark = "oscuro".equals(db.leerConfiguracion("tema", "claro"));
            access = db.leerConfiguracion("accesibilidad", "normal");
        } catch (Exception ignored) {
        }
        String bg = dark ? "#1b2a36" : "#e8f4fb";
        String fg = dark ? "#ffffff" : "#000000";
        String border = dark ? "#8aa8ba" : "#9fc7dc";
        if ("Protanopía".equals(access)) {
            bg = dark ? "#5f511a" : "#fff1b8";
            border = dark ? "#d6bd55" : "#c8a83b";
        } else if ("Deuteranopía".equals(access)) {
            bg = dark ? "#6b401c" : "#ffe0bf";
            border = dark ? "#e2a15f" : "#d99450";
        } else if ("Tritanopía".equals(access)) {
            bg = dark ? "#6b3151" : "#f8d5e8";
            border = dark ? "#d88ab2" : "#d889b0";
        } else if ("Acromatopsia".equals(access)) {
            bg = dark ? "#333333" : "#e6e6e6";
            border = dark ? "#aaaaaa" : "#555555";
        }
        if ("contraste".equals(access)) {
            bg = dark ? "#101010" : "#ffffff";
            fg = dark ? "#ffffff" : "#000000";
            border = fg;
        }
        if ("error".equalsIgnoreCase(tipo)) {
            if ("Acromatopsia".equals(access)) {
                bg = dark ? "#222222" : "#eeeeee";
                border = dark ? "#ffffff" : "#000000";
            } else if ("Protanopía".equals(access)) {
                bg = dark ? "#6a4a12" : "#fff0c2";
                border = dark ? "#f1cf64" : "#c8a83b";
            } else if ("Deuteranopía".equals(access)) {
                bg = dark ? "#63331b" : "#ffe3cf";
                border = dark ? "#e6a878" : "#d06d35";
            } else if ("Tritanopía".equals(access)) {
                bg = dark ? "#6b2947" : "#f8d9e7";
                border = dark ? "#e7a0c0" : "#c75d8e";
            } else {
                bg = dark ? "#5b2020" : "#fee2e2";
                border = dark ? "#ef9a9a" : "#dc6b6b";
            }
        }
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color:" + bg + ";-fx-padding:15;-fx-background-radius:8;-fx-border-color:" + border + ";-fx-border-radius:8;");
        Label label = new Label(mensaje);
        label.setStyle("-fx-text-fill:" + fg + ";-fx-font-size:14px;-fx-font-family:'Inter';-fx-font-weight:bold;");
        box.getChildren().add(label);
        popup.getContent().add(box);
        Stage stage = (Stage) javafx.stage.Window.getWindows().stream().filter(w -> w instanceof Stage && ((Stage) w).isShowing()).findFirst().orElse(null);
        if (stage != null)
            popup.show(stage, stage.getX() + stage.getWidth() / 2 - 150, stage.getY() + stage.getHeight() / 2 - 50);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (popup != null && popup.isShowing()) popup.hide();
                });
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
