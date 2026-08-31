package ActividadRecreativa;

import ActividadRecreativa.ActividadRecreativa;
import Conexion.Database;
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

public class ActividadRecreativaDAO {
    private Database db;
    private TableView<ActividadRecreativa> table;
    private TextField txtSearch;
    private ComboBox<String> cbEstado;

    public ActividadRecreativaDAO() {
        this.db = Database.getInstance();
    }

    public VBox getVista() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🏃 Gestión de Actividades");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#0f172a"));
        Label lblSubtitle = new Label("Listado de actividades turísticas disponibles");
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
        txtSearch.setPromptText("Buscar actividad...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Todos", "Activa", "Inactiva");
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");
        cbEstado.setOnAction(e -> filtrar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nueva");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnNuevo.setOnAction(e -> mostrarDialogoAlta());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<ActividadRecreativa, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);

        TableColumn<ActividadRecreativa, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<ActividadRecreativa, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(200);

        TableColumn<ActividadRecreativa, String> colDuracion = new TableColumn<>("Duración");
        colDuracion.setCellValueFactory(c -> c.getValue().duracionProperty());
        colDuracion.setPrefWidth(80);

        TableColumn<ActividadRecreativa, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colPrecio.setPrefWidth(80);

        TableColumn<ActividadRecreativa, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(70);
        colEstado.setCellFactory(col -> new TableCell<ActividadRecreativa, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + ("Activa".equals(item) ? "#22c55e" : "#ef4444") +
                            "; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<ActividadRecreativa, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<ActividadRecreativa, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnToggle = new Button("◉");
            private final Button btnEliminar = new Button("🗑");
            private final HBox pane = new HBox(4, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnToggle.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    ActividadRecreativa a = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(a);
                });

                btnToggle.setOnAction(e -> {
                    ActividadRecreativa a = getTableView().getItems().get(getIndex());
                    String nuevoEstado = a.getEstado().equals("Activa") ? "Inactiva" : "Activa";
                    db.cambiarEstadoActividad(a.getId(), nuevoEstado);
                    table.refresh();
                });

                btnEliminar.setOnAction(e -> {
                    ActividadRecreativa a = getTableView().getItems().get(getIndex());
                    mostrarDialogoEliminar(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colId, colNombre, colDesc, colDuracion, colPrecio, colEstado, colAcciones);
        table.setItems(db.getActividades());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {
        String search = txtSearch.getText();
        String estado = cbEstado.getValue();

        ObservableList<ActividadRecreativa> filtrados = FXCollections.observableArrayList();
        for (ActividadRecreativa a : db.getActividades()) {
            boolean matchSearch = search.isEmpty() || a.getNombre().toLowerCase().contains(search.toLowerCase());
            boolean matchEstado = estado.equals("Todos") || a.getEstado().equals(estado);
            if (matchSearch && matchEstado) {
                filtrados.add(a);
            }
        }
        table.setItems(filtrados);
    }

    private void mostrarDialogoAlta() {
        Dialog<ActividadRecreativa> dialog = new Dialog<>();
        dialog.setTitle("Nueva Actividad");
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
        TextField txtDuracion = new TextField();
        txtDuracion.setPromptText("Duración (ej: 3 horas)");
        txtDuracion.setStyle("-fx-font-size: 12px;");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio");
        txtPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activa", "Inactiva"));
        cbEstado.setValue("Activa");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Duración:"), 0, 2);
        grid.add(txtDuracion, 1, 2);
        grid.add(new Label("Precio:"), 0, 3);
        grid.add(txtPrecio, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextActividadId();
                return new ActividadRecreativa(id, txtNombre.getText(), txtDesc.getText(),
                        txtDuracion.getText(), txtPrecio.getText(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            db.insertarActividad(a);
            filtrar();
        });
    }

    private void mostrarDialogoEditar(ActividadRecreativa actividad) {
        Dialog<ActividadRecreativa> dialog = new Dialog<>();
        dialog.setTitle("Editar Actividad");
        dialog.setHeaderText("Modifique los datos");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField txtNombre = new TextField(actividad.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        TextField txtDesc = new TextField(actividad.getDescripcion());
        txtDesc.setStyle("-fx-font-size: 12px;");
        TextField txtDuracion = new TextField(actividad.getDuracion());
        txtDuracion.setStyle("-fx-font-size: 12px;");
        TextField txtPrecio = new TextField(actividad.getPrecio());
        txtPrecio.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activa", "Inactiva"));
        cbEstado.setValue(actividad.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Duración:"), 0, 2);
        grid.add(txtDuracion, 1, 2);
        grid.add(new Label("Precio:"), 0, 3);
        grid.add(txtPrecio, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cbEstado, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new ActividadRecreativa(actividad.getId(), txtNombre.getText(), txtDesc.getText(),
                        txtDuracion.getText(), txtPrecio.getText(), cbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            db.actualizarActividad(a);
            filtrar();
        });
    }

    private void mostrarDialogoEliminar(ActividadRecreativa actividad) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Actividad");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta actividad?");
        confirmacion.setContentText("Actividad: " + actividad.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (db.eliminarActividad(actividad.getId())) {
                filtrar();
            }
        }
    }
}