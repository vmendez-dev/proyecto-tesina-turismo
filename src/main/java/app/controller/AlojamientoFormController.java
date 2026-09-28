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
import java.util.ArrayList;
import java.util.List;
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
    @FXML private CheckBox chkWifi;
    @FXML private CheckBox chkCochera;
    @FXML private CheckBox chkPileta;
    @FXML private CheckBox chkDesayuno;
    @FXML private CheckBox chkClimatizacion;
    @FXML private Button btnGuardar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTipo.setItems(FXCollections.observableArrayList(
                "Albergue", "Apart Hotel", "Cabaña", "Camping", "Complejo Turístico","Departamento / Casa", "Glamping", "Hostel", "Hostería", "Hotel", "Posada", "Residencial"
        ));
        cbCategoria.setItems(FXCollections.observableArrayList(
                "1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas", "Estándar / Sin Categoría"
        ));
    }

    public void setAlojamientoParaModificar(Alojamiento a) {
        this.alojamientoEnEdicion = a;
        lblTituloForm.setText("Datos del Alojamiento");
        lblSubtituloForm.setText("Modifique los campos necesarios y guarde los cambios.");
        btnGuardar.setText("Guardar");

        txtNombre.setText(a.getNombre());
        cbTipo.setValue(a.getTipo());
        cbCategoria.setValue(a.getCategoria());
        txtDireccion.setText(a.getDireccion());
        txtTelefono.setText(a.getTelefono());
        txtCapacidad.setText(String.valueOf(a.getCapacidad()));
        txtNombreDueno.setText(a.getNombreDueno());
        txtDniDueno.setText(a.getDniDueno());
        txtDescripcion.setText(a.getDescripcion());

        if (a.getAmenities() != null) {
            chkWifi.setSelected(a.getAmenities().contains("Wifi"));
            chkCochera.setSelected(a.getAmenities().contains("Cochera"));
            chkPileta.setSelected(a.getAmenities().contains("Pileta"));
            chkDesayuno.setSelected(a.getAmenities().contains("Desayuno"));
            chkClimatizacion.setSelected(a.getAmenities().contains("Climatización"));
        }
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
                nuevo.setEstado("Activo");
                mapearCampos(nuevo, capacidad);

                if (alojamientoDAO.insertar(nuevo)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", "El nuevo alojamiento fue dado de alta en el sistema.");
                    cerrarVentana();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo registrar el alojamiento. Intente nuevamente.");
                }
            } else {
                mapearCampos(alojamientoEnEdicion, capacidad);

                if (alojamientoDAO.actualizar(alojamientoEnEdicion)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Actualización Exitosa", "Los datos del establecimiento se modificaron correctamente.");
                    cerrarVentana();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudieron actualizar los datos del establecimiento.");
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato no válido", "La capacidad debe ser un número entero mayor a cero.");
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
        a.setAmenities(obtenerAmenitiesSeleccionadas());
    }

    private String obtenerAmenitiesSeleccionadas() {
        List<String> seleccionados = new ArrayList<>();
        if (chkWifi.isSelected()) seleccionados.add("Wifi");
        if (chkCochera.isSelected()) seleccionados.add("Cochera");
        if (chkPileta.isSelected()) seleccionados.add("Pileta");
        if (chkDesayuno.isSelected()) seleccionados.add("Desayuno");
        if (chkClimatizacion.isSelected()) seleccionados.add("Climatización");
        return String.join(", ", seleccionados);
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || cbTipo.getValue() == null
                || cbCategoria.getValue() == null || txtCapacidad.getText().trim().isEmpty()
                || txtDireccion.getText().trim().isEmpty()|| txtTelefono.getText() == null
                || txtTelefono.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Nombre, Tipo, Categoría, Plazas, Dirección y Telefono son obligatorios.");
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