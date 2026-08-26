package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.dao.ProvinciaDAO;
import com.example.sistema_municipalidad.dao.TuristaDAO;
import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.model.Turista;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class TuristasController {

    // Componentes de Filtrado y Búsqueda (JFoenix)
    @FXML private TextField txtBuscar;
    @FXML private JFXComboBox<Provincia> comboProcedencia;
    @FXML private JFXComboBox<Pais> comboPais;
    @FXML private JFXButton btnRegistrar;
    @FXML private ImageView imgLimpiarFiltros;
    @FXML private Label lblTotalTuristas;
    @FXML private Label lblRegistradosMes;
    @FXML private Label lblProcedenciasDistintas;
    @FXML private Label lblPaisesDistintos;
    @FXML private Label lblMesActual;
    @FXML private Label lblListaPaises;

    // Tabla Principal de Turistas (JavaFX Nativo)
    @FXML private TableView<Turista> tablaTuristas;
    @FXML private TableColumn<Turista, String> columnaNombre;
    @FXML private TableColumn<Turista, String> columnaApellido;
    @FXML private TableColumn<Turista, String> columnaDocumento;
    @FXML private TableColumn<Turista, String> columnaProcedencia;
    @FXML private TableColumn<Turista, String> columnaPais;
    @FXML private TableColumn<Turista, String> columnaTelefono;
    @FXML private TableColumn<Turista, String> columnaEmail;
    @FXML private TableColumn<Turista, Void> columnaAcciones;   // Void xq no mapea un dato de texto directo

    private final TuristaDAO turistaDAO = new TuristaDAO();
    private final PaisDAO paisDAO = new PaisDAO();
    private final ProvinciaDAO provinciaDAO = new ProvinciaDAO();

    private final Map<Integer, String> paises = new HashMap<>();
    private final Map<Integer, String> provincias = new HashMap<>();

    @FXML
    private void initialize() {
        cargarPaises();
        cargarProvincias();

        configurarColumnas();
        configurarColumnaAcciones();

        cargarTuristas();

        cargarFiltroPaises();
        cargarFiltroProcedencias();
        cargarEstadisticas();
        actualizarDashboardTuristas();
        cargarListaPaises();
        cargarMesActual();

        configurarBusqueda();
        configurarFiltros();
        configurarLimpiarFiltros();

    }

    private void cargarPaises() {

        List<Pais> listaPaises = paisDAO.listar();
        for (Pais pais : listaPaises) {
            paises.put(
                    pais.getIdPais(),
                    pais.getNombrePais()
            );
        }
    }

    private void cargarProvincias() {
        List<Provincia> listaProvincias =
                provinciaDAO.listarTodas();

        for (Provincia provincia : listaProvincias) {
            provincias.put(
                    provincia.getIdProvincia(),
                    provincia.getNombreProvincia()
            );
        }
    }

    private void configurarColumnas() {

        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        columnaDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        columnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // PROCEDENCIA:
        columnaProcedencia.setCellValueFactory(
                turista -> {
                    Integer idProvincia = turista.getValue().getIdProvincia();
                    String nombreProvincia = provincias.get(idProvincia);

                    return new javafx.beans.property.SimpleStringProperty(
                            nombreProvincia != null
                                    ? nombreProvincia
                                    : ""
                    );
                }
        );

        // PAÍS:
        columnaPais.setCellValueFactory(
                turista -> {
                    Integer idPais = turista.getValue().getIdPais();
                    String nombrePais = paises.get(idPais);

                    return new javafx.beans.property.SimpleStringProperty(
                            nombrePais != null
                                    ? nombrePais
                                    : ""
                    );
                }
        );
    }

    private void cargarTuristas() {
        List<Turista> lista = turistaDAO.listar();
        tablaTuristas.getItems().setAll(lista);
        actualizarTabla(lista);
    }
    private void actualizarDashboardTuristas() {

        cargarTuristas();
        cargarEstadisticas();
        cargarListaPaises();
    }


    //
    // Metodo para crear e inyectar los botones del menú Acciones:
    //

    private void configurarColumnaAcciones() {
        // Definimos una fábrica de celdas personalizada para la columna de acciones
        columnaAcciones.setCellFactory(param -> new javafx.scene.control.TableCell<>() {

            // Creamos los botones de forma nativa
            private final javafx.scene.control.Button btnVer = new javafx.scene.control.Button();
            private final javafx.scene.control.Button btnEditar = new javafx.scene.control.Button();
            private final javafx.scene.control.Button btnEliminar = new javafx.scene.control.Button();
            private final javafx.scene.layout.HBox contenedorBotonera = new javafx.scene.layout.HBox(btnVer, btnEditar, btnEliminar);

            {
                //Configuramos el contenedor HBox (Alineación y espacio entre botones)
                contenedorBotonera.setAlignment(javafx.geometry.Pos.CENTER);
                contenedorBotonera.setSpacing(5);

                // Aplicamos estilos CSS
//                btnVer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #1a73e8;");
//                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #1a73e8;");
//                btnEliminar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #d93025;");
                btnVer.getStyleClass().addAll("boton-accion", "boton-ver");
                btnEditar.getStyleClass().addAll("boton-accion", "boton-editar");
                btnEliminar.getStyleClass().addAll("boton-accion", "boton-eliminar");

                //Texto temporal para probar los botones
//                btnVer.setText("👁");
//                btnEditar.setText("✏");
//                btnEliminar.setText("🗑");
                btnVer.setGraphic(crearIcono("/icons/consulta.png", 20, 20));
                btnEditar.setGraphic(crearIcono("/icons/modificar2.png", 20, 20));
                btnEliminar.setGraphic(crearIcono("/icons/eliminar.png", 20, 20));

                //Programamos las acciones de los clics para cada botón
                btnVer.setOnAction(event -> {

                    Turista turistaSeleccionado = getTableView().getItems().get(getIndex());

                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource(
                                        "/com/example/sistema_municipalidad/consulta-turista-view.fxml"
                                )
                        );

                        Scene scene = new Scene(loader.load());
                        ConsultaTuristaController controller = loader.getController();

                        String nombreProvincia = provincias.get(turistaSeleccionado.getIdProvincia());
                        String nombrePais = paises.get(turistaSeleccionado.getIdPais());

                        controller.setTurista(turistaSeleccionado, nombreProvincia, nombrePais);

                        Stage ventana = new Stage();

                        ventana.setTitle("Consultar turista");
                        ventana.setScene(scene);
                        ventana.initModality(Modality.APPLICATION_MODAL);
                        ventana.showAndWait();

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error al abrir consulta-turista-view.fxml");
                    }
                });

                btnEditar.setOnAction(event -> {
                    Turista turistaSeleccionado = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource(
                                        "/com/example/sistema_municipalidad/formulario-turista-view.fxml"
                                )
                        );
                        Scene scene = new Scene(loader.load());

                        // Obtenemos el controller del formulario
                        FormTuristaController controller = loader.getController();

                        // Le pasamos el turista seleccionado
                        controller.setTurista(turistaSeleccionado);

                        Stage ventana = new Stage();

                        ventana.setTitle("Modificar turista");
                        ventana.setScene(scene);

                        // Ventana modal
                        ventana.initModality(Modality.APPLICATION_MODAL);
                        ventana.showAndWait();

                        // Actualizamos la tabla al cerrar
                        actualizarDashboardTuristas();

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error al abrir el formulario de modificación.");
                    }
                });

                btnEliminar.setOnAction(event -> {

                    Turista turistaSeleccionado = getTableView().getItems().get(getIndex());

                    // CONFIRMACIÓN:
                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

                    confirmacion.setTitle("Eliminar turista");
                    confirmacion.setHeaderText("¿Está seguro de eliminar este turista?");
                    confirmacion.setContentText(turistaSeleccionado.getNombre() + " " + turistaSeleccionado.getApellido());

                    ButtonType botonSi = new ButtonType("Eliminar");
                    ButtonType botonNo =
                            new ButtonType(
                                    "Cancelar",
                                    ButtonBar.ButtonData.CANCEL_CLOSE
                            );

                    confirmacion.getButtonTypes().setAll(botonSi, botonNo);

                    // MOSTRAR CONFIRMACIÓN:
                    Optional<ButtonType> resultado = confirmacion.showAndWait();

                    // COMPROBAR RESPUESTA:

                    if (resultado.isPresent() && resultado.get() == botonSi) {

                        boolean eliminado = turistaDAO.eliminar(turistaSeleccionado.getIdTurista());
                        if (eliminado) {
                            Alert informacion = new Alert(Alert.AlertType.INFORMATION);

                            informacion.setTitle("Eliminación exitosa");
                            informacion.setHeaderText(null);
                            informacion.setContentText("El turista fue eliminado correctamente.");
                            informacion.showAndWait();
                            actualizarDashboardTuristas(); // Actualizar la tabla

                        } else {
                            Alert error = new Alert(Alert.AlertType.ERROR);

                            error.setTitle("Error");
                            error.setHeaderText(null);
                            error.setContentText("No se pudo eliminar el turista.");
                            error.showAndWait();
                        }
                    }
                });
            }

            // Este método dibuja físicamente los botones en la fila si esta tiene datos.
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(contenedorBotonera);
                }
            }
        });
    }

    private void configurarBusqueda() {

        txtBuscar.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    aplicarFiltros();
                }
        );
    }

    private void cargarFiltroPaises() {
        List<Pais> listaPaises = paisDAO.listar();

        comboPais.getItems().clear();
        comboPais.getItems().addAll(listaPaises);
        comboPais.setPromptText("Todos");
    }

    private void cargarFiltroProcedencias() {
        comboProcedencia.getItems().clear();
        comboProcedencia.setValue(null);
        comboProcedencia.setPromptText("Todas");
        comboProcedencia.setDisable(true);
    }

    private void cargarEstadisticas() {
        lblTotalTuristas.setText(String.valueOf(turistaDAO.contarTuristasActivos()));
        lblRegistradosMes.setText(String.valueOf(turistaDAO.contarRegistradosEsteMes()));
        lblProcedenciasDistintas.setText(String.valueOf(turistaDAO.contarProcedenciasDistintas()));
        lblPaisesDistintos.setText(String.valueOf(turistaDAO.contarPaisesDistintos()));
    }

    private void cargarListaPaises() {

        List<String> paises = turistaDAO.listarPaisesConTuristas();

        if (paises.isEmpty()) {
            lblListaPaises.setText("Sin registros");
            return;
        }
        lblListaPaises.setText(String.join(", ", paises));
    }

    private void configurarFiltros() {

        comboPais.setOnAction(event -> {

            Pais paisSeleccionado = comboPais.getValue();
            comboProcedencia.getItems().clear();
            comboProcedencia.setValue(null);

            if (paisSeleccionado == null) {
                comboProcedencia.setDisable(true);
                comboProcedencia.setPromptText("Todas");
            } else {
                List<Provincia> provincias = provinciaDAO.listarPorPais(paisSeleccionado.getIdPais());
                comboProcedencia.getItems().addAll(provincias);
                comboProcedencia.setDisable(provincias.isEmpty());
                comboProcedencia.setPromptText("Todas");
            }

            aplicarFiltros();
        });

        comboProcedencia.setOnAction(event -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {

        String criterio = txtBuscar.getText().trim();
        Pais paisSeleccionado = comboPais.getValue();
        Provincia provinciaSeleccionada = comboProcedencia.getValue();

        List<Turista> turistas;

        // BÚSQUEDA:
        if (criterio.isEmpty()) {
            turistas = turistaDAO.listar();
        } else {
            turistas = turistaDAO.buscar(criterio);
        }


        // FILTRO POR PAÍS:
        if (paisSeleccionado != null) {

            turistas.removeIf(turista -> paisSeleccionado.getIdPais() != turista.getIdPais());
        }

        // FILTRO POR PROCEDENCIA:
        if (provinciaSeleccionada != null) {

            turistas.removeIf(turista -> provinciaSeleccionada.getIdProvincia() != turista.getIdProvincia());
        }

        actualizarTabla(turistas);
    }


    @FXML
    private void abrirFormularioTurista() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/sistema_municipalidad/formulario-turista-view.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());
            Stage ventana = new Stage();

            //ventana.initStyle(StageStyle.UNDECORATED); //coloca la ventana sin bordes
            ventana.setTitle("Registrar turista");
            ventana.setScene(scene);

            // Hace que sea una ventana modal
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.showAndWait();

            // Cuando se cierre el formulario,
            // volvemos a cargar la tabla por si se registró un turista nuevo.
            actualizarDashboardTuristas();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir formulario-turista-view.fxml");
        }
    }

    private ImageView crearIcono(String ruta, double ancho, double alto) {
        ImageView icono = new ImageView(new Image(getClass().getResourceAsStream(ruta)));
        icono.setFitWidth(ancho);
        icono.setFitHeight(alto);
        icono.setPreserveRatio(true);
        return icono;
    }

    private void actualizarTabla(List<Turista> lista) {

        tablaTuristas.getItems().setAll(lista);
    }


    private void configurarLimpiarFiltros() {

        imgLimpiarFiltros.setOnMouseClicked(event -> {

            // Limpiar búsqueda
            txtBuscar.clear();

            // Limpiar país
            comboPais.setValue(null);

            // Limpiar procedencia
            comboProcedencia.getItems().clear();
            comboProcedencia.setValue(null);
            comboProcedencia.setDisable(true);
            comboProcedencia.setPromptText("Todas");

            // Mostrar nuevamente todos
            cargarTuristas();
        });
    }

    private void cargarMesActual() {

        String[] meses = {
                "enero",
                "febrero",
                "marzo",
                "abril",
                "mayo",
                "junio",
                "julio",
                "agosto",
                "septiembre",
                "octubre",
                "noviembre",
                "diciembre"
        };

        LocalDate fechaActual = LocalDate.now();
        String nombreMes = meses[fechaActual.getMonthValue() - 1];
        int anio = fechaActual.getYear();
        lblMesActual.setText("Durante " + nombreMes + " " + anio);
    }

}
