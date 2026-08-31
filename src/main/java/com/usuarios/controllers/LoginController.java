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
import javafx.stage.StageStyle;

public class LoginController {
    private Stage primaryStage;
    private TextField txtUsuario;
    private PasswordField txtContrasena;
    private TextField txtContrasenaVisible;
    private Label lblError;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean passwordVisible = false;

    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);
    }

    public void mostrar() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        VBox leftPanel = new VBox();
        leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #0284c7, #0369a1);");
        leftPanel.setPrefWidth(280);
        
        VBox loginContainer = new VBox(15);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(30));
        loginContainer.setStyle("-fx-background-color: white;");
        loginContainer.setPrefWidth(400);

        VBox logoBox = new VBox(3);
        logoBox.setAlignment(Pos.CENTER);
        
        Label lblTitle = new Label("Municipalidad");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitle.setTextFill(Color.web("#0f172a"));

        Label lblSubtitle = new Label("San Antonio de Arredondo");
        lblSubtitle.setFont(Font.font("System", 13));
        lblSubtitle.setTextFill(Color.web("#64748b"));

        Label lblLoginTitle = new Label("Registro de Visitantes");
        lblLoginTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblLoginTitle.setTextFill(Color.web("#0f172a"));
        lblLoginTitle.setPadding(new Insets(15, 0, 8, 0));

        logoBox.getChildren().addAll(lblTitle, lblSubtitle, lblLoginTitle);

        VBox formBox = new VBox(12);
        formBox.setPadding(new Insets(5, 0, 0, 0));

        txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-font-size: 13px;");

        HBox passwordBox = new HBox(0);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        passwordBox.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña");
        txtContrasena.setStyle("-fx-padding: 10 0 10 10; -fx-background-color: transparent; -fx-font-size: 13px;");
        txtContrasena.setPrefHeight(38);
        HBox.setHgrow(txtContrasena, Priority.ALWAYS);
        txtContrasena.setOnAction(e -> iniciarSesion());

        txtContrasenaVisible = new TextField();
        txtContrasenaVisible.setPromptText("Contraseña");
        txtContrasenaVisible.setStyle("-fx-padding: 10 0 10 10; -fx-background-color: transparent; -fx-font-size: 13px;");
        txtContrasenaVisible.setPrefHeight(38);
        HBox.setHgrow(txtContrasenaVisible, Priority.ALWAYS);
        txtContrasenaVisible.setVisible(false);
        txtContrasenaVisible.setOnAction(e -> iniciarSesion());

        Button btnOjo = new Button("👁");
        btnOjo.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 8 12;");
        btnOjo.setPrefHeight(38);
        btnOjo.setOnAction(e -> togglePasswordVisibility());

        txtContrasena.textProperty().addListener((obs, old, val) -> {
            if (!txtContrasenaVisible.isFocused()) {
                txtContrasenaVisible.setText(val);
            }
        });
        txtContrasenaVisible.textProperty().addListener((obs, old, val) -> {
            if (!txtContrasena.isFocused()) {
                txtContrasena.setText(val);
            }
        });

        passwordBox.getChildren().addAll(txtContrasena, txtContrasenaVisible, btnOjo);

        lblError = new Label();
        lblError.setTextFill(Color.web("#ef4444"));
        lblError.setFont(Font.font("System", 11));
        lblError.setVisible(false);

        Hyperlink lnkOlvido = new Hyperlink("¿Olvidó su contraseña?");
        lnkOlvido.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 12px;");
        lnkOlvido.setOnAction(e -> mostrarDialogoRecuperar());

        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 6; -fx-padding: 10; -fx-font-size: 14px;");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> iniciarSesion());

        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 16px; -fx-cursor: hand;");
        btnCerrar.setOnAction(e -> primaryStage.close());

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.TOP_RIGHT);
        titleBar.setPadding(new Insets(10, 10, 0, 0));
        titleBar.getChildren().add(btnCerrar);

        formBox.getChildren().addAll(txtUsuario, passwordBox, lblError, lnkOlvido, btnLogin);
        loginContainer.getChildren().addAll(logoBox, formBox);

        VBox leftContent = new VBox();
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setPadding(new Insets(30));
        
        Label lblWelcome = new Label("Bienvenido");
        lblWelcome.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblWelcome.setTextFill(Color.WHITE);
        
        Label lblDesc = new Label("Sistema de Gestión Municipal\nSan Antonio de Arredondo");
        lblDesc.setFont(Font.font("System", 12));
        lblDesc.setTextFill(Color.web("#bfdbfe"));
        lblDesc.setAlignment(Pos.CENTER);
        
        leftContent.getChildren().addAll(lblWelcome, lblDesc);
        leftPanel.getChildren().addAll(leftContent);

        HBox mainContainer = new HBox();
        mainContainer.getChildren().addAll(leftPanel, loginContainer);

        root.setTop(titleBar);
        root.setCenter(mainContainer);

        Scene scene = new Scene(root, 680, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            txtContrasenaVisible.setText(txtContrasena.getText());
            txtContrasena.setVisible(false);
            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.requestFocus();
            txtContrasenaVisible.positionCaret(txtContrasenaVisible.getText().length());
        } else {
            txtContrasena.setText(txtContrasenaVisible.getText());
            txtContrasenaVisible.setVisible(false);
            txtContrasena.setVisible(true);
            txtContrasena.requestFocus();
            txtContrasena.positionCaret(txtContrasena.getText().length());
        }
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = passwordVisible ? txtContrasenaVisible.getText() : txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblError.setText("⚠️ Usuario y contraseña son obligatorios");
            lblError.setVisible(true);
            return;
        }

        Database db = Database.getInstance();
        
        Usuario user = null;
        for (Usuario u : db.getUsuarios()) {
            if (u.getUsuario().equals(usuario) && u.getContrasena().equals(contrasena)) {
                user = u;
                break;
            }
        }

        if (user != null && user.getEstado().equals("Activo")) {
            SessionManager.getInstance().iniciarSesion(user);
            db.agregarLog("Usuario " + user.getUsuario() + " inició sesión");
            MainApp.getInstance().mostrarDashboard();
        } else if (user != null && !user.getEstado().equals("Activo")) {
            lblError.setText("⚠️ Usuario inactivo. Contacte al administrador.");
            lblError.setVisible(true);
        } else {
            lblError.setText("⚠️ Usuario o contraseña incorrectos");
            lblError.setVisible(true);
            db.agregarLog("Intento de login fallido: " + usuario);
        }
    }

    private void mostrarDialogoRecuperar() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar Contraseña");
        alert.setHeaderText("Contacte al administrador");
        alert.setContentText("Para recuperar su contraseña, comuníquese con el área de Sistemas.");
        alert.showAndWait();
    }
}