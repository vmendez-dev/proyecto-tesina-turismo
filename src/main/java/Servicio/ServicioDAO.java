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
    private ComboBox<String> cbEstado;

    public ServicioDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🔧 Gestión de Servicios");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Listado de servicios turísticos complementarios");
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
        colId.setPrefWidth(70);

        TableColumn<Servicio, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Servicio, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(180);

        TableColumn<Servicio, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colPrecio.setPrefWidth(80);

        TableColumn<Servicio, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(70);
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

        table.getColumns().addAll(colId, colNombre, colDesc, colPrecio, colEstado, colAcciones);
        table.setItems(db.getServicios());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String estado = cbEstado.getValue();

        ObservableList<Servicio> filtrados = FXCollections.observableArrayList();
        for (Servicio s : db.getServicios()) {
            boolean matchSearch = search.isEmpty() ||
                    s.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                    s.getDescripcion().toLowerCase().contains(search.toLowerCase());
            boolean matchEstado = estado.equals("Todos") || s.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(s);
            }
        }
        table.setItems(filtrados);
    }

    private void mostrarDialogoAlta() {
        Dialog<Servicio> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Servicio");
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
        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Descripción");
        txtDesc.setStyle("-fx-font-size: 12px;");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio");
        txtPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue("Activo");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(new Label("Estado:"), 0, 3);
        grid.add(cbEstado, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextServicioId();
                return new Servicio(id, txtNombre.getText(), txtDesc.getText(),
                        txtPrecio.getText(), cbEstado.getValue());
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

        TextField txtNombre = new TextField(servicio.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField(servicio.getDescripcion());
        txtDesc.setStyle("-fx-font-size: 12px;");
        TextField txtPrecio = new TextField(servicio.getPrecio());
        txtPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue(servicio.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(new Label("Estado:"), 0, 3);
        grid.add(cbEstado, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Servicio(servicio.getId(), txtNombre.getText(), txtDesc.getText(),
                        txtPrecio.getText(), cbEstado.getValue());
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