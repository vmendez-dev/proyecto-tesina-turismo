package Servicio;

import Conexion.Database;
import Servicio.Servicio;
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

public class ServicioDAO {
    private Database db;
    private TableView<Servicio> table;
    private TextField txtSearch;
    private ComboBox<String>  cbEstado;

    public ServicioDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🚨 Gestión de Servicios de Seguridad y Emergencia");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Listado de servicios de seguridad y emergencia del municipio");
        lblSubtitle.setFont(Font.font("System", 13));
        lblSubtitle.setTextFill(Color.web("#64748b"));

        VBox header = new VBox(2);
        header.getChildren().addAll(lblTitle, lblSubtitle);

        VBox tablaContainer = crearTabla();

        container.getChildren().addAll(header, tablaContainer);
        return container;
    }

    private VBox crearTabla() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar servicio...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());


        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Todos", "Activo", "Inactivo");
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");
        cbEstado.setOnAction(e -> filtrar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnNuevo.setOnAction(e -> mostrarDialogoAlta());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Servicio, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(60);
        colId.setVisible(false);

        TableColumn<Servicio, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Servicio, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colTipo.setPrefWidth(120);
        colTipo.setCellFactory(col -> new TableCell<Servicio, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "Policía" -> "#1e40af";
                        case "Bomberos" -> "#dc2626";
                        case "Hospital" -> "#16a34a";
                        case "Defensa Civil" -> "#f59e0b";
                        case "Protección Civil" -> "#8b5cf6";
                        default -> "#64748b";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<Servicio, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(c -> c.getValue().direccionProperty());
        colDireccion.setPrefWidth(180);

        TableColumn<Servicio, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colTelefono.setPrefWidth(120);

        TableColumn<Servicio, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        colHorario.setPrefWidth(100);

        TableColumn<Servicio, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(80);
        colEstado.setCellFactory(col -> new TableCell<Servicio, String>() {
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

        TableColumn<Servicio, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<Servicio, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    Servicio s = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(s);
                });

                btnToggle.setOnAction(e -> {
                    Servicio s = getTableView().getItems().get(getIndex());
                    String nuevoEstado = s.getEstado().equals("Activo") ? "Inactivo" : "Activo";
                    db.cambiarEstadoServicio(s.getId(), nuevoEstado);
                    s.setEstado(nuevoEstado);
                    table.refresh();
                });

                btnEliminar.setOnAction(e -> {
                    Servicio s = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colTipo, colDireccion, colTelefono, colHorario, colEstado, colAcciones);
        table.setItems(db.getServicios());

        container.getChildren().addAll(filterBar, table);
        return container;
    }
    private void filtrar() {
        String search = txtSearch.getText().toLowerCase();
        String estado = cbEstado.getValue();

        ObservableList<Servicio> filtrados = FXCollections.observableArrayList();
        for (Servicio s : db.getServicios()) {
            boolean matchSearch = search.isEmpty() ||
                    s.getNombre().toLowerCase().contains(search) ||
                    s.getTipo().toLowerCase().contains(search) ||
                    s.getDireccion().toLowerCase().contains(search);
            boolean matchEstado = estado.equals("Todos") || s.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(s);
            }
        }
        table.setItems(filtrados);
    }

    private Label crearAvisoEspacio() {
        Label aviso = new Label("⚠ No se permiten espacios al comienzo del campo.");
        aviso.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        aviso.setVisible(false);
        aviso.setWrapText(true);
        aviso.setMaxWidth(320);
        return aviso;
    }

    private void bloquearEspacioInicial(TextField campo, Label aviso) {
        campo.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                aviso.setVisible(true);
                return null;
            }
            aviso.setVisible(false);
            return change;
        }));
    }
    private void filtrarCampo(TextField campo, Label aviso, String regex, String mensaje) {
        campo.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                aviso.setText("⚠ No se permiten espacios al comienzo del campo.");
                aviso.setVisible(true);
                return null;
            }
            if (change.isAdded() && !nuevo.matches(regex)) {
                aviso.setText(mensaje);
                aviso.setVisible(true);
                return null;
            }
            aviso.setVisible(false);
            return change;
        }));
    }

        private void mostrarDialogoAlta() {
        Dialog<Servicio> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Servicio de Emergencia");
        dialog.setHeaderText("Complete los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        txtNombre.setStyle("-fx-font-size: 12px;");

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Policía", "Bomberos", "Hospital", "Defensa Civil", "Protección Civil", "Emergencia Médica"));
        cbTipo.setValue("Policía");
        cbTipo.setStyle("-fx-font-size: 12px;");

        TextField txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección");
        txtDireccion.setStyle("-fx-font-size: 12px;");

        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        txtTelefono.setStyle("-fx-font-size: 12px;");

        TextField txtHorario = new TextField();
            Label lblErrorNombre = new Label();
            lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
            lblErrorNombre.setWrapText(true);
            lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
            lblErrorNombre.setPrefWidth(320);

            Label lblErrorTipo = new Label();
            lblErrorTipo.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
            lblErrorTipo.setWrapText(true);
            lblErrorTipo.setMaxWidth(Double.MAX_VALUE);
            lblErrorTipo.setPrefWidth(320);

            Label lblErrorDireccion = new Label();
            lblErrorDireccion.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
            lblErrorDireccion.setWrapText(true);
            lblErrorDireccion.setMaxWidth(Double.MAX_VALUE);
            lblErrorDireccion.setPrefWidth(320);

            Label lblErrorTelefono = new Label();
            lblErrorTelefono.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
            lblErrorTelefono.setWrapText(true);
            lblErrorTelefono.setMaxWidth(Double.MAX_VALUE);
            lblErrorTelefono.setPrefWidth(320);
            txtHorario.setPromptText("Horario (ej: 24h, Cerrado, 08:00/20:00)");
            txtHorario.setStyle("-fx-font-size: 12px;");



        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo*:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Dirección*:"), 0, 2);
        grid.add(txtDireccion, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Horario:"), 0, 4);
        grid.add(txtHorario, 1, 4);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 6);
        GridPane.setColumnSpan(lblErrorTipo, 2);
        grid.add(lblErrorTipo, 0, 7);
        GridPane.setColumnSpan(lblErrorDireccion, 2);
        grid.add(lblErrorDireccion, 0, 8);
        GridPane.setColumnSpan(lblErrorTelefono, 2);
        grid.add(lblErrorTelefono, 0, 9);

        Label lblEspacio = crearAvisoEspacio();
        bloquearEspacioInicial(txtNombre, lblEspacio);
        bloquearEspacioInicial(txtDireccion, lblEspacio);
        filtrarCampo(txtTelefono, lblEspacio, "[0-9\\s\\-]{0,15}", "⚠ Solo se permiten números, espacios y guiones (máx. 15)");
        filtrarCampo(txtHorario, lblEspacio, "[0-9:/hCerado]{0,20}", "⚠ Formato: 24h, Cerrado o HH:MM/HH:MM");
        grid.add(lblEspacio, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

            Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);

            btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                boolean valido = true;

                if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                    lblErrorNombre.setText("El nombre es obligatorio");
                    valido = false;
                } else {
                    lblErrorNombre.setText("");
                }

                if (cbTipo.getValue() == null || cbTipo.getValue().trim().isEmpty()) {
                    lblErrorTipo.setText("El tipo es obligatorio");
                    valido = false;
                } else {
                    lblErrorTipo.setText("");
                }

                if (txtDireccion.getText() == null || txtDireccion.getText().trim().isEmpty()) {
                    lblErrorDireccion.setText("La dirección es obligatoria");
                    valido = false;
                } else {
                    lblErrorDireccion.setText("");
                }

                String tel = txtTelefono.getText().trim();
                if (tel.length() < 3 || !tel.matches("[0-9\\s\\-]{3,15}")) {
                    lblErrorTelefono.setText("El teléfono debe tener entre 3 y 15 caracteres (números, espacios y guiones)");
                    valido = false;
                } else {
                    lblErrorTelefono.setText("");
                }

                if (!valido) {
                    ev.consume();
                }
            });

            dialog.setResultConverter(btn -> {
                if (btn == guardarBtn) {
                    return new Servicio("0", txtNombre.getText(), cbTipo.getValue(),
                            txtDireccion.getText(), txtTelefono.getText(), txtHorario.getText().trim(),
                            "Activo");
                }
                return null;
            });

        dialog.showAndWait().ifPresent(s -> {
            db.insertarServicio(s);
            filtrar();
        });

    }

    private void mostrarDialogoEditar(Servicio servicio) {
        Dialog<Servicio> dialog = new Dialog<>();
        dialog.setTitle("Editar Servicio");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField(servicio.getNombre().trim());
        txtNombre.setStyle("-fx-font-size: 12px;");

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Policía", "Bomberos", "Hospital", "Defensa Civil", "Protección Civil", "Emergencia Médica"));
        cbTipo.setValue(servicio.getTipo());
        cbTipo.setStyle("-fx-font-size: 12px;");

        TextField txtDireccion = new TextField(servicio.getDireccion().trim());
        txtDireccion.setStyle("-fx-font-size: 12px;");

        TextField txtTelefono = new TextField(servicio.getTelefono().trim());
        txtTelefono.setStyle("-fx-font-size: 12px;");

        TextField txtHorario = new TextField(servicio.getHorario().trim());
        Label lblErrorNombre = new Label();
        lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorNombre.setWrapText(true);
        lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);

        Label lblErrorTipo = new Label();
        lblErrorTipo.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorTipo.setWrapText(true);
        lblErrorTipo.setMaxWidth(Double.MAX_VALUE);
        lblErrorTipo.setPrefWidth(320);

        Label lblErrorDireccion = new Label();
        lblErrorDireccion.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorDireccion.setWrapText(true);
        lblErrorDireccion.setMaxWidth(Double.MAX_VALUE);
        lblErrorDireccion.setPrefWidth(320);

        Label lblErrorTelefono = new Label();
        lblErrorTelefono.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorTelefono.setWrapText(true);
        lblErrorTelefono.setMaxWidth(Double.MAX_VALUE);
        lblErrorTelefono.setPrefWidth(320);
        txtHorario.setStyle("-fx-font-size: 12px;");



        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo*:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Dirección*:"), 0, 2);
        grid.add(txtDireccion, 1, 2);
        grid.add(new Label("Teléfono*:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Horario:"), 0, 4);
        grid.add(txtHorario, 1, 4);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 7);
        GridPane.setColumnSpan(lblErrorTipo, 2);
        grid.add(lblErrorTipo, 0, 8);
        GridPane.setColumnSpan(lblErrorDireccion, 2);
        grid.add(lblErrorDireccion, 0, 9);
        GridPane.setColumnSpan(lblErrorTelefono, 2);
        grid.add(lblErrorTelefono, 0, 10);


        if ("Inactivo".equals(servicio.getEstado())) {
            txtNombre.setDisable(true);
            cbTipo.setDisable(true);
            txtDireccion.setDisable(true);
            txtTelefono.setDisable(true);
            txtHorario.setDisable(true);

            Label lblAviso = new Label("⚠ Servicio inactivo: no se puede editar. Reactívelo primero.");
            lblAviso.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
            lblAviso.setWrapText(true);
            lblAviso.setMaxWidth(320);
            grid.add(lblAviso, 0, 5, 2, 1);

            dialog.getDialogPane().lookupButton(guardarBtn).setDisable(true);
        }
        Label lblEspacio = crearAvisoEspacio();
        bloquearEspacioInicial(txtNombre, lblEspacio);
        bloquearEspacioInicial(txtDireccion, lblEspacio);
        filtrarCampo(txtTelefono, lblEspacio, "[0-9\\s\\-]{0,15}", "⚠ Solo se permiten números, espacios y guiones (máx. 15)");
        filtrarCampo(txtHorario, lblEspacio, "[0-9:/hCerado]{0,20}", "⚠ Formato: 24h, Cerrado o HH:MM/HH:MM");
        grid.add(lblEspacio, 0, 6, 2, 1);
        dialog.getDialogPane().setContent(grid);
        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);

        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            boolean valido = true;

            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                lblErrorNombre.setText("El nombre es obligatorio");
                valido = false;
            } else {
                lblErrorNombre.setText("");
            }

            if (cbTipo.getValue() == null || cbTipo.getValue().trim().isEmpty()) {
                lblErrorTipo.setText("El tipo es obligatorio");
                valido = false;
            } else {
                lblErrorTipo.setText("");
            }

            if (txtDireccion.getText() == null || txtDireccion.getText().trim().isEmpty()) {
                lblErrorDireccion.setText("La dirección es obligatoria");
                valido = false;
            } else {
                lblErrorDireccion.setText("");
            }

            String tel = txtTelefono.getText().trim();
            if (tel.length() < 3 || !tel.matches("[0-9\\s\\-]{3,15}")) {
                lblErrorTelefono.setText("El teléfono debe tener entre 3 y 15 caracteres (números, espacios y guiones)");
                valido = false;
            } else {
                lblErrorTelefono.setText("");
            }

            if (!valido) {
                ev.consume();
            }
        });


        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Servicio(servicio.getId(), txtNombre.getText(), cbTipo.getValue(),
                        txtDireccion.getText(), txtTelefono.getText(), txtHorario.getText(),
                        servicio.getEstado());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            db.actualizarServicio(s);
            filtrar();
        });
    }

    private void mostrarDialogoEliminar(Servicio servicio) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Servicio");
        confirmacion.setHeaderText("¿Está seguro de eliminar este servicio?");
        confirmacion.setContentText("Servicio: " + servicio.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarServicio(servicio.getId())) {
                filtrar();
            }
        }
    }
}