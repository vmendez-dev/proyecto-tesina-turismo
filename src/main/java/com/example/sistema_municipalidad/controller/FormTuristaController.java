package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.model.Turista;
import com.example.sistema_municipalidad.dao.TuristaDAO;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.dao.ProvinciaDAO;
import com.example.sistema_municipalidad.model.TipoDocumento;
import com.example.sistema_municipalidad.dao.TipoDocumentoDAO;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class FormTuristaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private ComboBox<TipoDocumento> cmbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;
    @FXML private DatePicker dateFechaNacimiento;
    @FXML private ComboBox<Pais> cmbPais;
    @FXML private ComboBox<Provincia> cmbProcedencia;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtObservaciones;

    // =====================================================
    // DAOs
    // =====================================================

    private final PaisDAO paisDAO = new PaisDAO();
    private final ProvinciaDAO provinciaDAO = new ProvinciaDAO();
    private final TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
    private final TuristaDAO turistaDAO = new TuristaDAO();

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    private void initialize() {

        cargarTiposDocumento();
        cargarPaises();

        // Al comenzar, el ComboBox de procedencia está deshabilitado.
        cmbProcedencia.setDisable(true);

        // Cuando se seleccione un país,
        // se cargarán sus provincias.
        cmbPais.setOnAction(event -> cargarProvincias());
    }

    // =====================================================
    // CARGAR TIPOS DE DOCUMENTO
    // =====================================================

    private void cargarTiposDocumento() {

        List<TipoDocumento> tipos =
                tipoDocumentoDAO.listar();

        cmbTipoDocumento.getItems().setAll(tipos);
    }


    // =====================================================
    // CARGAR PAISES
    // =====================================================

    private void cargarPaises() {

        List<Pais> paises =
                paisDAO.listar();

        cmbPais.getItems().setAll(paises);
    }


    // =====================================================
    // CARGAR PROVINCIAS SEGÚN EL PAÍS
    // =====================================================

    private void cargarProvincias() {

        Pais paisSeleccionado =
                cmbPais.getValue();

        // Limpiamos las provincias anteriores.
        cmbProcedencia.getItems().clear();

        // Si no hay país seleccionado,
        // deshabilitamos procedencia.
        if (paisSeleccionado == null) {

            cmbProcedencia.setDisable(true);
            return;
        }

        // Buscamos las provincias correspondientes
        // al país seleccionado.
        List<Provincia> provincias =
                provinciaDAO.listarPorPais(
                        paisSeleccionado.getIdPais()
                );

        cmbProcedencia.getItems().setAll(provincias);

        // Si encontramos provincias, habilitamos el ComboBox.
        cmbProcedencia.setDisable(provincias.isEmpty());
    }


    // =====================================================
    // BOTÓN GUARDAR
    // =====================================================

    @FXML
    private void guardar() {

        // Primero validamos todos los campos.
        if (!validarCampos()) {
            return;
        }

        // =================================================
        // COMPROBAR DOCUMENTO DUPLICADO
        // =================================================

        int idTipoDocumento = cmbTipoDocumento.getValue().getIdTipoDocumento();
        String numeroDocumento = txtNumeroDocumento.getText().trim();

        if (turistaDAO.existeDocumento(idTipoDocumento, numeroDocumento)) {
            mostrarError("Ya existe un turista registrado con ese tipo y número de documento.");
            txtNumeroDocumento.requestFocus();
            return;
        }

        // =================================================
        // OBTENER VALORES DE LOS COMBOBOX
        // =================================================

        TipoDocumento tipoDocumento =
                cmbTipoDocumento.getValue();

        Pais pais =
                cmbPais.getValue();

        Provincia provincia =
                cmbProcedencia.getValue();

        // =================================================
        // CREAR OBJETO TURISTA
        // =================================================

        Turista turista = new Turista(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                tipoDocumento.getIdTipoDocumento(),
                txtNumeroDocumento.getText().trim(),
                dateFechaNacimiento.getValue(),
                provincia.getIdProvincia(),
                pais.getIdPais(),
                txtTelefono.getText().trim(),
                txtEmail.getText().trim(),
                txtObservaciones.getText().trim()
        );

        // =================================================
        // GUARDAR EN LA BASE DE DATOS
        // =================================================

        boolean guardado = turistaDAO.guardar(turista);
        if (guardado) {
            mostrarInformacion(
                    "Registro exitoso",
                    "El turista fue registrado correctamente."
            );
            cerrarVentana();

        } else {
            mostrarError("No se pudo registrar el turista.");
        }
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    private boolean validarCampos() {

        // -----------------------------------------------
        // NOMBRE
        // -----------------------------------------------

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("El nombre es obligatorio.");

            txtNombre.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // APELLIDO
        // -----------------------------------------------

        String apellido = txtApellido.getText().trim();

        if (apellido.isEmpty()) {
            mostrarError("El apellido es obligatorio.");

            txtApellido.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // TIPO DE DOCUMENTO
        // -----------------------------------------------

        if (cmbTipoDocumento.getValue() == null) {

            mostrarError("Debe seleccionar un tipo de documento.");

            cmbTipoDocumento.requestFocus();
            return false;
        }


        // -----------------------------------------------
        // NÚMERO DE DOCUMENTO
        // -----------------------------------------------

        String numeroDocumento = txtNumeroDocumento.getText().trim();

        if (numeroDocumento.isEmpty()) {
            mostrarError("El número de documento es obligatorio.");

            txtNumeroDocumento.requestFocus();
            return false;
        }
        if (numeroDocumento.length() > 30) {
            mostrarError("El número de documento no puede superar los 30 caracteres.");

            txtNumeroDocumento.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // FECHA DE NACIMIENTO
        // -----------------------------------------------

        LocalDate fechaNacimiento = dateFechaNacimiento.getValue();

        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {

            mostrarError("La fecha de nacimiento no puede ser futura.");

            dateFechaNacimiento.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // PAÍS
        // -----------------------------------------------

        if (cmbPais.getValue() == null) {

            mostrarError("Debe seleccionar un país.");

            cmbPais.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // PROCEDENCIA
        // -----------------------------------------------

        if (cmbProcedencia.getValue() == null) {

            mostrarError("Debe seleccionar una procedencia.");

            cmbProcedencia.requestFocus();
            return false;
        }

        // -----------------------------------------------
        // TELÉFONO
        // -----------------------------------------------

        String telefono = txtTelefono.getText().trim();

        if (telefono.length() > 30) {
            mostrarError("El teléfono no puede superar los 30 caracteres.");

            txtTelefono.requestFocus();
            return false;
        }


        // -----------------------------------------------
        // EMAIL
        // -----------------------------------------------

        String email = txtEmail.getText().trim();

        if (!email.isEmpty()) {

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                mostrarError("El email no tiene un formato válido.");

                txtEmail.requestFocus();
                return false;
            }

            if (email.length() > 100) {

                mostrarError("El email no puede superar los 100 caracteres.");

                txtEmail.requestFocus();
                return false;
            }
        }


        // -----------------------------------------------
        // OBSERVACIONES
        // -----------------------------------------------

        String observaciones = txtObservaciones.getText().trim();

        if (observaciones.length() > 1000) {

            mostrarError("Las observaciones son demasiado extensas.");

            txtObservaciones.requestFocus();
            return false;
        }


        return true;
    }

    // =====================================================
    // BOTÓN CANCELAR
    // =====================================================

    @FXML
    private void cancelar() {

        cerrarVentana();
    }

    // =====================================================
    // CERRAR VENTANA
    // =====================================================

    private void cerrarVentana() {

        Stage ventana = (Stage) txtNombre.getScene().getWindow();
        ventana.close();
    }


    // =====================================================
    // MOSTRAR ERROR
    // =====================================================

    private void mostrarError(String mensaje) {

        Alert alert = new Alert(
                        Alert.AlertType.ERROR,
                        mensaje,
                        ButtonType.OK
                );

        alert.setTitle("Error");
        alert.setHeaderText(null);

        alert.showAndWait();
    }

    // =====================================================
    // MOSTRAR INFORMACIÓN
    // =====================================================

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        mensaje,
                        ButtonType.OK
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);

        alert.showAndWait();
    }
}