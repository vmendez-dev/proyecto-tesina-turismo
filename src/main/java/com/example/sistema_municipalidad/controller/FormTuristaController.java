package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.model.Turista;
import com.example.sistema_municipalidad.dao.TuristaDAO;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.dao.ProvinciaDAO;
import com.example.sistema_municipalidad.model.TipoDocumento;
import com.example.sistema_municipalidad.dao.TipoDocumentoDAO;
import com.example.sistema_municipalidad.helper.AlertHelper;
import com.example.sistema_municipalidad.helper.ValidacionHelper;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
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
    @FXML private Button btnAgregarPais;

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
        // --- NUEVO: LÍMITES DE CARACTERES ---
        ValidacionHelper.limitarLongitud(txtNombre, 50);
        ValidacionHelper.limitarLongitud(txtApellido, 50);
        ValidacionHelper.limitarLongitud(txtNumeroDocumento, 30);
        ValidacionHelper.limitarLongitud(txtTelefono, 30);
        ValidacionHelper.limitarLongitud(txtEmail, 100);
        ValidacionHelper.limitarLongitud(txtObservaciones, 255);
        // --- NUEVO: SOLO LETRAS Y NÚMEROS ---
        ValidacionHelper.permitirSoloLetras(txtNombre);
        ValidacionHelper.permitirSoloLetras(txtApellido);
        ValidacionHelper.permitirSoloTelefono(txtTelefono);
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
            Turista turistaExistente = turistaDAO.buscarPorDocumento(idTipoDocumento, numeroDocumento);

            if (turistaExistente != null) {
                //El documento pertenece a un turista ACTIVO
                if(turistaExistente.isActivo()) {
                    AlertHelper.mostrarError("Ya existe un turista con ese tipo y número de documento.");
                    txtNumeroDocumento.requestFocus();
                    return;

                } else {
                    //El documento pertenece a un turista INACTIVO
                    AlertHelper.mostrarError(
                            "Ya existe un turista con ese documento, "
                                    + "pero se encuentra inactivo.\n\n"
                                    + "Puede reactivarlo desde el filtro "
                                    + "'Inactivos' de la pantalla de turistas."
                    );
                    txtNumeroDocumento.requestFocus();
                    return;
                }
            }

        } else {

            //Modificación:
            if (turistaDAO.existeDocumentoExceptoId(idTipoDocumento, numeroDocumento, turistaEdicion.getIdTurista())) {
                AlertHelper.mostrarError("Otro turista ya tiene ese tipo y número de documento.");
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
                AlertHelper.mostrarInformacion(
                        "Registro exitoso",
                        "El turista fue registrado correctamente."
                );
            } else {
                AlertHelper.mostrarInformacion(
                        "Modificación exitosa",
                        "Los datos del turista fueron modificados correctamente."
                );
            }

            cerrarVentana();

        } else {
            if (turistaEdicion == null) {
                AlertHelper.mostrarError(
                        "No se pudo registrar el turista."
                );
            } else {
                AlertHelper.mostrarError(
                        "No se pudo modificar el turista."
                );
            }
        }
    }

    // VALIDACIONES:

    private boolean validarCampos() {

        List<String> errores = new ArrayList<>();
        Control primerError = null;

        // NOMBRE
        if (txtNombre.getText().trim().isEmpty()) {

            errores.add("El campo 'Nombre' es obligatorio.");

            if (primerError == null) {
                primerError = txtNombre;
            }
        }

        // APELLIDO
        if (txtApellido.getText().trim().isEmpty()) {

            errores.add("El campo 'Apellido' es obligatorio.");

            if (primerError == null) {
                primerError = txtApellido;
            }
        }

        // TIPO DE DOCUMENTO
        if (cmbTipoDocumento.getValue() == null) {

            errores.add("Debe seleccionar un 'Tipo de documento'.");

            if (primerError == null) {
                primerError = cmbTipoDocumento;
            }
        }

        // DOCUMENTO
        String numeroDocumento = txtNumeroDocumento.getText().trim();

        if (numeroDocumento.isEmpty()) {

            errores.add("El campo 'Documento' es obligatorio.");

            if (primerError == null) {
                primerError = txtNumeroDocumento;
            }

        } else {

            String nombreTipoDocumento =
                    cmbTipoDocumento.getValue() != null
                            ? cmbTipoDocumento.getValue().getNombreTipo()
                            : "";

            // Normalizar documento
            String documentoNormalizado =
                    ValidacionHelper.normalizarDocumento(
                            nombreTipoDocumento,
                            numeroDocumento
                    );

            txtNumeroDocumento.setText(documentoNormalizado);

            // Validar documento
            boolean documentoValido =
                    ValidacionHelper.esDocumentoValido(
                            nombreTipoDocumento,
                            documentoNormalizado
                    );

            if (!documentoValido) {

                errores.add(ValidacionHelper.obtenerMensajeDocumentoInvalido(nombreTipoDocumento));

                if (primerError == null) {
                    primerError = txtNumeroDocumento;
                }
            }
        }

        // FECHA DE NACIMIENTO
        LocalDate fechaNacimiento = dateFechaNacimiento.getValue();
        if (fechaNacimiento == null) {

            errores.add("Debe seleccionar una 'Fecha de nacimiento'.");

            if (primerError == null) {
                primerError = dateFechaNacimiento;
            }

        } else if (fechaNacimiento.isAfter(LocalDate.now())) {

            errores.add("La fecha de nacimiento no puede ser futura.");

            if (primerError == null) {
                primerError = dateFechaNacimiento;
            }

        } else if (!ValidacionHelper.esMayorDeEdad(fechaNacimiento)) {

            int edad = ValidacionHelper.calcularEdad(fechaNacimiento);
            errores.add("El turista debe ser mayor de edad (tiene " + edad + " años).");

            if (primerError == null) {
                primerError = dateFechaNacimiento;
            }
        }

        // PAÍS
        if (cmbPais.getValue() == null) {

            errores.add("Debe seleccionar un 'País'.");

            if (primerError == null) {
                primerError = cmbPais;
            }
        }

        // PROCEDENCIA
        Pais paisSeleccionado = cmbPais.getValue();

        if (paisSeleccionado != null
                && paisSeleccionado.getNombrePais()
                .equalsIgnoreCase("Argentina")
                && cmbProcedencia.getValue() == null) {

            errores.add(
                    "Para turistas argentinos, debe seleccionar la 'Procedencia' (Provincia)."
            );

            if (primerError == null) {
                primerError = cmbProcedencia;
            }
        }

        // TELÉFONO
        String telefono = txtTelefono.getText().trim();
        if (!ValidacionHelper.esTelefonoValido(telefono)) {
            errores.add(
                    "El teléfono solo puede contener números y un signo '+' al comienzo."
            );

            if (primerError == null) {
                primerError = txtTelefono;
            }
        }

        // EMAIL
        String email = txtEmail.getText().trim();
        if (!ValidacionHelper.esEmailValido(email)) {

            errores.add(
                    "El email no tiene un formato válido."
            );

            if (primerError == null) {
                primerError = txtEmail;
            }
        }

        // OBSERVACIONES
        String observaciones = txtObservaciones.getText().trim();
        if (observaciones.length() > 255) {

            errores.add(
                    "Las observaciones son demasiado extensas."
            );

            if (primerError == null) {
                primerError = txtObservaciones;
            }
        }

        // MOSTRAR ERRORES
        if (!errores.isEmpty()) {

            AlertHelper.mostrarError(
                    String.join("\n", errores)
            );

            if (primerError != null) {
                primerError.requestFocus();
            }

            return false;
        }

        return true;
    }

    private void cerrarVentana() {

        Stage ventana = (Stage) txtNombre.getScene().getWindow();
        ventana.close();
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

    // ============================================================
    // AGREGAR NUEVO PAÍS
    // ============================================================

    @FXML
    private void abrirFormularioPais() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_municipalidad/formulario-pais-view.fxml"));
            Parent root = loader.load();
            FormPaisController controller = loader.getController();

            // Abrimos el formulario en modo ALTA
            controller.setPaisEdicion(null);

            Stage ventana = new Stage();

            ventana.setTitle("Registrar país");
            ventana.setScene(new Scene(root));
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.showAndWait();

            // RECUPERAR EL PAÍS NUEVO:
            Pais paisNuevo = controller.getPaisGuardado();

            // ACTUALIZAR COMBOBOX:
            cargarPaises();

            // SELECCIONAR EL PAÍS NUEVO:

            if (paisNuevo != null) {

                for (Pais pais : cmbPais.getItems()) {

                    if (pais.getIdPais() == paisNuevo.getIdPais()) {

                        cmbPais.setValue(pais);

                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.mostrarError("No se pudo abrir el formulario para registrar el país.");
        }
    }

    @FXML
    private void cancelar() {

        cerrarVentana();
    }

}