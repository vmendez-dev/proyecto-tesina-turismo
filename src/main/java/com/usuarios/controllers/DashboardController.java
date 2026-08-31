package com.usuarios.controllers;

import com.usuarios.database.Database;
import com.usuarios.model.Usuario;
import com.usuarios.utils.NotificationManager;
import com.usuarios.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.util.Optional;

public class DashboardController {
    private Stage primaryStage;
    private BorderPane root;
    private VBox contentArea;
    private Database db;
    private double xOffset = 0;
    private double yOffset = 0;
    private ToggleGroup menuGroup;

    public DashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.db = Database.getInstance();
        this.root = new BorderPane();
        this.contentArea = new VBox(15);
        contentArea.setPadding(new Insets(12));
        contentArea.setStyle("-fx-background-color: #f1f5f9;");
        this.menuGroup = new ToggleGroup();
        primaryStage.initStyle(StageStyle.UNDECORATED);
    }

    public void mostrar() {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        HBox topBar = crearTopBar();
        root.setTop(topBar);

        VBox sidebar = crearSidebar();
        root.setLeft(sidebar);

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);

        mostrarInicio();

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox crearTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        topBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 16px; -fx-cursor: hand;");
        btnCerrar.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Salir");
            confirm.setHeaderText("¿Desea cerrar la aplicación?");
            confirm.setContentText("Todos los cambios guardados permanecerán.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                primaryStage.close();
            }
        });

        Label lblTitle = new Label("Sistema de Turismo");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUser = new Label("👤 " + SessionManager.getInstance().getUsuarioActual().getNombre() + 
                                 " (" + SessionManager.getInstance().getUsuarioActual().getRol() + ")");
        lblUser.setFont(Font.font("System", 12));
        lblUser.setTextFill(Color.web("#64748b"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-padding: 4 12;");
        btnLogout.setOnAction(e -> confirmarCierreSesion());

        topBar.getChildren().addAll(btnCerrar, lblTitle, spacer, lblUser, btnLogout);
        return topBar;
    }

    private void confirmarCierreSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar sesión");
        confirmacion.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirmacion.setContentText("Todos los cambios guardados permanecerán.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            SessionManager.getInstance().cerrarSesion();
            db.agregarLog("Usuario cerró sesión");
            primaryStage.close();
        }
    }

    private VBox crearSidebar() {
        VBox sidebar = new VBox(3);
        sidebar.setPrefWidth(180);
        sidebar.setPadding(new Insets(8, 8, 8, 12));
        sidebar.setStyle("-fx-background-color: #e0f2fe; -fx-border-color: #cbd5e1; -fx-border-width: 0 1 0 0;");

        Label lblLogo = new Label("Municipalidad");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblLogo.setTextFill(Color.web("#0369a1"));
        lblLogo.setPadding(new Insets(0, 0, 10, 0));

        Label lblSubLogo = new Label("San Antonio de Arredondo");
        lblSubLogo.setFont(Font.font("System", 10));
        lblSubLogo.setTextFill(Color.web("#64748b"));
        lblSubLogo.setPadding(new Insets(-8, 0, 10, 0));

        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 8, 0));

        Label lblTuristas = new Label("TURISTAS");
        lblTuristas.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblTuristas.setTextFill(Color.web("#64748b"));
        lblTuristas.setPadding(new Insets(8, 0, 3, 0));

        String[] menuTuristas = {"Inicio", "Turistas", "Actividades", "Atractivos", 
                                 "Alojamientos", "Gastronomía", "Servicios", "Eventos"};
        
        Label lblPromociones = new Label("PROMOCIONES");
        lblPromociones.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblPromociones.setTextFill(Color.web("#64748b"));
        lblPromociones.setPadding(new Insets(10, 0, 3, 0));

        String[] menuPromociones = {"Reportes", "Usuarios"};

        Label lblConfig = new Label("CONFIGURACIÓN");
        lblConfig.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblConfig.setTextFill(Color.web("#64748b"));
        lblConfig.setPadding(new Insets(10, 0, 3, 0));

        String[] menuConfig = {"Configuración"};

        VBox menuBox = new VBox(2);
        
        for (String item : menuTuristas) {
            ToggleButton btn = crearBotonMenu(item, false);
            menuBox.getChildren().add(btn);
        }

        menuBox.getChildren().add(lblPromociones);
        for (String item : menuPromociones) {
            boolean esActivo = item.equals("Usuarios");
            ToggleButton btn = crearBotonMenu(item, esActivo);
            menuBox.getChildren().add(btn);
        }

        menuBox.getChildren().add(lblConfig);
        for (String item : menuConfig) {
            ToggleButton btn = crearBotonMenu(item, false);
            menuBox.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label lblBackup = new Label("🔄 Backup 30min");
        lblBackup.setFont(Font.font("System", 8));
        lblBackup.setTextFill(Color.web("#94a3b8"));
        lblBackup.setPadding(new Insets(8, 0, 3, 0));

        sidebar.getChildren().addAll(lblLogo, lblSubLogo, separator, menuBox, spacer, lblBackup);
        return sidebar;
    }

    private ToggleButton crearBotonMenu(String texto, boolean activo) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(5, 10, 5, 10));
        btn.setFont(Font.font("System", 12));
        btn.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-text-fill: #334155;");
        btn.setToggleGroup(menuGroup);
        
        if (activo) {
            btn.setSelected(true);
            btn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        }

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
                if (texto.equals("Usuarios")) {
                    mostrarUsuarios();
                } else if (texto.equals("Inicio")) {
                    mostrarInicio();
                }
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #334155;");
            }
        });

        return btn;
    }

    private void limpiarContent() {
        contentArea.getChildren().clear();
    }

    private void mostrarInicio() {
        limpiarContent();
        
        VBox header = new VBox(2);
        Label lblTitle = new Label("Panel de Control");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Resumen del sistema - " + java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblSubtitle.setFont(Font.font("System", 12));
        lblSubtitle.setTextFill(Color.web("#64748b"));
        header.getChildren().addAll(lblTitle, lblSubtitle);

        HBox cards = new HBox(10);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(12, 0, 0, 0));

        long usuariosActivos = db.getUsuarios().stream().filter(u -> u.getEstado().equals("Activo")).count();

        cards.getChildren().addAll(
            crearCardResumen("👥 Usuarios", String.valueOf(db.getUsuarios().size()), 
                "Activos: " + usuariosActivos, "#0284c7"),
            crearCardResumen("📋 Módulos", "8", "Activos", "#8b5cf6"),
            crearCardResumen("🔄 Sync", "Activa", "Última: " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), "#22c55e")
        );

        VBox ultimosSection = new VBox(8);
        ultimosSection.setPadding(new Insets(12, 0, 0, 0));
        Label lblUltimos = new Label("📝 Últimas actividades");
        lblUltimos.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblUltimos.setTextFill(Color.web("#0f172a"));

        ListView<String> logList = new ListView<>();
        logList.setPrefHeight(120);
        db.getLogs(15).forEach(logList.getItems()::add);
        logList.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        ultimosSection.getChildren().addAll(lblUltimos, logList);

        contentArea.getChildren().addAll(header, cards, ultimosSection);
    }

    private VBox crearCardResumen(String titulo, String valor, String detalle, String color) {
        VBox card = new VBox(3);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setMinWidth(140);
        card.setMaxWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", 10));
        lblTitulo.setTextFill(Color.web("#64748b"));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblValor.setTextFill(Color.web(color));

        Label lblDetalle = new Label(detalle);
        lblDetalle.setFont(Font.font("System", 9));
        lblDetalle.setTextFill(Color.web("#94a3b8"));

        card.getChildren().addAll(lblTitulo, lblValor, lblDetalle);
        return card;
    }

    // ============================================================
    // =====               MÓDULO DE USUARIOS                 =====
    // ============================================================

    @SuppressWarnings("unchecked")
    private void mostrarUsuarios() {
        limpiarContent();
        
        // HEADER
        VBox header = new VBox(2);
        Label lblTitle = new Label("Gestión de Usuarios");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Administración de cuentas del sistema");
        lblSubtitle.setFont(Font.font("System", 12));
        lblSubtitle.setTextFill(Color.web("#64748b"));
        header.getChildren().addAll(lblTitle, lblSubtitle);

        // CONTENEDOR TABLA
        VBox tablaContainer = new VBox(12);
        tablaContainer.setPadding(new Insets(12));
        tablaContainer.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // FILTROS
        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Buscar usuario...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");

        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("Todos", "Operador", "Inspector", "Director", "Administrador");
        cbRol.getSelectionModel().selectFirst();
        cbRol.setPrefWidth(100);
        cbRol.setStyle("-fx-font-size: 12px;");

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Todos", "Activo", "Inactivo");
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");

        ComboBox<String> cbDependencia = new ComboBox<>();
        cbDependencia.getItems().addAll("Todas", "Mesa de Entradas", "Inspección General", 
                                        "Sistemas", "Modernización", "Tesorería", "Turismo");
        cbDependencia.getSelectionModel().selectFirst();
        cbDependencia.setPrefWidth(110);
        cbDependencia.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");

        filterBar.getChildren().addAll(txtSearch, new Label("Rol"), cbRol, new Label("Estado"), cbEstado, 
                                       new Label("Dep."), cbDependencia, spacer, btnNuevo);

        // TABLA
        TableView<Usuario> table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty().asObject());
        colId.setPrefWidth(40);

        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Usuario, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(c -> c.getValue().usuarioProperty());
        colUsuario.setPrefWidth(80);

        TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colEmail.setPrefWidth(180);

        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(c -> c.getValue().rolProperty());
        colRol.setPrefWidth(80);

        TableColumn<Usuario, String> colDep = new TableColumn<>("Dependencia");
        colDep.setCellValueFactory(c -> c.getValue().dependenciaProperty());
        colDep.setPrefWidth(110);

        TableColumn<Usuario, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(70);
        colEstado.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + ("Activo".equals(item) ? "#22c55e" : "#ef4444") + 
                            "; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<Usuario, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<Usuario, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                
                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(u, table, txtSearch, cbRol, cbEstado, cbDependencia);
                });
                
                btnToggle.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    String nuevoEstado = u.getEstado().equals("Activo") ? "Inactivo" : "Activo";
                    db.cambiarEstadoUsuario(u.getId(), nuevoEstado);
                    table.refresh();
                    NotificationManager.getInstance().mostrarNotificacion(
                        "Usuario " + u.getUsuario() + " → " + nuevoEstado, "exito");
                });

                btnEliminar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(u, table, txtSearch, cbRol, cbEstado, cbDependencia);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colUsuario, colEmail, colRol, colDep, colEstado, colAcciones);
        table.setItems(db.getUsuarios());

        // FILTRADO
        Runnable filtrar = () -> {
            String search = txtSearch.getText();
            String rol = cbRol.getValue();
            String estado = cbEstado.getValue();
            String dep = cbDependencia.getValue();

            ObservableList<Usuario> filtrados = FXCollections.observableArrayList();
            for (Usuario u : db.getUsuarios()) {
                boolean matchSearch = search.isEmpty() || 
                    u.getNombre().toLowerCase().contains(search.toLowerCase()) || 
                    u.getUsuario().toLowerCase().contains(search.toLowerCase()) ||
                    u.getEmail().toLowerCase().contains(search.toLowerCase());
                boolean matchRol = rol.equals("Todos") || u.getRol().equals(rol);
                boolean matchEstado = estado.equals("Todos") || u.getEstado().equals(estado);
                boolean matchDep = dep.equals("Todas") || u.getDependencia().equals(dep);
                if (matchSearch && matchRol && matchEstado && matchDep) {
                    filtrados.add(u);
                }
            }
            table.setItems(filtrados);
        };

        txtSearch.textProperty().addListener((obs, old, val) -> filtrar.run());
        cbRol.setOnAction(e -> filtrar.run());
        cbEstado.setOnAction(e -> filtrar.run());
        cbDependencia.setOnAction(e -> filtrar.run());

        btnNuevo.setOnAction(e -> {
            mostrarDialogoAlta(table, txtSearch, cbRol, cbEstado, cbDependencia);
        });

        tablaContainer.getChildren().addAll(filterBar, table);

        // CARDS DE RESUMEN
        HBox cards = new HBox(10);
        cards.setPadding(new Insets(8, 0, 0, 0));
        cards.setAlignment(Pos.CENTER);

        VBox card1 = crearCardResumen("Total", String.valueOf(db.getUsuarios().size()), "usuarios", "#2563eb");
        VBox card2 = crearCardResumen("Activos", 
            String.valueOf(db.getUsuarios().stream().filter(u -> u.getEstado().equals("Activo")).count()), 
            "usuarios", "#22c55e");
        VBox card3 = crearCardResumen("Último", 
            db.getUsuarios().isEmpty() ? "---" : db.getUsuarios().get(db.getUsuarios().size()-1).getNombre(), 
            "usuario", "#8b5cf6");

        cards.getChildren().addAll(card1, card2, card3);

        contentArea.getChildren().addAll(header, tablaContainer, cards);
    }

    // ===== DIÁLOGOS =====

    private void mostrarDialogoAlta(TableView<Usuario> table, TextField txtSearch, 
                                    ComboBox<String> cbRol, ComboBox<String> cbEstado, 
                                    ComboBox<String> cbDependencia) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Complete los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");
        txtNombre.setPrefWidth(200);
        txtNombre.setStyle("-fx-font-size: 12px;");
        
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setStyle("-fx-font-size: 12px;");
        
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        txtEmail.setStyle("-fx-font-size: 12px;");
        
        ComboBox<String> cbRolDialog = new ComboBox<>(FXCollections.observableArrayList(
            "Operador", "Inspector", "Director", "Administrador"));
        cbRolDialog.setValue("Operador");
        cbRolDialog.setStyle("-fx-font-size: 12px;");
        
        TextField txtDependencia = new TextField();
        txtDependencia.setPromptText("Dependencia");
        txtDependencia.setStyle("-fx-font-size: 12px;");
        
        PasswordField txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña (6+ car.)");
        txtContrasena.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(txtUsuario, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(cbRolDialog, 1, 3);
        grid.add(new Label("Dependencia:"), 0, 4);
        grid.add(txtDependencia, 1, 4);
        grid.add(new Label("Contraseña:"), 0, 5);
        grid.add(txtContrasena, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                if (txtContrasena.getText().length() < 6) {
                    NotificationManager.getInstance().mostrarNotificacion("Contraseña: mínimo 6 caracteres", "error");
                    return null;
                }
                int id = db.getNextUsuarioId();
                return new Usuario(id, txtNombre.getText(), txtUsuario.getText(), 
                    txtEmail.getText(), cbRolDialog.getValue(), txtDependencia.getText(), 
                    "Activo", txtContrasena.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            db.insertarUsuario(u);
            actualizarTablaUsuarios(table, txtSearch, cbRol, cbEstado, cbDependencia);
            db.agregarLog("Usuario creado: " + u.getUsuario());
            NotificationManager.getInstance().mostrarNotificacion("Usuario " + u.getNombre() + " creado", "exito");
            mostrarUsuarios();
        });
    }

    private void mostrarDialogoEditar(Usuario usuario, TableView<Usuario> table, 
                                      TextField txtSearch, ComboBox<String> cbRol, 
                                      ComboBox<String> cbEstado, ComboBox<String> cbDependencia) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField(usuario.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        
        TextField txtUsuario = new TextField(usuario.getUsuario());
        txtUsuario.setStyle("-fx-font-size: 12px;");
        
        TextField txtEmail = new TextField(usuario.getEmail());
        txtEmail.setStyle("-fx-font-size: 12px;");
        
        ComboBox<String> cbRolDialog = new ComboBox<>(FXCollections.observableArrayList(
            "Operador", "Inspector", "Director", "Administrador"));
        cbRolDialog.setValue(usuario.getRol());
        cbRolDialog.setStyle("-fx-font-size: 12px;");
        
        TextField txtDependencia = new TextField(usuario.getDependencia());
        txtDependencia.setStyle("-fx-font-size: 12px;");
        
        ComboBox<String> cbEstadoDialog = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstadoDialog.setValue(usuario.getEstado());
        cbEstadoDialog.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(txtUsuario, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(cbRolDialog, 1, 3);
        grid.add(new Label("Dependencia:"), 0, 4);
        grid.add(txtDependencia, 1, 4);
        grid.add(new Label("Estado:"), 0, 5);
        grid.add(cbEstadoDialog, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Usuario(usuario.getId(), txtNombre.getText(), txtUsuario.getText(),
                    txtEmail.getText(), cbRolDialog.getValue(), txtDependencia.getText(),
                    cbEstadoDialog.getValue(), usuario.getContrasena());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            db.actualizarUsuario(u);
            actualizarTablaUsuarios(table, txtSearch, cbRol, cbEstado, cbDependencia);
            db.agregarLog("Usuario editado: " + u.getUsuario());
            NotificationManager.getInstance().mostrarNotificacion("Usuario " + u.getNombre() + " actualizado", "exito");
            mostrarUsuarios();
        });
    }

    private void mostrarDialogoEliminar(Usuario usuario, TableView<Usuario> table, 
                                       TextField txtSearch, ComboBox<String> cbRol, 
                                       ComboBox<String> cbEstado, ComboBox<String> cbDependencia) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Usuario");
        confirmacion.setHeaderText("¿Está seguro de eliminar este usuario?");
        confirmacion.setContentText("Usuario: " + usuario.getNombre() + " (" + usuario.getUsuario() + ")");
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarUsuario(usuario.getId())) {
                actualizarTablaUsuarios(table, txtSearch, cbRol, cbEstado, cbDependencia);
                db.agregarLog("Usuario eliminado: " + usuario.getUsuario());
                NotificationManager.getInstance().mostrarNotificacion("Usuario eliminado", "exito");
                mostrarUsuarios();
            }
        }
    }

    private void actualizarTablaUsuarios(TableView<Usuario> table, TextField txtSearch, 
                                        ComboBox<String> cbRol, ComboBox<String> cbEstado, 
                                        ComboBox<String> cbDependencia) {
        String search = txtSearch.getText();
        String rol = cbRol.getValue();
        String estado = cbEstado.getValue();
        String dep = cbDependencia.getValue();

        ObservableList<Usuario> filtrados = FXCollections.observableArrayList();
        for (Usuario u : db.getUsuarios()) {
            boolean matchSearch = search.isEmpty() || 
                u.getNombre().toLowerCase().contains(search.toLowerCase()) || 
                u.getUsuario().toLowerCase().contains(search.toLowerCase()) ||
                u.getEmail().toLowerCase().contains(search.toLowerCase());
            boolean matchRol = rol.equals("Todos") || u.getRol().equals(rol);
            boolean matchEstado = estado.equals("Todos") || u.getEstado().equals(estado);
            boolean matchDep = dep.equals("Todas") || u.getDependencia().equals(dep);
            if (matchSearch && matchRol && matchEstado && matchDep) {
                filtrados.add(u);
            }
        }
        table.setItems(filtrados);
    }
}