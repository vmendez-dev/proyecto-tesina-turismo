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

        Button btnPapelera = new Button("🗑 Papelera");
        btnPapelera.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnPapelera.setOnAction(e -> mostrarDialogoPapelera());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnPapelera, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<ActividadRecreativa, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);
        colId.setVisible(false);

        TableColumn<ActividadRecreativa, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<ActividadRecreativa, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(200);

        TableColumn<ActividadRecreativa, String> colDuracion = new TableColumn<>("Duración");
        colDuracion.setCellValueFactory(c -> c.getValue().duracionProperty());
        colDuracion.setPrefWidth(80);


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
        TableColumn<ActividadRecreativa, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        colHorario.setPrefWidth(140);
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
                    filtrar();
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

        table.getColumns().addAll(colId, colNombre, colDesc, colDuracion, colEstado, colHorario, colAcciones);
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

    private HBox crearLabelObligatorio(String texto) {
        Label lbl = new Label(texto + ":");
        Label asterisco = new Label(" *");
        asterisco.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        return new HBox(lbl, asterisco);
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
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descripción");
        txtDesc.setStyle("-fx-font-size: 12px;");
        txtDesc.setPrefRowCount(3);
        txtDesc.setWrapText(true);
        txtDesc.setPrefWidth(250);

        Label lblContador = new Label("0/255 caracteres");
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
        TextField txtDuracion = new TextField();
        txtDuracion.setPromptText("Duración (ej: 3 horas)");
        txtDuracion.setStyle("-fx-font-size: 12px;");
        TextField txtHorario = new TextField();
        txtHorario.setPromptText("Ej: Lun a Vie 9 a 18hs");
        txtHorario.setStyle("-fx-font-size: 12px;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setPrefWidth(320);
        lblError.setVisible(false);

        java.util.function.UnaryOperator<TextFormatter.Change> bloqueoEspacio = change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                return null;
            }
            return change;
        };
        txtNombre.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDuracion.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDesc.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtHorario.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));

        grid.add(crearLabelObligatorio("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabelObligatorio("Descripción"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(lblContador, 1, 2);
        grid.add(crearLabelObligatorio("Duración"), 0, 3);
        grid.add(txtDuracion, 1, 3);
        grid.add(new Label("Horario:"), 0, 4);
        grid.add(txtHorario, 1, 4);
        GridPane.setColumnSpan(lblError, 2);
        grid.add(lblError, 0, 5);
        dialog.getDialogPane().setContent(grid);

        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            java.util.List<String> faltantes = new java.util.ArrayList<>();

            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                faltantes.add("Nombre");
            }
            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                faltantes.add("Descripción");
            }
            if (txtDuracion.getText() == null || txtDuracion.getText().trim().isEmpty()) {
                faltantes.add("Duración");
            }

            if (!faltantes.isEmpty()) {
                lblError.setText("⚠ Los siguientes campos son obligatorios: " + String.join(", ", faltantes));
                lblError.setVisible(true);
                ev.consume();
            } else {
                lblError.setVisible(false);
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new ActividadRecreativa("0", txtNombre.getText().trim(), txtDesc.getText().trim(),
                        txtDuracion.getText().trim(), "", "Activa", txtHorario.getText());
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
        boolean esBloqueado = "Inactiva".equals(actividad.getEstado());
        String estiloDeshabilitado = "-fx-font-size: 12px; -fx-background-color: #e9ecef; -fx-text-fill: #6c757d;";
        TextField txtNombre = new TextField(actividad.getNombre());
        txtNombre.setStyle("-fx-font-size: 12px;");
        String descTexto = actividad.getDescripcion() != null ? actividad.getDescripcion() : "";
        TextArea txtDesc = new TextArea(descTexto.trim());
        txtDesc.setStyle("-fx-font-size: 12px;");
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
        TextField txtDuracion = new TextField(actividad.getDuracion());
        txtDuracion.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activa", "Inactiva"));
        cbEstado.setValue(actividad.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");
        TextField txtHorario = new TextField(actividad.getHorario());
        txtHorario.setStyle("-fx-font-size: 12px;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setPrefWidth(320);
        lblError.setVisible(false);

        java.util.function.UnaryOperator<TextFormatter.Change> bloqueoEspacio = change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                return null;
            }
            return change;
        };
        txtNombre.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDuracion.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDesc.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtHorario.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));

        grid.add(crearLabelObligatorio("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabelObligatorio("Descripción"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(lblContador, 1, 2);
        grid.add(crearLabelObligatorio("Duración"), 0, 3);
        grid.add(txtDuracion, 1, 3);
        grid.add(new Label("Horario:"), 0, 4);
        grid.add(txtHorario, 1, 4);
        GridPane.setColumnSpan(lblError, 2);
        grid.add(lblError, 0, 5);

        dialog.getDialogPane().setContent(grid);

        if (esBloqueado) {
            txtNombre.setStyle(estiloDeshabilitado);
            txtNombre.setDisable(true);
            txtDesc.setStyle(estiloDeshabilitado);
            txtDesc.setDisable(true);
            txtDuracion.setStyle(estiloDeshabilitado);
            txtDuracion.setDisable(true);
            txtHorario.setStyle(estiloDeshabilitado);
            txtHorario.setDisable(true);

            Label lblAviso = new Label("⚠ Actividad inactiva: no se puede editar. Reactívela primero.");
            lblAviso.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblAviso.setWrapText(true);
            lblAviso.setMaxWidth(Double.MAX_VALUE);
            GridPane.setColumnSpan(lblAviso, 2);
            grid.add(lblAviso, 0, 6);
        }

        dialog.getDialogPane().setContent(grid);

        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.setDisable(esBloqueado);

        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            java.util.List<String> faltantes = new java.util.ArrayList<>();

            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                faltantes.add("Nombre");
            }
            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                faltantes.add("Descripción");
            }
            if (txtDuracion.getText() == null || txtDuracion.getText().trim().isEmpty()) {
                faltantes.add("Duración");
            }

            if (!faltantes.isEmpty()) {
                lblError.setText("⚠ Los siguientes campos son obligatorios: " + String.join(", ", faltantes));
                lblError.setVisible(true);
                ev.consume();
            } else {
                lblError.setVisible(false);
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new ActividadRecreativa(actividad.getId(), txtNombre.getText().trim(), txtDesc.getText().trim(),
                        txtDuracion.getText().trim(), "", cbEstado.getValue(), txtHorario.getText());
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
    private void mostrarDialogoPapelera() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Papelera de Actividades");
        dialog.setHeaderText("Actividades eliminadas");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<ActividadRecreativa> tablaPapelera = new TableView<>();
        tablaPapelera.setPrefSize(500, 250);

        TableColumn<ActividadRecreativa, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(180);

        TableColumn<ActividadRecreativa, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setPrefWidth(120);

        TableColumn<ActividadRecreativa, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setPrefWidth(100);
        colAccion.setCellFactory(col -> new TableCell<ActividadRecreativa, Void>() {
            private final Button btnRestaurar = new Button("Restaurar");
            {
                btnRestaurar.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnRestaurar.setOnAction(e -> {
                    ActividadRecreativa a = getTableView().getItems().get(getIndex());
                    db.restaurarActividad(a.getId());
                    tablaPapelera.setItems(db.getActividadesEliminadas());
                    filtrar();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRestaurar);
            }
        });

        tablaPapelera.getColumns().addAll(colNombre, colEstado, colAccion);
        tablaPapelera.setItems(db.getActividadesEliminadas());

        dialog.getDialogPane().setContent(tablaPapelera);
        dialog.showAndWait();
    }
}
