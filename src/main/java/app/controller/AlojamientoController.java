package app.controller;

import app.model.Alojamiento;
import app.model.AlojamientoDAO;
import app.model.CRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class AlojamientoController implements Initializable {

    private final CRUD<Alojamiento> alojamientoDAO = new AlojamientoDAO();

    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNombreDueno;
    @FXML private TextField txtDniDueno;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtDescripcion;

    @FXML private TableView<Alojamiento> tablaAlojamientos;
    @FXML private TableColumn<Alojamiento, Integer> colId;
    @FXML private TableColumn<Alojamiento, String> colNombre;
    @FXML private TableColumn<Alojamiento, String> colTipo;
    @FXML private TableColumn<Alojamiento, String> colCategoria;
    @FXML private TableColumn<Alojamiento, String> colDireccion;
    @FXML private TableColumn<Alojamiento, String> colTelefono;
    @FXML private TableColumn<Alojamiento, Integer> colCapacidad;
    @FXML private TableColumn<Alojamiento, String> colDueno;
    @FXML private TableColumn<Alojamiento, String> colEstado;

    private Alojamiento alojamientoSeleccionado = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTipo.setItems(FXCollections.observableArrayList("Cabaña", "Hotel", "Hostal", "Posada"));
        cbCategoria.setItems(FXCollections.observableArrayList("1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas"));
        cbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.getSelectionModel().select("Activo");

        colId.setCellValueFactory(new PropertyValueFactory<>("idAlojamiento"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colDueno.setCellValueFactory(new PropertyValueFactory<>("nombreDueno"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaAlojamientos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, nuevo) -> {
            if (nuevo != null) {
                alojamientoSeleccionado = nuevo;
                cargarFormulario(nuevo);
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        List<Alojamiento> lista = alojamientoDAO.listarTodos();
        ObservableList<Alojamiento> datos = FXCollections.observableArrayList(lista);
        tablaAlojamientos.setItems(datos);
    }

    private void cargarFormulario(Alojamiento a) {
        txtNombre.setText(a.getNombre());
        cbTipo.setValue(a.getTipo());
        cbCategoria.setValue(a.getCategoria());
        txtCapacidad.setText(String.valueOf(a.getCapacidad()));
        txtDireccion.setText(a.getDireccion());
        txtTelefono.setText(a.getTelefono());
        txtNombreDueno.setText(a.getNombreDueno());
        txtDniDueno.setText(a.getDniDueno());
        cbEstado.setValue(a.getEstado());
        txtDescripcion.setText(a.getDescripcion());
    }

    @FXML
    private void guardarAlojamiento() {
        if (!validarCampos()) {
            return;
        }

        try {
            Alojamiento nuevo = new Alojamiento();
            nuevo.setNombre(txtNombre.getText().trim());
            nuevo.setTipo(cbTipo.getValue());
            nuevo.setCategoria(cbCategoria.getValue());
            nuevo.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            nuevo.setDireccion(txtDireccion.getText().trim());
            nuevo.setTelefono(txtTelefono.getText().trim());
            nuevo.setNombreDueno(txtNombreDueno.getText().trim());
            nuevo.setDniDueno(txtDniDueno.getText().trim());
            nuevo.setEstado(cbEstado.getValue());
            nuevo.setDescripcion(txtDescripcion.getText().trim());

            if (alojamientoDAO.insertar(nuevo)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Alojamiento registrado correctamente.");
                cargarTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar en la base de datos.");
            }
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato inválido", e.getMessage());
        }
    }

    @FXML
    private void modificarAlojamiento() {
        if (alojamientoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccione un alojamiento de la tabla para modificar.");
            return;
        }
        if (!validarCampos()) {
            return;
        }

        try {
            alojamientoSeleccionado.setNombre(txtNombre.getText().trim());
            alojamientoSeleccionado.setTipo(cbTipo.getValue());
            alojamientoSeleccionado.setCategoria(cbCategoria.getValue());
            alojamientoSeleccionado.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            alojamientoSeleccionado.setDireccion(txtDireccion.getText().trim());
            alojamientoSeleccionado.setTelefono(txtTelefono.getText().trim());
            alojamientoSeleccionado.setNombreDueno(txtNombreDueno.getText().trim());
            alojamientoSeleccionado.setDniDueno(txtDniDueno.getText().trim());
            alojamientoSeleccionado.setEstado(cbEstado.getValue());
            alojamientoSeleccionado.setDescripcion(txtDescripcion.getText().trim());

            if (alojamientoDAO.actualizar(alojamientoSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Alojamiento actualizado correctamente.");
                cargarTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el registro.");
            }
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato inválido", e.getMessage());
        }
    }

    @FXML
    private void eliminarAlojamiento() {
        if (alojamientoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccione un alojamiento de la tabla para dar de baja.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de dar de baja a '" + alojamientoSeleccionado.getNombre() + "'?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            if (alojamientoDAO.eliminar(alojamientoSeleccionado.getIdAlojamiento())) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Baja Confirmada", "El alojamiento se dio de baja correctamente (estado Inactivo).");
                cargarTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado en la base de datos.");
            }
        }
    }

    @FXML
    private void limpiarCampos() {
        alojamientoSeleccionado = null;
        txtNombre.clear();
        cbTipo.setValue(null);
        cbCategoria.setValue(null);
        txtCapacidad.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtNombreDueno.clear();
        txtDniDueno.clear();
        cbEstado.setValue("Activo");
        txtDescripcion.clear();
        tablaAlojamientos.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || cbTipo.getValue() == null
                || cbCategoria.getValue() == null || txtCapacidad.getText().trim().isEmpty()
                || txtDireccion.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Nombre, Tipo, Categoría, Plazas y Dirección son obligatorios.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}