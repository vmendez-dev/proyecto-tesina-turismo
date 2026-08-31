package com.usuarios;

import com.usuarios.controllers.DashboardController;
import com.usuarios.controllers.LoginController;
import com.usuarios.database.Database;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static MainApp instance;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        instance = this;
        Database.getInstance();
        mostrarLogin();
    }

    public void mostrarLogin() {
        LoginController login = new LoginController(primaryStage);
        login.mostrar();
    }

    public void mostrarDashboard() {
        Stage dashboardStage = new Stage();
        dashboardStage.setMaximized(true);
        DashboardController dashboard = new DashboardController(dashboardStage);
        dashboard.mostrar();
        primaryStage.close();
    }

    public static MainApp getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}