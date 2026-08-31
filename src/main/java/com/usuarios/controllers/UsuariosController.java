package com.usuarios.controllers;

import com.usuarios.database.Database;
import com.usuarios.model.Usuario;
import com.usuarios.utils.NotificationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

public class UsuariosController {
    private VBox contentArea;
    private Database db;
    private TableView<Usuario> table;
    private ObservableList<Usuario> usuarios;
    private TextField txtSearch;
    private ComboBox<String> cbRol, cbEstado, cbDependencia;
    private Label lblTotal, lblActivos, lblUltimoAgregado;

    public UsuariosController(VBox contentArea) {
        this.contentArea = contentArea;
        this.db = Database.getInstance();
        this.usuarios = db.getUsuarios();
    }

    public void mostrar() {
        VBox header = crearHeader();
        VBox tablaSection = crearTabla();
        HBox cards = crearCardsResumen();

        contentArea.getChildren().addAll(header, tablaSection, cards);
    }

    private VBox crearHeader() {
        VBox header = new VBox(2);
        Label lblTitle = new Label("Gestión de Usuarios");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Administración de cuentas del sistema");
        lblSubtitle.setFont(Font.font("System", 12));
        lblSubtitle.setTextFill(Color.web("#64748b"));
        header.getChildren().addAll(lblTitle, lblSubtitle);
        return header;
    }

    private VBox crearTabla() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar usuario...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());

        cbRol = new ComboBox<>(FXCollections.observableArrayList("Todos", "Operador", "Inspector", "Director", "Administrador"));
        cbRol.getSelectionModel().selectFirst();
        cbRol.setPrefWidth(100);
        cbRol.setStyle("-fx-font-size: 12px;");
        cbRol.setOnAction(e -> filtrar());

        cbEstado = new ComboBox<>(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");
        cbEstado.setOnAction(e -> filtrar());

        cbDependencia = new ComboBox<>(FXCollections.observableArrayList("Todas", "Mesa de Entradas", "Inspección General", 
                                         "Sistemas", "Modernización", "Tesorería", "Turismo"));
        cbDependencia.getSelectionModel().selectFirst();
        cbDependencia.setPrefWidth(110);
        cbDependencia.setStyle("-fx-font-size: 12px;");
        cbDependencia.setOnAction(e -> filtrar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnNuevo.setOnAction(e -> mostrarDialogoAlta());

        filterBar.getChildren().addAll(txtSearch, new Label("Rol"), cbRol, new Label("Estado"), cbEstado, 
                                       new Label("Dep."), cbDependencia, spacer, btnNuevo);

        table = new TableView<>();
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
                    mostrarDialogoEditar(u);
                });
                
                btnToggle.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    String nuevoEstado = u.getEstado().equals("Activo") ? "Inactivo" : "Activo";
                    db.cambiarEstadoUsuario(u.getId(), nuevoEstado);
                    table.refresh();
                    actualizarResumen();
                    NotificationManager.getInstance().mostrarNotificacion(
                        "Usuario " + u.getUsuario() + " → " + nuevoEstado, "exito");
                });

                btnEliminar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(
            colId, colNombre, colUsuario, colEmail, colRol, colDep, colEstado, colAcciones);
        table.setItems(usuarios);

        container.getChildren().addAll(filterBar, table);
        actualizarResumen();
        return container;
    }

    private HBox crearCardsResumen() {
        HBox container = new HBox(10);
        container.setPadding(new Insets(8, 0, 0, 0));
        container.setAlignment(Pos.CENTER);

        VBox card1 = crearCardResumen("Total", "0", "usuarios", "#2563eb");
        VBox card2 = crearCardResumen("Activos", "0", "usuarios", "#22c55e");
        VBox card3 = crearCardResumen("Último", "---", "usuario", "#8b5cf6");

        lblTotal = (Label) card1.getChildren().get(1);
        lblActivos = (Label) card2.getChildren().get(1);
        lblUltimoAgregado = (Label) card3.getChildren().get(1);

        container.getChildren().addAll(card1, card2, card3);
        return container;
    }

    private VBox crearCardResumen(String titulo, String valor, String subtitulo, String color) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setMinWidth(120);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", 10));
        lblTitulo.setTextFill(Color.web("#64748b"));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblValor.setTextFill(Color.web(color));

        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setFont(Font.font("System", 9));
        lblSubtitulo.setTextFill(Color.web("#94a3b8"));

        card.getChildren().addAll(lblTitulo, lblValor, lblSubtitulo);
        return card;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String rol = cbRol.getValue();
        String estado = cbEstado.getValue();
        String dep = cbDependencia.getValue();

        ObservableList<Usuario> filtrados = FXCollections.observableArrayList();
        for (Usuario u : usuarios) {
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
        actualizarResumen();
    }

    private void actualizarResumen() {
        ObservableList<Usuario> lista = table.getItems();
        long activos = lista.stream().filter(u -> u.getEstado().equals("Activo")).count();
        lblTotal.setText(String.valueOf(lista.size()));
        lblActivos.setText(String.valueOf(activos));
        if (!lista.isEmpty()) {
            lblUltimoAgregado.setText(lista.get(lista.size()-1).getNombre());
        }
    }

    private void mostrarDialogoAlta() {
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
            filtrar();
            db.agregarLog("Usuario creado: " + u.getUsuario());
            NotificationManager.getInstance().mostrarNotificacion("Usuario " + u.getNombre() + " creado", "exito");
        });
    }

    private void mostrarDialogoEditar(Usuario usuario) {
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
            filtrar();
            db.agregarLog("Usuario editado: " + u.getUsuario());
            NotificationManager.getInstance().mostrarNotificacion("Usuario " + u.getNombre() + " actualizado", "exito");
        });
    }

    private void mostrarDialogoEliminar(Usuario usuario) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Usuario");
        confirmacion.setHeaderText("¿Está seguro de eliminar este usuario?");
        confirmacion.setContentText("Usuario: " + usuario.getNombre() + " (" + usuario.getUsuario() + ")");
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarUsuario(usuario.getId())) {
                filtrar();
                db.agregarLog("Usuario eliminado: " + usuario.getUsuario());
                NotificationManager.getInstance().mostrarNotificacion("Usuario eliminado", "exito");
            }
        }
    }
}