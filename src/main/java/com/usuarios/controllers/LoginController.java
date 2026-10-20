package com.usuarios.controllers;

import com.usuarios.MainApp;
import com.usuarios.database.Database;
import com.usuarios.model.Usuario;
import com.usuarios.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class LoginController {
    private final Stage primaryStage;
    private TextField txtUsuario;
    private PasswordField txtContrasena;
    private TextField txtContrasenaVisible;
    private Label lblError;

    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void mostrar() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        VBox leftPanel = new VBox();
        leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #0284c7, #0369a1);");
        leftPanel.setPrefWidth(280);
        VBox leftContent = new VBox(8);
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setPadding(new Insets(30));

        Label welcome = new Label("Bienvenido");
        welcome.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        welcome.setTextFill(Color.WHITE);
        Label desc = new Label("Sistema de Gestión Municipal\nSan Antonio de Arredondo");
        desc.setFont(Font.font("Inter", 12));
        desc.setTextFill(Color.web("#bfdbfe"));
        desc.setAlignment(Pos.CENTER);
        leftContent.getChildren().addAll(welcome, desc);
        leftPanel.getChildren().add(leftContent);

        VBox login = new VBox(15);
        login.setAlignment(Pos.CENTER);
        login.setPadding(new Insets(30));
        login.setPrefWidth(400);

        Label title = new Label("Municipalidad");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));
        Label subtitle = new Label("San Antonio de Arredondo");
        subtitle.setTextFill(Color.web("#64748b"));
        Label loginTitle = new Label("Inicio de sesión");
        loginTitle.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        loginTitle.setTextFill(Color.web("#0f172a"));

        txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña");
        txtContrasena.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");
        txtContrasena.setOnAction(e -> iniciarSesion());

        txtContrasenaVisible = new TextField();
        txtContrasenaVisible.setPromptText("Contraseña");
        txtContrasenaVisible.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");
        txtContrasenaVisible.setVisible(false);
        txtContrasenaVisible.setManaged(false);
        txtContrasenaVisible.setOnAction(e -> iniciarSesion());

        Button btnVer = new Button("👁");
        btnVer.setTooltip(new Tooltip("Ver contraseña"));
        btnVer.setFocusTraversable(false);
        btnVer.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 16px; -fx-cursor: hand;");
        btnVer.setOnAction(e -> alternarContrasena());

        HBox passwordContainer = new HBox(4);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtContrasena, Priority.ALWAYS);
        HBox.setHgrow(txtContrasenaVisible, Priority.ALWAYS);
        passwordContainer.getChildren().addAll(txtContrasena, txtContrasenaVisible, btnVer);

        txtContrasena.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!txtContrasenaVisible.isFocused()) txtContrasenaVisible.setText(newValue);
        });
        txtContrasenaVisible.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!txtContrasena.isFocused()) txtContrasena.setText(newValue);
        });

        lblError = new Label();
        lblError.setTextFill(Color.web("#ef4444"));
        lblError.setWrapText(true);
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10;");
        btnLogin.setOnAction(e -> iniciarSesion());

        Hyperlink recuperar = new Hyperlink("¿Olvidó su contraseña?");
        recuperar.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Comuníquese con el área de Sistemas.").showAndWait());

        VBox form = new VBox(12, txtUsuario, passwordContainer, lblError, recuperar, btnLogin);
        login.getChildren().addAll(title, subtitle, loginTitle, form);
        root.setCenter(new HBox(leftPanel, login));

        primaryStage.setTitle("Sistema de Turismo - San Antonio de Arredondo");
        primaryStage.setMaximized(false);
        primaryStage.setResizable(false);
        primaryStage.setWidth(680);
        primaryStage.setHeight(480);
        primaryStage.setScene(new Scene(root, 680, 480));
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.centerOnScreen();
        primaryStage.toFront();
        txtUsuario.requestFocus();
    }

    private void alternarContrasena() {
        boolean mostrar = txtContrasena.isVisible();
        if (mostrar) {
            txtContrasenaVisible.setText(txtContrasena.getText());
            txtContrasena.setVisible(false);
            txtContrasena.setManaged(false);
            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.setManaged(true);
            txtContrasenaVisible.requestFocus();
            txtContrasenaVisible.positionCaret(txtContrasenaVisible.getText().length());
        } else {
            txtContrasena.setText(txtContrasenaVisible.getText());
            txtContrasenaVisible.setVisible(false);
            txtContrasenaVisible.setManaged(false);
            txtContrasena.setVisible(true);
            txtContrasena.setManaged(true);
            txtContrasena.requestFocus();
            txtContrasena.positionCaret(txtContrasena.getText().length());
        }
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();
        if (usuario.isBlank() || contrasena.isBlank()) {
            error("⚠️ Usuario y contraseña son obligatorios.");
            return;
        }

        try {
            Usuario user = Database.getInstance().autenticar(usuario, contrasena);
            if (user == null) {
                error("⚠️ Usuario o contraseña incorrectos.");
                Database.getInstance().agregarLog("Intento de login fallido: " + usuario);
                return;
            }
            if (!"Activo".equals(user.getEstado())) {
                error("⚠️ Usuario inactivo. Contacte al administrador.");
                return;
            }

            SessionManager.getInstance().iniciarSesion(user);
            Database.getInstance().agregarLog("Usuario " + user.getUsuario() + " inició sesión");
            MainApp.getInstance().mostrarDashboard();
        } catch (RuntimeException e) {
            error("⚠️ " + e.getMessage());
        }
    }

    private void error(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
