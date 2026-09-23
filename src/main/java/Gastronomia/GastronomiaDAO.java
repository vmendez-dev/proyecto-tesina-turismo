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
import TipoEstablecimiento.TipoEstablecimiento;

import java.util.Optional;

public class GastronomiaDAO {         //variables que la clase necesita recordar//
    private Database db;
    private TableView<Gastronomia> table;
    private TextField txtSearch;
    private ComboBox<String> cbEstado;

    public GastronomiaDAO() { //constructor//
        this.db = Database.getInstance();
    }

    public VBox getVista() {  //método que arma toda la tabla de gastronomia y devuelve todo en un VBox//
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Label lblTitle = new Label("🍽  Gestión de Gastronomía");
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

    private VBox crearTabla() {  //crea las filas de arriba horizontal//
        VBox container = new VBox(12);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();               //aqui se crea el cuadro de texto, el buscador//
        txtSearch.setPromptText("Buscar establecimiento...");
        txtSearch.setPrefWidth(150);
        txtSearch.setStyle("-fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, old, val) -> filtrar());
        //con LISTENER el usuario tipea una letra se ejecuta filtrar//

        cbEstado = new ComboBox<>();  //es el combo de filtro por estado//
        cbEstado.getItems().addAll("Todos", "Activo", "Inactivo");
        cbEstado.getSelectionModel().selectFirst();
        cbEstado.setPrefWidth(100);
        cbEstado.setStyle("-fx-font-size: 12px;");
        cbEstado.setOnAction(e -> filtrar());// cambio estados se filtra y se actualiza solo//

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo");
        btnNuevo.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnNuevo.setOnAction(e -> mostrarDialogoAlta());

        Button btnPapelera = new Button("🗑 Papelera");
        btnPapelera.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 12px;");
        btnPapelera.setOnAction(e -> mostrarDialogoPapelera());

        filterBar.getChildren().addAll(txtSearch, new Label("Estado"), cbEstado, spacer, btnPapelera, btnNuevo);

        table = new TableView<>();  //se crea la tabla //
        table.setPrefHeight(280);

        TableColumn<Gastronomia, String> colId = new TableColumn<>("ID");  //las columnas de datos//
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(70);
        colId.setVisible(false);

        TableColumn<Gastronomia, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colNombre.setPrefWidth(150);

        TableColumn<Gastronomia, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colTipo.setPrefWidth(100);

        TableColumn<Gastronomia, String> colEspecialidad = new TableColumn<>("Especialidad");
        colEspecialidad.setCellValueFactory(c -> c.getValue().especialidadProperty());
        colEspecialidad.setPrefWidth(130);



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
        TableColumn<Gastronomia, String> colFecha = new TableColumn<>("Fecha Registro");
        colFecha.setCellValueFactory(c -> c.getValue().fechaRegistroProperty());
        colFecha.setPrefWidth(130);

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
                    filtrar();
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

        table.getColumns().addAll(colId, colNombre, colTipo, colEspecialidad, colEstado,colFecha, colAcciones);
        table.setItems(db.getGastronomias());

        container.getChildren().addAll(filterBar, table);
        return container;
    }

    private void filtrar() {  //se usan todos los metodos ya crreados,//
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
           //------ALTA------//

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

        ComboBox<TipoEstablecimiento> cbTipo = new ComboBox<>(db.getTiposEstablecimiento());
        if (!cbTipo.getItems().isEmpty()) {
            cbTipo.getSelectionModel().selectFirst();
        }
        Button btnNuevoTipo = new Button("+ Nuevo");
        btnNuevoTipo.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
        btnNuevoTipo.setOnAction(e -> {
            TextInputDialog tipoDialog = new TextInputDialog();
            tipoDialog.setTitle("Nuevo Tipo");
            tipoDialog.setHeaderText("Crear nuevo tipo de establecimiento");
            tipoDialog.setContentText("Nombre:");
            tipoDialog.showAndWait().ifPresent(nombreTipo -> {
                if (!nombreTipo.trim().isEmpty()) {
                    TipoEstablecimiento nuevo = db.insertarTipoEstablecimiento(nombreTipo.trim());
                    if (nuevo != null) {
                        cbTipo.getItems().add(nuevo);
                        cbTipo.setValue(nuevo);
                    }
                }
            });
        });
        HBox tipoBox = new HBox(5, cbTipo, btnNuevoTipo);



        cbTipo.setStyle("-fx-font-size: 12px;");
        TextField txtEspecialidad = new TextField();
        txtEspecialidad.setPromptText("Especialidad");
        txtEspecialidad.setStyle("-fx-font-size: 12px;");
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue("Activo");
        cbEstado.setStyle("-fx-font-size: 12px;");

        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        Label lblErrorNombre = new Label();
        lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorNombre.setWrapText(true);
        lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 1);
        grid.add(new Label("Tipo:"), 0, 2);
        grid.add(tipoBox, 1, 2);
        grid.add(new Label("Especialidad*:"), 0, 3);
        grid.add(txtEspecialidad, 1, 3);

        Label lblErrorEspecialidad = new Label();
        lblErrorEspecialidad.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorEspecialidad.setWrapText(true);
        lblErrorEspecialidad.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);
        GridPane.setColumnSpan(lblErrorEspecialidad, 2);
        grid.add(lblErrorEspecialidad, 0, 4);



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


            if (txtEspecialidad.getText() == null || txtEspecialidad.getText().isEmpty()) {
                lblErrorEspecialidad.setText("La especialidad es obligatoria");
                txtEspecialidad.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtEspecialidad.getText().charAt(0))) {
                lblErrorEspecialidad.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtEspecialidad.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorEspecialidad.setText("");
                txtEspecialidad.setStyle("-fx-font-size: 12px;");
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
        txtEspecialidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                lblErrorEspecialidad.setText("");
                txtEspecialidad.setStyle("-fx-font-size: 12px;");
            }
        });


        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                String id = db.getNextGastronomiaId();
                TipoEstablecimiento tipoSeleccionado = cbTipo.getValue();
                return new Gastronomia(id, txtNombre.getText(), tipoSeleccionado.getNombre(), tipoSeleccionado.getId(),
                        txtEspecialidad.getText(), cbEstado.getValue(), "");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            db.insertarGastronomia(g);
            filtrar();
        });
    }
             //------EDITAR------//
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

        boolean esInactivo = "Inactivo".equals(gastronomia.getEstado());
        String estiloDeshabilitado = "-fx-font-size: 12px; -fx-background-color: #e9ecef; -fx-text-fill: #6c757d;";
        String estiloNormal = "-fx-font-size: 12px;";

        TextField txtNombre = new TextField(gastronomia.getNombre());
        txtNombre.setStyle(esInactivo ? estiloDeshabilitado : estiloNormal);
        txtNombre.setDisable(esInactivo);

        Label lblErrorNombre = new Label();
        lblErrorNombre.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorNombre.setWrapText(true);
        lblErrorNombre.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);

        ComboBox<TipoEstablecimiento> cbTipo = new ComboBox<>(db.getTiposEstablecimiento());
        for (TipoEstablecimiento t : cbTipo.getItems()) {
            if (t.getId().equals(gastronomia.getTipoId())) {
                cbTipo.setValue(t);
                break;
            }
        }
        Button btnNuevoTipo = new Button("+ Nuevo");
        btnNuevoTipo.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
        btnNuevoTipo.setOnAction(e -> {
            TextInputDialog tipoDialog = new TextInputDialog();
            tipoDialog.setTitle("Nuevo Tipo");
            tipoDialog.setHeaderText("Crear nuevo tipo de establecimiento");
            tipoDialog.setContentText("Nombre:");
            tipoDialog.showAndWait().ifPresent(nombreTipo -> {
                if (!nombreTipo.trim().isEmpty()) {
                    TipoEstablecimiento nuevo = db.insertarTipoEstablecimiento(nombreTipo.trim());
                    if (nuevo != null) {
                        cbTipo.getItems().add(nuevo);
                        cbTipo.setValue(nuevo);
                    }
                }
            });
        });
        btnNuevoTipo.setDisable(esInactivo);
        HBox tipoBox = new HBox(5, cbTipo, btnNuevoTipo);
        cbTipo.setStyle(esInactivo ? estiloDeshabilitado : estiloNormal);
        cbTipo.setDisable(esInactivo);

        TextField txtEspecialidad = new TextField(gastronomia.getEspecialidad());
        txtEspecialidad.setStyle(esInactivo ? estiloDeshabilitado : estiloNormal);
        txtEspecialidad.setDisable(esInactivo);

        Label lblErrorEspecialidad = new Label();
        lblErrorEspecialidad.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lblErrorEspecialidad.setWrapText(true);
        lblErrorEspecialidad.setMaxWidth(Double.MAX_VALUE);
        lblErrorNombre.setPrefWidth(320);




        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        GridPane.setColumnSpan(lblErrorNombre, 2);
        grid.add(lblErrorNombre, 0, 1);
        grid.add(new Label("Tipo:"), 0, 2);
        grid.add(tipoBox, 1, 2);
        grid.add(new Label("Especialidad*" + ":"), 0, 3);
        grid.add(txtEspecialidad, 1, 3);
        GridPane.setColumnSpan(lblErrorEspecialidad, 2);
        grid.add(lblErrorEspecialidad, 0, 4);


        if (esInactivo) {
            Label lblAviso = new Label("⚠ Establecimiento inactivo: no se puede editar. Reactívelo primero.");
            lblAviso.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold;");
            GridPane.setColumnSpan(lblAviso, 2);
            grid.add(lblAviso, 0, 6);
        }

        dialog.getDialogPane().setContent(grid);
        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.setDisable(esInactivo);

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
                txtNombre.setStyle(esInactivo ? estiloDeshabilitado : estiloNormal);
            }

            if (txtEspecialidad.getText() == null || txtEspecialidad.getText().isEmpty()) {
                lblErrorEspecialidad.setText("La especialidad es obligatoria");
                txtEspecialidad.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else if (!Character.isLetter(txtEspecialidad.getText().charAt(0))) {
                lblErrorEspecialidad.setText("Debe comenzar con una letra, sin espacios al inicio");
                txtEspecialidad.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px; -fx-border-radius: 4;");
                valido = false;
            } else {
                lblErrorEspecialidad.setText("");
                txtEspecialidad.setStyle(esInactivo ? estiloDeshabilitado : estiloNormal);
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
        txtEspecialidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                lblErrorEspecialidad.setText("");
                txtEspecialidad.setStyle("-fx-font-size: 12px;");
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                return new Gastronomia(gastronomia.getId(), txtNombre.getText(), cbTipo.getValue().getNombre(), cbTipo.getValue().getId(),
                        txtEspecialidad.getText(), gastronomia.getEstado(),gastronomia.getFechaRegistro());
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
            private void mostrarDialogoPapelera() {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Papelera de Gastronomía");
            dialog.setHeaderText("Establecimientos eliminados");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            TableView<Gastronomia> tablaPapelera = new TableView<>();
            tablaPapelera.setPrefSize(500, 250);

            TableColumn<Gastronomia, String> colNombre = new TableColumn<>("Nombre");
            colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
            colNombre.setPrefWidth(150);

            TableColumn<Gastronomia, String> colTipo = new TableColumn<>("Tipo");
            colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
            colTipo.setPrefWidth(100);

            TableColumn<Gastronomia, Void> colAccion = new TableColumn<>("Acción");
            colAccion.setPrefWidth(100);
            colAccion.setCellFactory(col -> new TableCell<Gastronomia, Void>() {
                private final Button btnRestaurar = new Button("Restaurar");
                {
                    btnRestaurar.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 6; -fx-font-size: 11px;");
                    btnRestaurar.setOnAction(e -> {
                        Gastronomia g = getTableView().getItems().get(getIndex());
                        db.restaurarGastronomia(g.getId());
                        tablaPapelera.setItems(db.getGastronomiasEliminadas());
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
            tablaPapelera.setItems(db.getGastronomiasEliminadas());

            dialog.getDialogPane().setContent(tablaPapelera);
            dialog.showAndWait();
        }


}