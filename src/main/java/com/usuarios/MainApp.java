package com.usuarios;

import com.usuarios.controllers.DashboardController;
import com.usuarios.controllers.LoginController;
import com.usuarios.database.Database;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class MainApp extends Application {
    private static MainApp instance;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        instance = this;
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Bold.ttf"), 14);

        try {
            
            Database.getInstance().inicializar();
            mostrarLogin();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error de conexión");
            error.setHeaderText("No se pudo iniciar el sistema");
            error.setContentText("Verifique que MySQL esté ejecutándose y que MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER y MYSQL_PASSWORD sean correctos.\n\nDetalle: " + e.getMessage());
            error.showAndWait();
        }
    }

    public void mostrarLogin() {
        new LoginController(primaryStage).mostrar();
    }

    public void mostrarDashboard() {
        DashboardController dashboard = new DashboardController(primaryStage);
        dashboard.mostrar();
    }

    public static MainApp getInstance() { return instance; }

    public static void main(String[] args) { launch(args); }
}
