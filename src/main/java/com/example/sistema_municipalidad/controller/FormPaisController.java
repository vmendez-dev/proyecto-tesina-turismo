package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.helper.AlertHelper;
import com.example.sistema_municipalidad.helper.ValidacionHelper;
import com.example.sistema_municipalidad.model.Pais;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import javax.xml.validation.Validator;

public class FormPaisController {

    @FXML private TextField txtNombrePais;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTitulo;

    private final PaisDAO paisDAO = new PaisDAO();

    private Pais paisEdicion;
    private Pais paisGuardado;

    public void setPaisEdicion(Pais pais) {

        this.paisEdicion = pais;

        if (pais != null) {

            // Modo modificación
            txtNombrePais.setText(pais.getNombrePais());
            lblTitulo.setText("Modificar país");
            btnGuardar.setText("Guardar cambios");

        } else {

            // Modo registro
            lblTitulo.setText("Registrar país");
            btnGuardar.setText("Guardar");
        }
    }

    public Pais getPaisGuardado() {

        return paisGuardado;
    }

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    @FXML
    public void initialize() {

        ValidacionHelper.permitirSoloLetrasYLimitarLongitud(txtNombrePais, 50);
        txtNombrePais.requestFocus();
    }


    // ============================================================
    // GUARDAR PAÍS
    // ============================================================

    @FXML
    private void guardar() {

        String nombrePais = txtNombrePais.getText();

        // --------------------------------------------------------
        // VALIDAR CAMPO VACÍO
        // --------------------------------------------------------

        if (nombrePais == null || nombrePais.trim().isEmpty()) {

            AlertHelper.mostrarError(
                    "Debe ingresar el nombre del país."
            );

            txtNombrePais.requestFocus();

            return;
        }


        // --------------------------------------------------------
        // LIMPIAR ESPACIOS
        // --------------------------------------------------------

        nombrePais = nombrePais.trim();


        // --------------------------------------------------------
        // VALIDAR LONGITUD
        // --------------------------------------------------------

        if (nombrePais.length() < 2) {

            AlertHelper.mostrarError(
                    "El nombre del país debe tener al menos 2 caracteres."
            );

            txtNombrePais.requestFocus();

            return;
        }


        // --------------------------------------------------------
        // VALIDAR ARGENTINA
        // --------------------------------------------------------

        if (nombrePais.equalsIgnoreCase("Argentina")) {

            AlertHelper.mostrarError(
                    "Argentina ya está registrada y no es necesario agregarla nuevamente."
            );

            txtNombrePais.requestFocus();

            return;
        }


        // --------------------------------------------------------
        // VALIDAR DUPLICADO
        // --------------------------------------------------------

        if (paisDAO.existeNombre(nombrePais)) {

            AlertHelper.mostrarError(
                    "El país '" + nombrePais + "' ya está registrado."
            );

            txtNombrePais.requestFocus();

            return;
        }

        // --------------------------------------------------------
        // ALTA
        // --------------------------------------------------------

        if (paisEdicion == null) {

            Pais nuevoPais = new Pais();

            nuevoPais.setNombrePais(nombrePais);

            boolean guardado = paisDAO.guardar(nuevoPais);

            if (guardado) {
                paisGuardado = nuevoPais;

                AlertHelper.mostrarInformacion(
                        "País registrado",
                        "El país '" + nombrePais
                                + "' se registró correctamente."
                );

                cerrarVentana();

            } else {

                AlertHelper.mostrarError(
                        "No se pudo registrar el país."
                );
            }

            return;
        }


    // --------------------------------------------------------
    // MODIFICACIÓN
    // --------------------------------------------------------

        if (paisEdicion.getNombrePais()
                .trim()
                .equalsIgnoreCase("Argentina")) {

            AlertHelper.mostrarError(
                    "Argentina no puede ser modificada."
            );

            return;
        }


    // --------------------------------------------------------
    // VERIFICAR DUPLICADO AL MODIFICAR
    // --------------------------------------------------------

        if (paisDAO.existeNombreExceptoId(
                nombrePais,
                paisEdicion.getIdPais()
        )) {

            AlertHelper.mostrarError(
                    "Ya existe otro país registrado con el nombre '"
                            + nombrePais
                            + "'."
            );

            txtNombrePais.requestFocus();

            return;
        }



    // --------------------------------------------------------
    // ACTUALIZAR OBJETO
    // --------------------------------------------------------

        paisEdicion.setNombrePais(nombrePais);


    // --------------------------------------------------------
    // MODIFICAR EN BD
    // --------------------------------------------------------

        boolean modificado =
                paisDAO.modificar(paisEdicion);


        if (modificado) {

            AlertHelper.mostrarInformacion(
                    "País modificado",
                    "El país fue modificado correctamente."
            );

            cerrarVentana();

        } else {

            AlertHelper.mostrarError(
                    "No se pudo modificar el país."
            );
        }

    }
    // ============================================================
    // CANCELAR
    // ============================================================

    @FXML
    private void cancelar() {

        cerrarVentana();
    }


    // ============================================================
    // CERRAR VENTANA
    // ============================================================

    private void cerrarVentana() {

        Stage stage = (Stage) txtNombrePais.getScene().getWindow();

        stage.close();
    }
}