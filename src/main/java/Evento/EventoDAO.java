package Evento;

import Conexion.Database;
import Evento.Evento;
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

public class EventoDAO {
    private Database db;
    private TableView<Evento> table;
    private TextField txtSearch;
    private ComboBox<String> cbEstado;

    public EventoDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🎪 Gestión de Eventos");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Planificación y seguimiento de eventos turísticos");
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
        txtSearch.setPromptText("Buscar evento...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Todos", "Activo", "Finalizado", "Cancelado");
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");
        cbEstado.setOnAction(e -> filtrar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnNuevo.setOnAction(e -> mostrarDialogoAlta());

        Button btnPapelera = new Button("🗑 Papelera");
        btnPapelera.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnPapelera.setOnAction(e -> mostrarDialogoPapelera());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnPapelera, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Evento, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);
        colId.setVisible(false);

        TableColumn<Evento, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Evento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            String fecha = c.getValue().getFecha();
            String horario = c.getValue().getHorario();
            String texto = (horario != null && !horario.trim().isEmpty()) ? fecha + " · " + horario : fecha;
            return new javafx.beans.property.SimpleStringProperty(texto);
        });
        colFecha.setPrefWidth(180);

        TableColumn<Evento, String> colLugar = new TableColumn<>("Lugar");
        colLugar.setCellValueFactory(c -> c.getValue().lugarProperty());
        colLugar.setPrefWidth(130);

        TableColumn<Evento, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(180);

        TableColumn<Evento, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(80);
        colEstado.setCellFactory(col -> new TableCell<Evento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "Activo" -> "#22c55e";
                        case "Finalizado" -> "#8b5cf6";
                        default -> "#ef4444";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<Evento, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<Evento, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(ev);
                });

                btnToggle.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    String[] estados = {"Activo", "Finalizado", "Cancelado"};
                    int idx = java.util.Arrays.asList(estados).indexOf(ev.getEstado());
                    String nuevo = estados[(idx + 1) % estados.length];
                    db.cambiarEstadoEvento(ev.getId(), nuevo);
                    ev.setEstado(nuevo);
                    table.refresh();
                });

                btnEliminar.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(ev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colFecha, colLugar, colDesc, colEstado, colAcciones);
        table.setItems(db.getEventos());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String estado = cbEstado.getValue();

        ObservableList<Evento> filtrados = FXCollections.observableArrayList();
        for (Evento e : db.getEventos()) {
            boolean matchSearch = search.isEmpty() ||
                    e.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                    e.getLugar().toLowerCase().contains(search.toLowerCase());
            boolean matchEstado = estado.equals("Todos") || e.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(e);
            }
        }
        table.setItems(filtrados);
    }

    private void  mostrarDialogoAlta() {
        Dialog<Evento> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Evento");
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

        Label lblErrorNombre = new Label();
        lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorNombre.setWrapText(true);
        lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);
        TextField txtFecha = new TextField();
        txtFecha.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> { String newText = change.getControlNewText(); if (newText.matches("[0-9/\\-]*")) { return change; } return null; }));
        txtFecha.setPromptText("Fecha (dd/mm/aaaa)");
        txtFecha.setStyle("-fx-font-size: 12px;");
        Label lblErrorFecha = new Label();
        lblErrorFecha.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorFecha.setWrapText(true);
        lblErrorFecha.setMaxWidth(Double.MAX_VALUE);
        lblErrorFecha.setPrefWidth(320);
        TextField txtHorario = new TextField();
        txtHorario.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9:/]*")) {
                return change;
            }
            return null;
        }));
        txtHorario.setPromptText("Horario (ej: 18:00/22:00)");
        Label lblErrorHorario = new Label();
        lblErrorHorario.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorHorario.setWrapText(true);
        lblErrorHorario.setMaxWidth(Double.MAX_VALUE);
        lblErrorHorario.setPrefWidth(320);
        txtHorario.setStyle("-fx-font-size: 12px;");
        TextField txtLugar = new TextField();
        txtLugar.setPromptText("Lugar");
        txtLugar.setStyle("-fx-font-size: 12px;");
        Label lblErrorLugar = new Label();
        lblErrorLugar.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorLugar.setWrapText(true);
        lblErrorLugar.setMaxWidth(Double.MAX_VALUE);
        lblErrorLugar.setPrefWidth(320);
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descripción");
        txtDesc.setStyle("-fx-font-size: 12px;");
        txtDesc.setPrefRowCount(3);
        txtDesc.setWrapText(true);
        txtDesc.setPrefWidth(250);

        Label lblContador = new Label("0/255 caracteres");
        Label lblErrorDesc = new Label();
        lblErrorDesc.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorDesc.setWrapText(true);
        lblErrorDesc.setMaxWidth(Double.MAX_VALUE);
        lblErrorDesc.setPrefWidth(320);
        lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        txtDesc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 255) {
                txtDesc.setText(oldVal);
                return;
            }
            lblContador.setText(newVal.length() + "/255 caracteres");
            if (newVal.length() >= 230) {
                lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            } else {
                lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
            }
        });


        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Horario:"), 0, 6);
        grid.add(txtHorario, 1, 6);
        GridPane.setColumnSpan(lblErrorHorario, 2);
        grid.add(lblErrorHorario, 0, 12);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 6);
        grid.add(new Label("Fecha*:"), 0, 1);
        grid.add(txtFecha, 1, 1);
        GridPane.setColumnSpan(lblErrorFecha, 2);
        grid.add(lblErrorFecha, 0, 7);
        grid.add(new Label("Lugar*:"), 0, 2);
        grid.add(txtLugar, 1, 2);
        GridPane.setColumnSpan(lblErrorLugar, 2);
        grid.add(lblErrorLugar, 0, 8);
        grid.add(new Label("Descripción*:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(lblContador, 1 , 4);
        GridPane.setColumnSpan(lblErrorDesc, 2);
        grid.add(lblErrorDesc, 0, 9);


        dialog.getDialogPane().setContent(grid);
        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean valido = true;
            if (txtNombre.getText() == null || txtNombre.getText().isEmpty()) {
                lblErrorNombre.setText("El nombre es obligatorio");
                txtNombre.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtNombre.getText().charAt(0))) {
                lblErrorNombre.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtNombre.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorNombre.setText("");
                txtNombre.setStyle("-fx-font-size: 12px;");
            }

            if (txtFecha.getText() == null || txtFecha.getText().trim().isEmpty()) {
                lblErrorFecha.setText("La fecha es obligatoria");
                txtFecha.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!txtFecha.getText().matches("(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{4}")) {
                lblErrorFecha.setText("Formato inválido. Usá dd/mm/aaaa (ej: 15/12/2026)");
                txtFecha.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorFecha.setText("");
                txtFecha.setStyle("-fx-font-size: 12px;");
            }

            if (txtLugar.getText() == null || txtLugar.getText().trim().isEmpty()) {
                lblErrorLugar.setText("El lugar es obligatorio");
                txtLugar.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtLugar.getText().charAt(0))) {
                lblErrorLugar.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtLugar.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorLugar.setText("");
                txtLugar.setStyle("-fx-font-size: 12px;");
            }

            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                lblErrorDesc.setText("La descripción es obligatoria");
                txtDesc.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtDesc.getText().charAt(0))) {
                lblErrorDesc.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtDesc.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorDesc.setText("");
                txtDesc.setStyle("-fx-font-size: 12px;");
            }
            if (txtHorario.getText() != null && !txtHorario.getText().trim().isEmpty()) {
                if (!txtHorario.getText().matches("([01][0-9]|2[0-3]):[0-5][0-9]/([01][0-9]|2[0-3]):[0-5][0-9]")) {
                    lblErrorHorario.setText("Formato inválido. Usá HH:MM/HH:MM (ej: 18:00/22:00)");
                    txtHorario.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                    valido = false;
                } else {
                    lblErrorHorario.setText("");
                    txtHorario.setStyle("-fx-font-size: 12px;");
                }
            } else {
                lblErrorHorario.setText("");
                txtHorario.setStyle("-fx-font-size: 12px;");
            }
            if (!valido) {
                event.consume();
            }
        });
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                lblErrorNombre.setText("");
                txtNombre.setStyle("-fx-font-size: 12px;");
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextEventoId();
                return new Evento(id, txtNombre.getText(), txtFecha.getText(), txtHorario.getText(),
                        txtLugar.getText(), txtDesc.getText(), "Activo");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(e -> {
            db.insertarEvento(e);
            filtrar();
        });
    }

    private void mostrarDialogoEditar(Evento evento) {
        Dialog<Evento> dialog = new Dialog<>();
        dialog.setTitle("Editar Evento");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));
        boolean esBloqueado = "Finalizado".equals(evento.getEstado()) || "Cancelado".equals(evento.getEstado());
        String estiloDeshabilitado = "-fx-font-size: 12px; -fx-background-color: #e9ecef; -fx-text-fill: #6c757d;";
        String estiloNormal = "-fx-font-size: 12px;";

        TextField txtNombre = new TextField(evento.getNombre());
        txtNombre.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
        txtNombre.setDisable(esBloqueado);

        Label lblErrorNombre = new Label();
        lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorNombre.setWrapText(true);
        lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);

        TextField txtFecha = new TextField(evento.getFecha());
        txtFecha.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> { String newText = change.getControlNewText(); if (newText.matches("[0-9/\\-]*")) { return change; } return null; }));
        txtFecha.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
        txtFecha.setDisable(esBloqueado);
        Label lblErrorFecha = new Label();
        lblErrorFecha.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorFecha.setWrapText(true);
        lblErrorFecha.setMaxWidth(Double.MAX_VALUE);
        lblErrorFecha.setPrefWidth(320);
        TextField txtHorario = new TextField(evento.getHorario());
        txtHorario.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9:/]*")) {
                return change;
            }
            return null;
        }));
        txtHorario.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9:/]*")) {
                return change;
            }
            return null;
        }));
        txtHorario.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
        txtHorario.setDisable(esBloqueado);
        TextField txtLugar = new TextField(evento.getLugar());
        txtLugar.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
        txtLugar.setDisable(esBloqueado);
        Label lblErrorLugar = new Label();
        lblErrorLugar.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorLugar.setWrapText(true);
        lblErrorLugar.setMaxWidth(Double.MAX_VALUE);
        lblErrorLugar.setPrefWidth(320);
        String descTexto = evento.getDescripcion() != null ? evento.getDescripcion() : "";
        TextArea txtDesc = new TextArea(descTexto);
        txtDesc.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
        txtDesc.setDisable(esBloqueado);
        Label lblErrorHorario = new Label();
        lblErrorHorario.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorHorario.setWrapText(true);
        lblErrorHorario.setMaxWidth(Double.MAX_VALUE);
        lblErrorHorario.setPrefWidth(320);
        Label lblErrorDesc = new Label();
        lblErrorDesc.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorDesc.setWrapText(true);
        lblErrorDesc.setMaxWidth(Double.MAX_VALUE);
        lblErrorDesc.setPrefWidth(320);

        txtDesc.setPrefRowCount(3);
        txtDesc.setWrapText(true);
        txtDesc.setPrefWidth(250);


        Label lblContador = new Label(descTexto.length() + "/255 caracteres");
        lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        txtDesc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 255) {
                txtDesc.setText(oldVal);
                return;
            }
            lblContador.setText(newVal.length() + "/255 caracteres");
            if (newVal.length() >= 230) {
                lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            } else {
                lblContador.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
            }
        });


        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Horario:"), 0, 11);
        grid.add(txtHorario, 1, 11);
        GridPane.setColumnSpan(lblErrorHorario, 2);
        grid.add(lblErrorHorario, 0, 12);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 7);
        grid.add(new Label("Fecha*:"), 0, 1);
        grid.add(txtFecha, 1, 1);
        GridPane.setColumnSpan(lblErrorFecha, 2);
        grid.add(lblErrorFecha, 0, 8);
        grid.add(new Label("Lugar*:"), 0, 2);
        grid.add(txtLugar, 1, 2);
        GridPane.setColumnSpan(lblErrorLugar, 2);
        grid.add(lblErrorLugar, 0, 9);
        grid.add(new Label("Descripción*:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(lblContador, 1, 4);
        GridPane.setColumnSpan(lblErrorDesc, 2);
        grid.add(lblErrorDesc, 0, 10);

        if (esBloqueado) {
            Label lblAviso = new Label("⚠ Evento " + evento.getEstado().toLowerCase() + ": no se puede editar.");
            lblAviso.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold;");
            GridPane.setColumnSpan(lblAviso, 2);
            grid.add(lblAviso, 0, 6);
        }

        dialog.getDialogPane().setContent(grid);
        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.setDisable(esBloqueado);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean valido = true;
            if (txtNombre.getText() == null || txtNombre.getText().isEmpty()) {
                lblErrorNombre.setText("El nombre es obligatorio");
                txtNombre.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtNombre.getText().charAt(0))) {
                lblErrorNombre.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtNombre.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorNombre.setText("");
                txtNombre.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
            }

            if (txtFecha.getText() == null || txtFecha.getText().trim().isEmpty()) {
                lblErrorFecha.setText("La fecha es obligatoria");
                txtFecha.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!txtFecha.getText().matches("(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{4}")) {
                lblErrorFecha.setText("Formato inválido. Usá dd/mm/aaaa (ej: 15/12/2026)");
                txtFecha.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorFecha.setText("");
                txtFecha.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
            }

            if (txtLugar.getText() == null || txtLugar.getText().trim().isEmpty()) {
                lblErrorLugar.setText("El lugar es obligatorio");
                txtLugar.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtLugar.getText().charAt(0))) {
                lblErrorLugar.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtLugar.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorLugar.setText("");
                txtLugar.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
            }

            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                lblErrorDesc.setText("La descripción es obligatoria");
                txtDesc.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtDesc.getText().charAt(0))) {
                lblErrorDesc.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtDesc.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorDesc.setText("");
                txtDesc.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
            }
            if (txtHorario.getText() != null && !txtHorario.getText().trim().isEmpty()) {
                if (!txtHorario.getText().matches("([01][0-9]|2[0-3]):[0-5][0-9]/([01][0-9]|2[0-3]):[0-5][0-9]")) {
                    lblErrorHorario.setText("Formato inválido. Usá HH:MM/HH:MM (ej: 18:00/22:00)");
                    txtHorario.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                    valido = false;
                } else {
                    lblErrorHorario.setText("");
                    txtHorario.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
                }
            } else {
                lblErrorHorario.setText("");
                txtHorario.setStyle(esBloqueado ? estiloDeshabilitado : estiloNormal);
            }

            if (!valido) {
                event.consume();
            }
        });
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                lblErrorNombre.setText("");
                txtNombre.setStyle("-fx-font-size: 12px;");
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Evento(evento.getId(), txtNombre.getText(), txtFecha.getText(), txtHorario.getText(),
                        txtLugar.getText(), txtDesc.getText(), evento.getEstado());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(e -> {
            db.actualizarEvento(e);
            filtrar();
        });
    }

    private void mostrarDialogoEliminar(Evento evento) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Evento");
        confirmacion.setHeaderText("¿Está seguro de eliminar este evento?");
        confirmacion.setContentText("Evento: " + evento.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarEvento(evento.getId())) {
                filtrar();
            }
        }
    }
    private void mostrarDialogoPapelera() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Papelera de Eventos");
        dialog.setHeaderText("Eventos eliminados");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Evento> tablaPapelera = new TableView<>();
        tablaPapelera.setPrefSize(500, 250);

        TableColumn<Evento, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Evento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colFecha.setPrefWidth(100);

        TableColumn<Evento, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setPrefWidth(100);
        colAccion.setCellFactory(col -> new TableCell<Evento, Void>() {
            private final Button btnRestaurar = new Button("Restaurar");
            {
                btnRestaurar.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnRestaurar.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    db.restaurarEvento(ev.getId());
                    tablaPapelera.setItems(db.getEventosEliminados());
                    filtrar();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRestaurar);
            }
        });

        tablaPapelera.getColumns().addAll(colNombre, colFecha, colAccion);
        tablaPapelera.setItems(db.getEventosEliminados());

        dialog.getDialogPane().setContent(tablaPapelera);
        dialog.showAndWait();
    }
}
