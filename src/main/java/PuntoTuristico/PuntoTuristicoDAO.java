package PuntoTuristico;

import Conexion.Database;
import PuntoTuristico.PuntoTuristico;
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

public class PuntoTuristicoDAO {
    private Database db;
    private TableView<PuntoTuristico> table;
    private TextField txtSearch;
    private ComboBox<String> cbEstado;

    public PuntoTuristicoDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🏛 Gestión de Atractivos");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Catalogación de puntos de interés turístico");
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
        txtSearch.setPromptText("Buscar atractivo...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Todos", "Disponible", "En mantenimiento", "Cerrado");
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

        TableColumn<PuntoTuristico, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);

        TableColumn<PuntoTuristico, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<PuntoTuristico, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(c -> c.getValue().ubicacionProperty());
        colUbicacion.setPrefWidth(130);

        TableColumn<PuntoTuristico, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(180);

        TableColumn<PuntoTuristico, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colTipo.setPrefWidth(80);

        TableColumn<PuntoTuristico, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(100);
        colEstado.setCellFactory(col -> new TableCell<PuntoTuristico, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "Disponible" -> "#22c55e";
                        case "En mantenimiento" -> "#f59e0b";
                        default -> "#ef4444";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<PuntoTuristico, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<PuntoTuristico, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    PuntoTuristico p = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(p);
                });

                btnToggle.setOnAction(e -> {
                    PuntoTuristico p = getTableView().getItems().get(getIndex());
                    String[] estados = {"Disponible", "En mantenimiento", "Cerrado"};
                    int idx = java.util.Arrays.asList(estados).indexOf(p.getEstado());
                    String nuevo = estados[(idx + 1) % estados.length];
                    db.cambiarEstadoAtractivo(p.getId(), nuevo);
                    table.refresh();
                });

                btnEliminar.setOnAction(e -> {
                    PuntoTuristico p = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colUbicacion, colDesc, colTipo, colEstado, colAcciones);
        table.setItems(db.getAtractivos());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String estado = cbEstado.getValue();

        ObservableList<PuntoTuristico> filtrados = FXCollections.observableArrayList();
        for (PuntoTuristico p : db.getAtractivos()) {
            boolean matchSearch = search.isEmpty() || p.getNombre().toLowerCase().contains(search.toLowerCase());
            boolean matchEstado = estado.equals("Todos") || p.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(p);
            }
        }
        table.setItems(filtrados);
    }

    private void mostrarDialogoAlta() {
        Dialog<PuntoTuristico> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Atractivo");
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
        TextField txtUbicacion = new TextField();
        txtUbicacion.setPromptText("Ubicación");
        txtUbicacion.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Descripción");
        txtDesc.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Natural", "Cultural", "Histórico"));
        cbTipo.setValue("Natural");
        cbTipo.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Disponible", "En mantenimiento", "Cerrado"));
        cbEstado.setValue("Disponible");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Ubicación:"), 0, 1);
        grid.add(txtUbicacion, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(txtDesc, 1, 2);
        grid.add(new Label("Tipo:"), 0, 3);
        grid.add(cbTipo, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextAtractivoId();
                return new PuntoTuristico(id, txtNombre.getText(), txtUbicacion.getText(),
                        txtDesc.getText(), cbTipo.getValue(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            db.insertarAtractivo(p);
            filtrar();
        });
    }

    private void mostrarDialogoEditar(PuntoTuristico punto) {
        Dialog<PuntoTuristico> dialog = new Dialog<>();
        dialog.setTitle("Editar Atractivo");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField(punto.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        TextField txtUbicacion = new TextField(punto.getUbicacion());
        txtUbicacion.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField(punto.getDescripcion());
        txtDesc.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Natural", "Cultural", "Histórico"));
        cbTipo.setValue(punto.getTipo());
        cbTipo.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Disponible", "En mantenimiento", "Cerrado"));
        cbEstado.setValue(punto.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Ubicación:"), 0, 1);
        grid.add(txtUbicacion, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(txtDesc, 1, 2);
        grid.add(new Label("Tipo:"), 0, 3);
        grid.add(cbTipo, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new PuntoTuristico(punto.getId(), txtNombre.getText(), txtUbicacion.getText(),
                        txtDesc.getText(), cbTipo.getValue(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            db.actualizarAtractivo(p);
            filtrar();
        });
    }

    private void mostrarDialogoEliminar(PuntoTuristico punto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Atractivo");
        confirmacion.setHeaderText("¿Está seguro de eliminar este atractivo?");
        confirmacion.setContentText("Atractivo: " + punto.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarAtractivo(punto.getId())) {
                filtrar();
            }
        }
    }
}