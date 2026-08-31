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

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Evento, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);

        TableColumn<Evento, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Evento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colFecha.setPrefWidth(100);

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

    private void mostrarDialogoAlta() {
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
        TextField txtFecha = new TextField();
        txtFecha.setPromptText("Fecha (dd/mm/aaaa)");
        txtFecha.setStyle("-fx-font-size: 12px;");
        TextField txtLugar = new TextField();
        txtLugar.setPromptText("Lugar");
        txtLugar.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Descripción");
        txtDesc.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Finalizado", "Cancelado"));
        cbEstado.setValue("Activo");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(txtFecha, 1, 1);
        grid.add(new Label("Lugar:"), 0, 2);
        grid.add(txtLugar, 1, 2);
        grid.add(new Label("Descripción:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextEventoId();
                return new Evento(id, txtNombre.getText(), txtFecha.getText(),
                        txtLugar.getText(), txtDesc.getText(), cbEstado.getValue());
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

        TextField txtNombre = new TextField(evento.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        TextField txtFecha = new TextField(evento.getFecha());
        txtFecha.setStyle("-fx-font-size: 12px;");
        TextField txtLugar = new TextField(evento.getLugar());
        txtLugar.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField(evento.getDescripcion());
        txtDesc.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Finalizado", "Cancelado"));
        cbEstado.setValue(evento.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(txtFecha, 1, 1);
        grid.add(new Label("Lugar:"), 0, 2);
        grid.add(txtLugar, 1, 2);
        grid.add(new Label("Descripción:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Evento(evento.getId(), txtNombre.getText(), txtFecha.getText(),
                        txtLugar.getText(), txtDesc.getText(), cbEstado.getValue());
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
}