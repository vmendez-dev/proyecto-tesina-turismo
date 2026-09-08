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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private ImageView imgIcono;
    @FXML private Button btnGuardar;

    private final PaisDAO paisDAO = new PaisDAO();
    private final ProvinciaDAO provinciaDAO = new ProvinciaDAO();
    private final TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
    private final TuristaDAO turistaDAO = new TuristaDAO();
    private Turista turistaEdicion;

    @FXML
    private void initialize() {
        cargarTiposDocumento();
        cargarPaises();

        // Al comenzar, el ComboBox de procedencia está deshabilitado.
        cmbProcedencia.setDisable(true);

        // Cuando se seleccione un país,
        // se cargarán sus provincias.
        cmbPais.setOnAction(event -> cargarProvincias());

        // Validar teléfono mientras el usuario escribe
        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\+?[0-9]*")) {
                txtTelefono.setText(oldValue);
            }
        });
    }

    public void setTurista(Turista turista) {

        this.turistaEdicion = turista;

        //Si el objeto es null, estamos registrando a un turista.
        if (turista == null) {
            limpiarFormulario();
            return;
        }

        // Si tiene un objeto, estamos modificando.
        cambiarEncabezado(
                "Modificar turista",
                "Edite los datos del turista seleccionado",
                "Guardar cambios",
                "/icons/modificar.png"
        );

        cargarDatosTurista(turista);
    }

    private void cargarTiposDocumento() {
        List<TipoDocumento> tipos = tipoDocumentoDAO.listar();
        cmbTipoDocumento.getItems().setAll(tipos);
    }

    private void cargarPaises() {
        List<Pais> paises = paisDAO.listar();
        cmbPais.getItems().setAll(paises);
    }

    // CARGAR PROVINCIAS SEGÚN EL PAÍS:

    private void cargarProvincias() {
        Pais paisSeleccionado = cmbPais.getValue();

        // Limpiamos las provincias anteriores.
        cmbProcedencia.getItems().clear();

        // Si no hay país seleccionado, deshabilitamos procedencia.
        if (paisSeleccionado == null) {
            cmbProcedencia.setDisable(true);
            return;
        }

        // Buscamos las provincias correspondientes al país seleccionado.
        List<Provincia> provincias = provinciaDAO.listarPorPais(paisSeleccionado.getIdPais());
        cmbProcedencia.getItems().setAll(provincias);

        // Si encontramos provincias, habilitamos el ComboBox.
        cmbProcedencia.setDisable(provincias.isEmpty());
    }

    @FXML
    private void guardar() {

        // Primero se validan todos los campos
        if (!validarCampos()) {
            return;
        }

        // Se comprueba documento duplicado:
        int idTipoDocumento = cmbTipoDocumento.getValue().getIdTipoDocumento();

        String numeroDocumento = txtNumeroDocumento.getText().trim();

        String telefono = txtTelefono.getText().trim();
        if (telefono.startsWith("+")) {
            telefono = telefono.substring(1);
        }

        if (turistaEdicion == null) {

            //Alta:
            if (turistaDAO.existeDocumento(idTipoDocumento, numeroDocumento)) {
                mostrarError("Ya existe un turista registrado con ese tipo y número de documento.");
                txtNumeroDocumento.requestFocus();
                return;
            }

        } else {

            //Modificación:
            if (turistaDAO.existeDocumentoExceptoId(idTipoDocumento, numeroDocumento, turistaEdicion.getIdTurista())) {
                mostrarError("Otro turista ya tiene ese tipo y número de documento.");
                txtNumeroDocumento.requestFocus();
                return;
            }
        }


        // OBTENER VALORES DE LOS COMBOBOX:

        TipoDocumento tipoDocumento = cmbTipoDocumento.getValue();
        Pais pais = cmbPais.getValue();

        Provincia provincia = cmbProcedencia.getValue();
        Integer idProvincia = null;
        if (provincia != null) { idProvincia = provincia.getIdProvincia(); }

        // SE CREA EL OBJETO TURISTA:

        Turista turista = new Turista(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                tipoDocumento.getIdTipoDocumento(),
                txtNumeroDocumento.getText().trim(),
                dateFechaNacimiento.getValue(),
                idProvincia,
                pais.getIdPais(),
                telefono,
                txtEmail.getText().trim(),
                txtObservaciones.getText().trim()
        );


        // SE GUARDA EN LA BASE DE DATOS:

        boolean guardado;

        if (turistaEdicion == null) {

            //Alta:
            guardado = turistaDAO.guardar(turista);

        } else {

            //Modificación:
            turista.setIdTurista(turistaEdicion.getIdTurista());
            guardado = turistaDAO.modificar(turista);
        }

        // RESULTADO:

        if (guardado) {
            if (turistaEdicion == null) {
                mostrarInformacion(
                        "Registro exitoso",
                        "El turista fue registrado correctamente."
                );
            } else {
                mostrarInformacion(
                        "Modificación exitosa",
                        "Los datos del turista fueron modificados correctamente."
                );
            }

            cerrarVentana();

        } else {
            if (turistaEdicion == null) {
                mostrarError(
                        "No se pudo registrar el turista."
                );
            } else {
                mostrarError(
                        "No se pudo modificar el turista."
                );
            }
        }
    }

    //
    // VALIDACIONES:
    //

    private boolean validarCampos() {
        List<String> errores = new ArrayList<>();
        Control primerError = null;

        if (txtNombre.getText().trim().isEmpty()) {
            errores.add("El campo 'Nombre' es obligatorio.");
            if (primerError == null) primerError = txtNombre;
        }

        if (txtApellido.getText().trim().isEmpty()) {
            errores.add("El campo 'Apellido' es obligatorio.");
            if (primerError == null) primerError = txtApellido;
        }

        if (cmbTipoDocumento.getValue() == null) {
            errores.add("Debe seleccionar un 'Tipo de documento'.");
            if (primerError == null) primerError = cmbTipoDocumento;
        }

        String numeroDocumento = txtNumeroDocumento.getText().trim();
        if (numeroDocumento.isEmpty()) {
            errores.add("El campo 'Documento' es obligatorio.");
            if (primerError == null) primerError = txtNumeroDocumento;
        } else {
            String numeroDocumentoLimpio = numeroDocumento.replaceAll("[^A-Za-z0-9]", "");
            txtNumeroDocumento.setText(numeroDocumentoLimpio);

            String nombreTipoDocumento = cmbTipoDocumento.getValue() != null
                    ? cmbTipoDocumento.getValue().getNombreTipo() : "";
            if (nombreTipoDocumento == null) {
                nombreTipoDocumento = "";
            }
            nombreTipoDocumento = nombreTipoDocumento.toUpperCase(Locale.ROOT);

            boolean documentoValido;
            String mensajeDocumentoInvalido;

            if (nombreTipoDocumento.contains("DNI")) {
                documentoValido = numeroDocumentoLimpio.matches("^\\d{7,9}$");
                mensajeDocumentoInvalido = "El DNI debe contener entre 7 y 9 dígitos.";
            } else if (nombreTipoDocumento.contains("PASAPORTE")) {
                documentoValido = numeroDocumentoLimpio.matches("^[A-Za-z0-9]{6,15}$");
                mensajeDocumentoInvalido = "El pasaporte debe ser alfanumérico y tener entre 6 y 15 caracteres.";
            } else {
                documentoValido = numeroDocumentoLimpio.matches("^[A-Za-z0-9]{5,20}$");
                mensajeDocumentoInvalido = "El número de documento debe tener entre 5 y 20 caracteres alfanuméricos.";
            }

            if (!documentoValido) {
                errores.add(mensajeDocumentoInvalido);
                if (primerError == null) primerError = txtNumeroDocumento;
            }
        }

        LocalDate fechaNacimiento = dateFechaNacimiento.getValue();
        if (fechaNacimiento == null) {
            errores.add("Debe seleccionar una 'Fecha de nacimiento'.");
            if (primerError == null) primerError = dateFechaNacimiento;
        } else if (fechaNacimiento.isAfter(LocalDate.now())) {
            errores.add("La fecha de nacimiento no puede ser futura.");
            if (primerError == null) primerError = dateFechaNacimiento;
        }

        if (cmbPais.getValue() == null) {
            errores.add("Debe seleccionar un 'País'.");
            if (primerError == null) primerError = cmbPais;
        }

        Pais paisSeleccionado = cmbPais.getValue();
        if (paisSeleccionado != null
                && paisSeleccionado.getNombrePais().equalsIgnoreCase("Argentina")
                && cmbProcedencia.getValue() == null) {
            errores.add("Para turistas argentinos, debe seleccionar la 'Procedencia' (Provincia).");
            if (primerError == null) primerError = cmbProcedencia;
        }

        String telefono = txtTelefono.getText().trim();
        if (!telefono.isEmpty()) {
            if (!telefono.matches("\\+?[0-9]+")) {
                errores.add("El teléfono solo puede contener números y un signo '+' al comienzo.");
                if (primerError == null) primerError = txtTelefono;
            } else if (telefono.length() > 30) {
                errores.add("El teléfono no puede superar los 30 caracteres.");
                if (primerError == null) primerError = txtTelefono;
            }
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                errores.add("El email no tiene un formato válido.");
                if (primerError == null) primerError = txtEmail;
            } else if (email.length() > 100) {
                errores.add("El email no puede superar los 100 caracteres.");
                if (primerError == null) primerError = txtEmail;
            }
        }

        String observaciones = txtObservaciones.getText().trim();
        if (observaciones.length() > 500) {
            errores.add("Las observaciones son demasiado extensas.");
            if (primerError == null) primerError = txtObservaciones;
        }

        if (!errores.isEmpty()) {
            mostrarError(String.join("\n", errores));
            if (primerError != null) {
                primerError.requestFocus();
            }
            return false;
        }

        return true;
    }

    @FXML
    private void cancelar() {

        cerrarVentana();
    }

    private void cerrarVentana() {

        Stage ventana = (Stage) txtNombre.getScene().getWindow();
        ventana.close();
    }


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

    //
    // MÉTODOS AUXILIARES:
    //
    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        cmbTipoDocumento.setValue(null);
        txtNumeroDocumento.clear();
        dateFechaNacimiento.setValue(null);
        cmbPais.setValue(null);
        cmbProcedencia.getItems().clear();
        cmbProcedencia.setDisable(true);
        txtTelefono.clear();
        txtEmail.clear();
        txtObservaciones.clear();
    }

    private void cargarDatosTurista(Turista turista) {
        txtNombre.setText(turista.getNombre());
        txtApellido.setText(turista.getApellido());
        txtNumeroDocumento.setText(turista.getNumeroDocumento());
        dateFechaNacimiento.setValue(turista.getFechaNacimiento());
        txtTelefono.setText(turista.getTelefono());
        txtEmail.setText(turista.getEmail());
        txtObservaciones.setText(turista.getObservaciones());

        // Seleccionar tipo de documento
        for (TipoDocumento tipo : cmbTipoDocumento.getItems()) {
            if (tipo.getIdTipoDocumento() == turista.getIdTipoDocumento()) {
                cmbTipoDocumento.setValue(tipo);
                break;
            }
        }

        // Seleccionar país
        for (Pais pais : cmbPais.getItems()) {

            if (pais.getIdPais() == turista.getIdPais()) {
                cmbPais.setValue(pais);
                break;
            }
        }

        // Cargar provincias del país seleccionado
        cargarProvincias();

        // Seleccionar procedencia
        for (Provincia provincia : cmbProcedencia.getItems()) {

            if (provincia.getIdProvincia() == turista.getIdProvincia()) {
                cmbProcedencia.setValue(provincia);
                break;
            }
        }
    }

    private void cambiarEncabezado(String titulo, String subtitulo, String textoBoton, String rutaImagen) {
        lblTitulo.setText(titulo);
        lblSubtitulo.setText(subtitulo);
        btnGuardar.setText(textoBoton);

        try {
            Image imagen = new Image(getClass().getResourceAsStream(rutaImagen));
            imgIcono.setImage(imagen);

        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen: " + rutaImagen);
        }
    }

}