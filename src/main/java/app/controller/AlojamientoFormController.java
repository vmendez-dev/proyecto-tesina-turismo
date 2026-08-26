package app.controller;

import app.model.Alojamiento;
import app.model.AlojamientoDAO;
import app.model.CRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AlojamientoFormController implements Initializable {

    private final CRUD<Alojamiento> alojamientoDAO = new AlojamientoDAO();
    private Alojamiento alojamientoEnEdicion = null;
    private Runnable alGuardarCallback;

    @FXML private Label lblTituloForm;
    @FXML private Label lblSubtituloForm;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtNombreDueno;
    @FXML private TextField txtDniDueno;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnGuardar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTipo.setItems(FXCollections.observableArrayList("Cabaña", "Hotel", "Hostal", "Posada"));
        cbCategoria.setItems(FXCollections.observableArrayList("1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas"));
        cbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.getSelectionModel().select("Activo");
    }

    public void setAlojamientoParaModificar(Alojamiento a) {
        this.alojamientoEnEdicion = a;
        lblTituloForm.setText("Modificar Alojamiento");
        lblSubtituloForm.setText("Edita los datos del establecimiento seleccionado.");
        btnGuardar.setText("Guardar Cambios");

        txtNombre.setText(a.getNombre());
        cbTipo.setValue(a.getTipo());
        cbCategoria.setValue(a.getCategoria());
        txtDireccion.setText(a.getDireccion());
        txtTelefono.setText(a.getTelefono());
        txtCapacidad.setText(String.valueOf(a.getCapacidad()));
        txtNombreDueno.setText(a.getNombreDueno());
        txtDniDueno.setText(a.getDniDueno());
        txtDescripcion.setText(a.getDescripcion());
        cbEstado.setValue(a.getEstado());
    }

    public void setAlGuardarCallback(Runnable callback) {
        this.alGuardarCallback = callback;
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());

            if (alojamientoEnEdicion == null) {
                Alojamiento nuevo = new Alojamiento();
                mapearCampos(nuevo, capacidad);

                if (alojamientoDAO.insertar(nuevo)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Alojamiento registrado correctamente.");
                    cerrarVentana();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar en la base de datos.");
                }
            } else {
                mapearCampos(alojamientoEnEdicion, capacidad);

                if (alojamientoDAO.actualizar(alojamientoEnEdicion)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Alojamiento actualizado correctamente.");
                    cerrarVentana();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el registro.");
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato Inválido", "La capacidad debe ser un número entero.");
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato Inválido", e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void mapearCampos(Alojamiento a, int capacidad) {
        a.setNombre(txtNombre.getText().trim());
        a.setTipo(cbTipo.getValue());
        a.setCategoria(cbCategoria.getValue());
        a.setDireccion(txtDireccion.getText().trim());
        a.setTelefono(txtTelefono.getText().trim());
        a.setCapacidad(capacidad);
        a.setNombreDueno(txtNombreDueno.getText().trim());
        a.setDniDueno(txtDniDueno.getText().trim());
        a.setDescripcion(txtDescripcion.getText().trim());
        a.setEstado(cbEstado.getValue());
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || cbTipo.getValue() == null
                || cbCategoria.getValue() == null || txtCapacidad.getText().trim().isEmpty()
                || txtDireccion.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obligatorios", "Complete los campos obligatorios (*).");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (alGuardarCallback != null) {
            alGuardarCallback.run();
        }
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}