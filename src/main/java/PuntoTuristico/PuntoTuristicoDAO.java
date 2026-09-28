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

        Button btnPapelera = new Button("🗑 Papelera");
        btnPapelera.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnPapelera.setOnAction(e -> mostrarDialogoPapelera());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnPapelera, btnNuevo);

        table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<PuntoTuristico, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);
        colId.setVisible(false);

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
        TableColumn<PuntoTuristico, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        colHorario.setPrefWidth(140);
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
                    filtrar();
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

        table.getColumns().addAll(colId, colNombre, colUbicacion, colDesc, colTipo, colEstado, colHorario, colAcciones);
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

    private HBox crearLabelObligatorio(String texto) {
        Label lbl = new Label(texto + ":");
        Label asterisco = new Label(" *");
        asterisco.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        return new HBox(lbl, asterisco);
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
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Natural", "Cultural", "Histórico"));
        cbTipo.setValue("Natural");
        cbTipo.setStyle("-fx-font-size: 12px;");
        TextField txtHorario = new TextField();
        txtHorario.setPromptText("Ej: Lun a Vie 9 a 18hs / Acceso libre");
        txtHorario.setStyle("-fx-font-size: 12px;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(320);
        lblError.setVisible(false);

        java.util.function.UnaryOperator<TextFormatter.Change> bloqueoEspacio = change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                return null;
            }
            return change;
        };
        txtNombre.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtUbicacion.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDesc.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtHorario.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));

        grid.add(crearLabelObligatorio("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabelObligatorio("Ubicación"), 0, 1);
        grid.add(txtUbicacion, 1, 1);
        grid.add(crearLabelObligatorio("Descripción"), 0, 2);
        grid.add(txtDesc, 1, 2);
        grid.add(lblContador, 1, 3);
        grid.add(crearLabelObligatorio("Tipo"), 0, 4);
        grid.add(cbTipo, 1, 4);
        grid.add(new Label("Horario:"), 0, 5);
        grid.add(txtHorario, 1, 5);
        GridPane.setColumnSpan(lblError, 2);
        grid.add(lblError, 0, 6);

        dialog.getDialogPane().setContent(grid);

        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            java.util.List<String> faltantes = new java.util.ArrayList<>();

            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                faltantes.add("Nombre");
            }
            if (txtUbicacion.getText() == null || txtUbicacion.getText().trim().isEmpty()) {
                faltantes.add("Ubicación");
            }
            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                faltantes.add("Descripción");
            }
            if (cbTipo.getValue() == null) {
                faltantes.add("Tipo");
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
                return new PuntoTuristico("0", txtNombre.getText().trim(), txtUbicacion.getText().trim(),
                        txtDesc.getText().trim(), cbTipo.getValue(), "Disponible", txtHorario.getText().trim());
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
        String descTexto = punto.getDescripcion() != null ? punto.getDescripcion() : "";
        TextArea txtDesc = new TextArea(descTexto);
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
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Natural", "Cultural", "Histórico"));
        cbTipo.setValue(punto.getTipo());
        cbTipo.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Disponible", "En mantenimiento", "Cerrado"));
        cbEstado.setValue(punto.getEstado());
        cbEstado.setStyle("-fx-font-size: 12px;");
        TextField txtHorario = new TextField(punto.getHorario());
        txtHorario.setStyle("-fx-font-size: 12px;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(320);
        lblError.setVisible(false);

        java.util.function.UnaryOperator<TextFormatter.Change> bloqueoEspacio = change -> {
            String nuevo = change.getControlNewText();
            if (!nuevo.isEmpty() && Character.isWhitespace(nuevo.charAt(0))) {
                return null;
            }
            return change;
        };
        txtNombre.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtUbicacion.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtDesc.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));
        txtHorario.setTextFormatter(new TextFormatter<String>(bloqueoEspacio));

        grid.add(crearLabelObligatorio("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabelObligatorio("Ubicación"), 0, 1);
        grid.add(txtUbicacion, 1, 1);
        grid.add(crearLabelObligatorio("Descripción"), 0, 2);
        grid.add(txtDesc, 1, 2);
        grid.add(lblContador, 1, 3);
        grid.add(crearLabelObligatorio("Tipo"), 0, 4);
        grid.add(cbTipo, 1, 4);
        grid.add(new Label("Horario:"), 0, 5);
        grid.add(txtHorario, 1, 5);
        GridPane.setColumnSpan(lblError, 2);
        grid.add(lblError, 0, 6);
        dialog.getDialogPane().setContent(grid);

        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            java.util.List<String> faltantes = new java.util.ArrayList<>();

            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                faltantes.add("Nombre");
            }
            if (txtUbicacion.getText() == null || txtUbicacion.getText().trim().isEmpty()) {
                faltantes.add("Ubicación");
            }
            if (txtDesc.getText() == null || txtDesc.getText().trim().isEmpty()) {
                faltantes.add("Descripción");
            }
            if (cbTipo.getValue() == null) {
                faltantes.add("Tipo");
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
                return new PuntoTuristico(punto.getId(), txtNombre.getText().trim(), txtUbicacion.getText().trim(),
                        txtDesc.getText().trim(), cbTipo.getValue(), cbEstado.getValue(), txtHorario.getText().trim());
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
    private void mostrarDialogoPapelera() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Papelera de Atractivos");
        dialog.setHeaderText("Atractivos eliminados");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<PuntoTuristico> tablaPapelera = new TableView<>();
        tablaPapelera.setPrefSize(500, 250);

        TableColumn<PuntoTuristico, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<PuntoTuristico, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colTipo.setPrefWidth(120);

        TableColumn<PuntoTuristico, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        colHorario.setPrefWidth(140);

        TableColumn<PuntoTuristico, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setPrefWidth(100);
        colAccion.setCellFactory(col -> new TableCell<PuntoTuristico, Void>() {
            private final Button btnRestaurar = new Button("Restaurar");
            {
                btnRestaurar.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                btnRestaurar.setOnAction(e -> {
                    PuntoTuristico p = getTableView().getItems().get(getIndex());
                    db.restaurarAtractivo(p.getId());
                    tablaPapelera.setItems(db.getAtractivosEliminados());
                    filtrar();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRestaurar);
            }
        });

        tablaPapelera.getColumns().addAll(colNombre, colTipo, colAccion);
        tablaPapelera.setItems(db.getAtractivosEliminados());

        dialog.getDialogPane().setContent(tablaPapelera);
        dialog.showAndWait();
    }
}