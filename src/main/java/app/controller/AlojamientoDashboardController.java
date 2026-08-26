package app.controller;

import app.model.Alojamiento;
import app.model.AlojamientoDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AlojamientoDashboardController implements Initializable {

    private final AlojamientoDAO alojamientoDAO = new AlojamientoDAO();
    private final ObservableList<Alojamiento> masterData = FXCollections.observableArrayList();

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroTipo;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private ComboBox<String> cbFiltroEstado;

    @FXML private TableView<Alojamiento> tablaAlojamientos;
    @FXML private TableColumn<Alojamiento, Integer> colId;
    @FXML private TableColumn<Alojamiento, String> colNombre;
    @FXML private TableColumn<Alojamiento, String> colTipo;
    @FXML private TableColumn<Alojamiento, String> colDireccion;
    @FXML private TableColumn<Alojamiento, Integer> colCapacidad;
    @FXML private TableColumn<Alojamiento, String> colTelefono;
    @FXML private TableColumn<Alojamiento, String> colEstado;
    @FXML private TableColumn<Alojamiento, Void> colAcciones;

    @FXML private Label lblTotalAlojamientos;
    @FXML private Label lblNuevosMes;
    @FXML private Label lblCapacidadTotal;
    @FXML private Label lblAlojamientosActivos;

    @FXML private TableView<Alojamiento> tablaRecientes;
    @FXML private TableColumn<Alojamiento, String> colRecienteNombre;
    @FXML private TableColumn<Alojamiento, String> colRecienteTipo;
    @FXML private TableColumn<Alojamiento, String> colRecienteDireccion;
    @FXML private TableColumn<Alojamiento, Integer> colRecienteCapacidad;
    @FXML private TableColumn<Alojamiento, String> colRecienteFecha;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarFiltrosDesplegables();
        configurarColumnasTablaPrincipal();
        configurarColumnasTablaRecientes();
        cargarDatos();
        configurarFiltrosEnTiempoReal();
    }

    private void configurarFiltrosDesplegables() {
        cbFiltroTipo.setItems(FXCollections.observableArrayList("Todos", "Cabaña", "Hotel", "Hostal", "Posada"));
        cbFiltroTipo.getSelectionModel().selectFirst();

        cbFiltroCategoria.setItems(FXCollections.observableArrayList("Todas", "1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas"));
        cbFiltroCategoria.getSelectionModel().selectFirst();

        cbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        cbFiltroEstado.getSelectionModel().selectFirst();
    }

    private void configurarColumnasTablaPrincipal() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAlojamiento"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(estado);
                    badge.getStyleClass().add(estado.equalsIgnoreCase("Activo") ? "badge-activo" : "badge-inactivo");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁");
            private final Button btnEditar = new Button("✏");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(6, btnVer, btnEditar, btnEliminar);

            {
                btnVer.getStyleClass().add("btn-action");
                btnEditar.getStyleClass().add("btn-action");
                btnEliminar.getStyleClass().add("btn-action-delete");
                contenedor.setAlignment(Pos.CENTER);

                btnVer.setOnAction(e -> {
                    Alojamiento a = getTableView().getItems().get(getIndex());
                    mostrarDetalle(a);
                });

                btnEditar.setOnAction(e -> {
                    Alojamiento a = getTableView().getItems().get(getIndex());
                    abrirFormularioModificar(a);
                });

                btnEliminar.setOnAction(e -> {
                    Alojamiento a = getTableView().getItems().get(getIndex());
                    confirmarBajaLogica(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void configurarColumnasTablaRecientes() {
        colRecienteNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRecienteTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colRecienteDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colRecienteCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colRecienteFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaRegistro();
            return new SimpleStringProperty(fecha != null ? fecha.toString() : "Reciente");
        });
    }

    public void cargarDatos() {
        List<Alojamiento> lista = alojamientoDAO.listarTodos();
        masterData.setAll(lista);

        int total = masterData.size();
        long activos = masterData.stream().filter(a -> "Activo".equalsIgnoreCase(a.getEstado())).count();
        int capacidadTotal = masterData.stream().mapToInt(Alojamiento::getCapacidad).sum();
        long nuevosEsteMes = masterData.stream()
                .filter(a -> a.getFechaRegistro() != null &&
                        a.getFechaRegistro().getMonth() == LocalDate.now().getMonth() &&
                        a.getFechaRegistro().getYear() == LocalDate.now().getYear())
                .count();

        lblTotalAlojamientos.setText(String.valueOf(total));
        lblAlojamientosActivos.setText(String.valueOf(activos));
        lblCapacidadTotal.setText(String.valueOf(capacidadTotal));
        lblNuevosMes.setText(String.valueOf(nuevosEsteMes));

        tablaRecientes.setItems(FXCollections.observableArrayList(alojamientoDAO.listarUltimosRegistrados(5)));
    }

    private void configurarFiltrosEnTiempoReal() {
        FilteredList<Alojamiento> filteredData = new FilteredList<>(masterData, p -> true);

        Runnable aplicarFiltros = () -> {
            String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
            String tipoSel = cbFiltroTipo.getValue();
            String catSel = cbFiltroCategoria.getValue();
            String estadoSel = cbFiltroEstado.getValue();

            filteredData.setPredicate(a -> {
                boolean coincideTexto = texto.isEmpty() ||
                        a.getNombre().toLowerCase().contains(texto) ||
                        a.getDireccion().toLowerCase().contains(texto);

                boolean coincideTipo = tipoSel == null || tipoSel.equals("Todos") ||
                        a.getTipo().equalsIgnoreCase(tipoSel);

                boolean coincideCat = catSel == null || catSel.equals("Todas") ||
                        a.getCategoria().equalsIgnoreCase(catSel);

                boolean coincideEstado = estadoSel == null || estadoSel.equals("Todos") ||
                        a.getEstado().equalsIgnoreCase(estadoSel);

                return coincideTexto && coincideTipo && coincideCat && coincideEstado;
            });
        };

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros.run());
        cbFiltroTipo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros.run());
        cbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros.run());
        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros.run());

        tablaAlojamientos.setItems(filteredData);
    }

    @FXML
    public void abrirFormularioNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/alojamiento_form.fxml"));
            Parent root = loader.load();

            AlojamientoFormController formCtrl = loader.getController();
            formCtrl.setAlGuardarCallback(this::cargarDatos);

            Stage stage = new Stage();
            stage.setTitle("Registrar Nuevo Alojamiento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirFormularioModificar(Alojamiento a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/alojamiento_form.fxml"));
            Parent root = loader.load();

            AlojamientoFormController formCtrl = loader.getController();
            formCtrl.setAlojamientoParaModificar(a);
            formCtrl.setAlGuardarCallback(this::cargarDatos);

            Stage stage = new Stage();
            stage.setTitle("Modificar Alojamiento - " + a.getNombre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarDetalle(Alojamiento a) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Alojamiento");
        alert.setHeaderText(a.getNombre());
        alert.setContentText(
                "Resumen: " + a.obtenerResumen() + "\n\n" +
                        "Dirección: " + a.getDireccion() + "\n" +
                        "Teléfono: " + a.getTelefono() + "\n" +
                        "Dueño: " + a.getNombreDueno() + " (DNI: " + a.getDniDueno() + ")\n" +
                        "Estado: " + a.getEstado() + "\n" +
                        "Descripción: " + (a.getDescripcion() != null ? a.getDescripcion() : "Sin descripción")
        );
        alert.showAndWait();
    }

    private void confirmarBajaLogica(Alojamiento a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Dar de baja el alojamiento '" + a.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (alojamientoDAO.eliminar(a.getIdAlojamiento())) {
                cargarDatos();
            }
        }
    }
}