package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.helper.AlertHelper;
import com.example.sistema_municipalidad.helper.ValidacionHelper;
import com.example.sistema_municipalidad.model.Pais;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PaisesController {

    @FXML private TableView<Pais> tablaPaises;
    @FXML private TableColumn<Pais, Integer> colId;
    @FXML private TableColumn<Pais, String> colNombre;
    @FXML private TextField txtBuscar;
    @FXML private Button btnNuevo;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private final PaisDAO paisDAO = new PaisDAO();
    private final ObservableList<Pais> listaPaises = FXCollections.observableArrayList();

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    @FXML
    public void initialize() {

        configurarTabla();
        cargarPaises();
        configurarBusqueda();
        ValidacionHelper.permitirSoloLetrasYLimitarLongitud(txtBuscar, 50);
    }


    // ============================================================
    // CONFIGURAR TABLA
    // ============================================================

    private void configurarTabla() {

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombrePais"));
        tablaPaises.setItems(listaPaises);
    }


    // ============================================================
    // CARGAR PAÍSES
    // ============================================================

    private void cargarPaises() {

        List<Pais> paises = paisDAO.listar();

        listaPaises.setAll(paises);
    }


    // ============================================================
    // BUSCAR PAÍSES
    // ============================================================

    private void configurarBusqueda() {

        txtBuscar.textProperty().addListener(
                (observable, valorAnterior, valorNuevo) -> {

                    String texto = valorNuevo.trim().toLowerCase();

                    if (texto.isEmpty()) {

                        listaPaises.setAll(
                                paisDAO.listar()
                        );

                        return;
                    }

                    List<Pais> resultados =
                            paisDAO.listar()
                                    .stream()
                                    .filter(pais ->
                                            pais.getNombrePais()
                                                    .toLowerCase()
                                                    .contains(texto)
                                    )
                                    .toList();

                    listaPaises.setAll(resultados);
                }
        );
    }


    // ============================================================
    // NUEVO PAÍS
    // ============================================================

    @FXML
    private void nuevoPais() {

        abrirFormularioPais(null);
    }


    // ============================================================
    // MODIFICAR PAÍS
    // ============================================================

    @FXML
    private void modificarPais() {

        Pais paisSeleccionado =
                tablaPaises.getSelectionModel().getSelectedItem();

        if (paisSeleccionado == null) {

            AlertHelper.mostrarError(
                    "Debe seleccionar un país para modificarlo."
            );

            return;
        }

        abrirFormularioPais(paisSeleccionado);
    }


    // ============================================================
    // ELIMINAR PAÍS
    // ============================================================

    @FXML
    private void eliminarPais() {

        Pais paisSeleccionado =
                tablaPaises.getSelectionModel().getSelectedItem();

        if (paisSeleccionado == null) {

            AlertHelper.mostrarError(
                    "Debe seleccionar un país para eliminarlo."
            );

            return;
        }


        // --------------------------------------------------------
        // ARGENTINA
        // --------------------------------------------------------

        if (paisSeleccionado.getNombrePais()
                .trim()
                .equalsIgnoreCase("Argentina")) {

            AlertHelper.mostrarError(
                    "No se puede eliminar Argentina porque es un país obligatorio del sistema."
            );

            return;
        }


        // --------------------------------------------------------
        // TURISTAS ASOCIADOS
        // --------------------------------------------------------

        if (paisDAO.tieneTuristas(
                paisSeleccionado.getIdPais())) {

            AlertHelper.mostrarError(
                    "No se puede eliminar el país '"
                            + paisSeleccionado.getNombrePais()
                            + "' porque existen turistas asociados."
            );

            return;
        }


        // --------------------------------------------------------
        // PROVINCIAS ASOCIADAS
        // --------------------------------------------------------

        if (paisDAO.tieneProvincias(
                paisSeleccionado.getIdPais())) {

            AlertHelper.mostrarError(
                    "No se puede eliminar el país '"
                            + paisSeleccionado.getNombrePais()
                            + "' porque existen provincias asociadas."
            );

            return;
        }


        // --------------------------------------------------------
        // CONFIRMACIÓN
        // --------------------------------------------------------

        boolean confirmar =
                AlertHelper.mostrarConfirmacion(
                        "Eliminar país",
                        "¿Está seguro de que desea eliminar el país '"
                                + paisSeleccionado.getNombrePais()
                                + "'?"
                );

        if (!confirmar) {
            return;
        }


        // --------------------------------------------------------
        // ELIMINAR
        // --------------------------------------------------------

        boolean eliminado =
                paisDAO.eliminar(paisSeleccionado);

        if (eliminado) {

            AlertHelper.mostrarInformacion(
                    "País eliminado",
                    "El país '"
                            + paisSeleccionado.getNombrePais()
                            + "' fue eliminado correctamente."
            );

            cargarPaises();

        } else {

            AlertHelper.mostrarError(
                    "No se pudo eliminar el país."
            );
        }
    }


    // ============================================================
    // ABRIR FORMULARIO
    // ============================================================

    private void abrirFormularioPais(Pais pais) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/sistema_municipalidad/formulario-pais-view.fxml"
                    )
            );

            Parent root = loader.load();

            FormPaisController controller =
                    loader.getController();

            /*
             * Más adelante utilizaremos este parámetro
             * para diferenciar Alta y Modificación.
             */
            controller.setPaisEdicion(pais);

            Stage ventana = new Stage();

            ventana.setTitle(
                    pais == null
                            ? "Registrar país"
                            : "Modificar país"
            );

            ventana.initModality(Modality.APPLICATION_MODAL);

            ventana.setScene(
                    new Scene(root)
            );

            ventana.showAndWait();

            cargarPaises();

        } catch (IOException e) {

            e.printStackTrace();

            AlertHelper.mostrarError(
                    "No se pudo abrir el formulario de país."
            );
        }
    }
}