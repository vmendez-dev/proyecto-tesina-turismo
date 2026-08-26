package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/alojamiento_dashboard.fxml"));
        primaryStage.setTitle("Sistema de Turismo - Gestión de Alojamientos");
        primaryStage.setScene(new Scene(root, 1366, 768));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}