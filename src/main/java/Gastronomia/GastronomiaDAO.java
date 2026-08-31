package Gastronomia;

import Conexion.Database;
import Gastronomia.Gastronomia;
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

public class GastronomiaDAO {
    private Database db;
    private TableView<Gastronomia> table;
    private TextField txtSearch;
    private ComboBox<String> cbEstado;

    public GastronomiaDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🍽 Gestión de Gastronomía");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Listado de establecimientos gastronómicos");
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
        txtSearch.setPromptText("Buscar establecimiento...");
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

        TableColumn<Gastronomia, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);

        TableColumn<Gastronomia, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Gastronomia, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colTipo.setPrefWidth(100);

        TableColumn<Gastronomia, String> colEspecialidad = new TableColumn<>("Especialidad");
        colEspecialidad.setCellValueFactory(c -> c.getValue().especialidadProperty());
        colEspecialidad.setPrefWidth(130);

        TableColumn<Gastronomia, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colPrecio.setPrefWidth(60);

        TableColumn<Gastronomia, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(70);
        colEstado.setCellFactory(col -> new TableCell<Gastronomia, String>() {
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

        TableColumn<Gastronomia, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<Gastronomia, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    Gastronomia g = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(g);
                });

                btnToggle.setOnAction(e -> {
                    Gastronomia g = getTableView().getItems().get(getIndex());
                    String nuevoEstado = g.getEstado().equals("Activo") ? "Inactivo" : "Activo";
                    db.cambiarEstadoGastronomia(g.getId(), nuevoEstado);
                    table.refresh();
                });

                btnEliminar.setOnAction(e -> {
                    Gastronomia g = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(g);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colTipo, colEspecialidad, colPrecio, colEstado, colAcciones);
        table.setItems(db.getGastronomias());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String estado = cbEstado.getValue();

        ObservableList<Gastronomia> filtrados = FXCollections.observableArrayList();
        for (Gastronomia g : db.getGastronomias()) {
            boolean matchSearch = search.isEmpty() ||
                    g.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                    g.getTipo().toLowerCase().contains(search.toLowerCase());
            boolean matchEstado = estado.equals("Todos") || g.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(g);
            }
        }
        table.setItems(filtrados);
    }

    private void mostrarDialogoAlta() {
        Dialog<Gastronomia> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Establecimiento");
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
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Parrilla", "Italiana", "Cafetería", "Restaurante", "Fast Food"));
        cbTipo.setValue("Restaurante");
        cbTipo.setStyle("-fx-font-size: 12px;");
        TextField txtEspecialidad = new TextField();
        txtEspecialidad.setPromptText("Especialidad");
        txtEspecialidad.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbPrecio = new ComboBox<>(FXCollections.observableArrayList("$", "$$", "$$$", "$$$$"));
        cbPrecio.setValue("$$");
        cbPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue("Activo");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Especialidad:"), 0, 2);
        grid.add(txtEspecialidad, 1, 2);
        grid.add(new Label("Precio:"), 0, 3);
        grid.add(cbPrecio, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextGastronomiaId();
                return new Gastronomia(id, txtNombre.getText(), cbTipo.getValue(),
                        txtEspecialidad.getText(), cbPrecio.getValue(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            db.insertarGastronomia(g);
            filtrar();
        });
    }

    private void mostrarDialogoEditar(Gastronomia gastronomia) {
        Dialog<Gastronomia> dialog = new Dialog<>();
        dialog.setTitle("Editar Establecimiento");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField(gastronomia.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Parrilla", "Italiana", "Cafetería", "Restaurante", "Fast Food"));
        cbTipo.setValue(gastronomia.getTipo());
        cbTipo.setStyle("-fx-font-size: 12px;");
        TextField txtEspecialidad = new TextField(gastronomia.getEspecialidad());
        txtEspecialidad.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbPrecio = new ComboBox<>(FXCollections.observableArrayList("$", "$$", "$$$", "$$$$"));
        cbPrecio.setValue(gastronomia.getPrecio());
        cbPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue(gastronomia.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Especialidad:"), 0, 2);
        grid.add(txtEspecialidad, 1, 2);
        grid.add(new Label("Precio:"), 0, 3);
        grid.add(cbPrecio, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Gastronomia(gastronomia.getId(), txtNombre.getText(), cbTipo.getValue(),
                        txtEspecialidad.getText(), cbPrecio.getValue(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            db.actualizarGastronomia(g);
            filtrar();
        });
    }

    private void mostrarDialogoEliminar(Gastronomia gastronomia) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Establecimiento");
        confirmacion.setHeaderText("¿Está seguro de eliminar este establecimiento?");
        confirmacion.setContentText("Establecimiento: " + gastronomia.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarGastronomia(gastronomia.getId())) {
                filtrar();
            }
        }
    }
}