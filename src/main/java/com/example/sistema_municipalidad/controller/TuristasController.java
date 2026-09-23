package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.helper.AlertHelper;
import com.example.sistema_municipalidad.dao.PaisDAO;
import com.example.sistema_municipalidad.dao.ProvinciaDAO;
import com.example.sistema_municipalidad.dao.TuristaDAO;
import com.example.sistema_municipalidad.helper.TooltipHelper;
import com.example.sistema_municipalidad.model.Pais;
import com.example.sistema_municipalidad.model.Provincia;
import com.example.sistema_municipalidad.model.Turista;
import com.example.sistema_municipalidad.helper.ValidacionHelper;
import com.example.sistema_municipalidad.helper.ImageHelper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TuristasController {

    // Componentes de Filtrado y Búsqueda (JFoenix)
    @FXML private TextField txtBuscar;
    @FXML private JFXComboBox<Provincia> comboProcedencia;
    @FXML private JFXComboBox<Pais> comboPais;
    @FXML private JFXComboBox<String> comboEstado;
    @FXML private JFXButton btnRegistrar;
    @FXML private Button btnLimpiarFiltros;
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

    // Tabla últimos turistas registrados
    @FXML private TableView<Turista> tablaUltimosTuristas;
    @FXML private TableColumn<Turista, String> colUltimoNombre;
    @FXML private TableColumn<Turista, String> colUltimoApellido;
    @FXML private TableColumn<Turista, String> colUltimoDocumento;
    @FXML private TableColumn<Turista, String> colUltimaProcedencia;
    @FXML private TableColumn<Turista, String> colUltimoPais;
    @FXML private TableColumn<Turista, String> colUltimaFecha;
    @FXML private TableColumn<Turista, Void> colUltimasAcciones;

    private final TuristaDAO turistaDAO = new TuristaDAO();
    private final PaisDAO paisDAO = new PaisDAO();
    private final ProvinciaDAO provinciaDAO = new ProvinciaDAO();

    private final Map<Integer, String> paises = new HashMap<>();
    private final Map<Integer, String> provincias = new HashMap<>();

    // Paginación
    @FXML private Pagination paginador;
    @FXML private Label lblMostrando;
    private final int filasPorPagina = 5;
    private List<Turista> listaActualTuristas = new ArrayList<>(); // Guarda la lista filtrada completa

    @FXML
    private void initialize() {
        cargarPaises();
        cargarProvincias();

        configurarColumnas();
        configurarColumnaAcciones();

        cargarTuristas();

        cargarFiltroPaises();
        cargarFiltroProcedencias();
        cargarFiltroEstado();
        cargarEstadisticas();
        actualizarDashboardTuristas();
        cargarListaPaises();
        cargarMesActual();
        cargarUltimosTuristas();

        configurarBusqueda();
        configurarFiltros();
        configurarLimpiarFiltros();
        configurarAccionesUltimosTuristas();

        ValidacionHelper.limitarLongitud(txtBuscar, 50);

        // NUEVO: Escuchar cambios en la página
        if (paginador != null) {
            paginador.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                mostrarPagina(newIndex.intValue());
            });
        }

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

        //Primera tabla:
        columnaNombre.setCellValueFactory(
                turista -> new SimpleStringProperty(
                        mostrarGuionSiVacio(
                                turista.getValue().getNombre()
                        )
                )
        );
        columnaApellido.setCellValueFactory(
                turista -> new SimpleStringProperty(
                        mostrarGuionSiVacio(
                                turista.getValue().getApellido()
                        )
                )
        );
        columnaDocumento.setCellValueFactory(
                turista -> new SimpleStringProperty(
                        mostrarGuionSiVacio(
                                turista.getValue().getNumeroDocumento()
                        )
                )
        );
        columnaTelefono.setCellValueFactory(
                turista -> new SimpleStringProperty(
                        mostrarGuionSiVacio(
                                turista.getValue().getTelefono()
                        )
                )
        );
        columnaEmail.setCellValueFactory(
                turista -> new SimpleStringProperty(
                        mostrarGuionSiVacio(
                                turista.getValue().getEmail()
                        )
                )
        );

        //Segunda tabla (últimos turistas):
        colUltimoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUltimoApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colUltimoDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));

        // PROCEDENCIA:
        columnaProcedencia.setCellValueFactory(
                turista -> {
                    Integer idProvincia = turista.getValue().getIdProvincia();
                    String nombreProvincia = provincias.get(idProvincia);

                    return new SimpleStringProperty(mostrarGuionSiVacio(nombreProvincia));
                }
        );
        colUltimaProcedencia.setCellValueFactory(
                turista -> {
                    Integer idProvincia = turista.getValue().getIdProvincia();
                    String nombreProvincia = provincias.get(idProvincia);

                    return new SimpleStringProperty(
                            nombreProvincia != null
                                    ? nombreProvincia
                                    : "-"
                    );
                }
        );

        // PAÍS:
        columnaPais.setCellValueFactory(
                turista -> {
                    Integer idPais = turista.getValue().getIdPais();
                    String nombrePais = paises.get(idPais);

                    return new SimpleStringProperty(mostrarGuionSiVacio(nombrePais));
                }
        );
        colUltimoPais.setCellValueFactory(
                turista -> {
                    Integer idPais = turista.getValue().getIdPais();
                    String nombrePais = paises.get(idPais);

                    return new SimpleStringProperty(
                            nombrePais != null
                                    ? nombrePais
                                    : "-"
                    );
                }
        );

        //FECHA:
        colUltimaFecha.setCellValueFactory(
                turista -> {
                    LocalDate fecha = turista.getValue().getFechaRegistro();

                    String textoFecha =
                            fecha != null
                                    ? fecha.format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                    )
                            )
                                    : "-";

                    return new SimpleStringProperty(textoFecha);
                }
        );
    }

    private String mostrarGuionSiVacio(String valor) {

        if (valor == null || valor.trim().isEmpty()) {
            return "-";
        }

        return valor;
    }

    private void cargarTuristas() {
        List<Turista> lista = turistaDAO.listar();
        tablaTuristas.getItems().setAll(lista);
        actualizarTabla(lista);
    }

    private void cargarUltimosTuristas() {
        List<Turista> turistas = turistaDAO.listarUltimosRegistrados(5);
        tablaUltimosTuristas.getItems().setAll(turistas);
    }

    private void actualizarDashboardTuristas() {

        aplicarFiltros();
        cargarEstadisticas();
        cargarListaPaises();
        cargarUltimosTuristas();
    }


    //
    // Metodo para crear e inyectar los botones del menú Acciones:
    //

    private void configurarColumnaAcciones() {

        columnaAcciones.setCellFactory(param ->
                new TableCell<Turista, Void>() {

                    private final Button btnVer = new Button();
                    private final Button btnEditar = new Button();
                    private final Button btnEliminar = new Button();
                    private final Button btnReactivar = new Button();

                    private final HBox contenedorBotonera =
                            new HBox(
                                    btnVer,
                                    btnEditar,
                                    btnEliminar,
                                    btnReactivar
                            );

                    {
                        // ==========================================
                        // CONFIGURACIÓN DEL CONTENEDOR
                        // ==========================================

                        contenedorBotonera.setAlignment(Pos.CENTER);
                        contenedorBotonera.setSpacing(5);


                        // ==========================================
                        // ESTILOS
                        // ==========================================

                        btnVer.getStyleClass().addAll("boton-accion", "boton-ver");
                        btnEditar.getStyleClass().addAll("boton-accion", "boton-editar");
                        btnEliminar.getStyleClass().addAll("boton-accion", "boton-eliminar");
                        btnReactivar.getStyleClass().addAll("boton-accion", "boton-reactivar");

                        // ==========================================
                        // AGREGAR TOOLTIPS RÁPIDOS
                        // ==========================================

                        TooltipHelper.registrarTooltipRapido(btnVer, "Ver detalles", 600);
                        TooltipHelper.registrarTooltipRapido(btnEditar, "Modificar turista", 600);
                        TooltipHelper.registrarTooltipRapido(btnEliminar, "Eliminar turista", 600);
                        TooltipHelper.registrarTooltipRapido(btnReactivar, "Reactivar turista", 600);

                        // ==========================================
                        // ICONOS
                        // ==========================================

                        btnVer.setGraphic(
                                ImageHelper.crearIcono(
                                        "/icons/consulta.png",
                                        20,
                                        20
                                )
                        );

                        btnEditar.setGraphic(
                                ImageHelper.crearIcono(
                                        "/icons/modificar2.png",
                                        20,
                                        20
                                )
                        );

                        btnEliminar.setGraphic(
                                ImageHelper.crearIcono(
                                        "/icons/eliminar.png",
                                        20,
                                        20
                                )
                        );

                        btnReactivar.setGraphic(
                                ImageHelper.crearIcono(
                                        "/icons/reactivar.png",
                                        20,
                                        20
                                )
                        );


                        // ==========================================
                        // VER
                        // ==========================================

                        btnVer.setOnAction(event -> {

                            Turista turistaSeleccionado =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            abrirConsultaTurista(
                                    turistaSeleccionado
                            );
                        });


                        // ==========================================
                        // EDITAR
                        // ==========================================

                        btnEditar.setOnAction(event -> {

                            Turista turistaSeleccionado =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            try {

                                FXMLLoader loader =
                                        new FXMLLoader(
                                                getClass().getResource(
                                                        "/com/example/sistema_municipalidad/formulario-turista-view.fxml"
                                                )
                                        );

                                Scene scene =
                                        new Scene(loader.load());

                                FormTuristaController controller =
                                        loader.getController();

                                controller.setTurista(
                                        turistaSeleccionado
                                );

                                Stage ventana =
                                        new Stage();

                                ventana.setTitle(
                                        "Modificar turista"
                                );

                                ventana.setScene(scene);

                                ventana.initModality(
                                        Modality.APPLICATION_MODAL
                                );

                                ventana.showAndWait();

                                actualizarDashboardTuristas();

                            } catch (IOException e) {

                                e.printStackTrace();

                                System.out.println(
                                        "Error al abrir el formulario de modificación."
                                );
                            }
                        });


                        // ==========================================
                        // ELIMINAR
                        // ==========================================

                        btnEliminar.setOnAction(event -> {

                            Turista turistaSeleccionado =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            boolean confirmar =
                                    AlertHelper.mostrarConfirmacion(
                                            "Eliminar turista",
                                            "¿Está seguro de eliminar este turista?\n\n"
                                                    + turistaSeleccionado.getNombre()
                                                    + " "
                                                    + turistaSeleccionado.getApellido()
                                    );


                            if (confirmar) {

                                boolean eliminado =
                                        turistaDAO.eliminar(
                                                turistaSeleccionado
                                                        .getIdTurista()
                                        );


                                if (eliminado) {

                                    AlertHelper.mostrarInformacion(
                                            "Eliminación exitosa",
                                            "El turista fue eliminado correctamente."
                                    );

                                    actualizarDashboardTuristas();

                                } else {

                                    AlertHelper.mostrarError(
                                            "No se pudo eliminar el turista."
                                    );
                                }
                            }
                        });


                        // ==========================================
                        // REACTIVAR
                        // ==========================================

                        btnReactivar.setOnAction(event -> {

                            Turista turistaSeleccionado =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            boolean confirmar =
                                    AlertHelper.mostrarConfirmacion(
                                            "Reactivar turista",
                                            "¿Está seguro de reactivar este turista?\n\n"
                                                    + turistaSeleccionado.getNombre()
                                                    + " "
                                                    + turistaSeleccionado.getApellido()
                                    );


                            if (confirmar) {

                                boolean reactivado =
                                        turistaDAO.reactivar(
                                                turistaSeleccionado
                                                        .getIdTurista()
                                        );


                                if (reactivado) {

                                    AlertHelper.mostrarInformacion(
                                            "Reactivación exitosa",
                                            "El turista fue reactivado correctamente."
                                    );

                                    actualizarDashboardTuristas();

                                } else {

                                    AlertHelper.mostrarError(
                                            "No se pudo reactivar el turista."
                                    );
                                }
                            }
                        });
                    }

                    // ==========================================
                    // MOSTRAR BOTONES SEGÚN ESTADO
                    // ==========================================

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                        } else {

                            Turista turista =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            if (turista.isActivo()) {

                                // TURISTA ACTIVO
                                contenedorBotonera.getChildren().setAll(
                                                btnVer,
                                                btnEditar,
                                                btnEliminar
                                        );

                            } else {

                                // TURISTA INACTIVO
                                contenedorBotonera.getChildren().setAll(btnReactivar);
                            }

                            setGraphic(contenedorBotonera);
                        }
                    }
                }
        );
    }

    private void configurarAccionesUltimosTuristas() {

        colUltimasAcciones.setCellFactory(param ->
                new TableCell<>() {
                    private final Button btnVer = new Button();
                    {
                        btnVer.setGraphic(ImageHelper.crearIcono("/icons/consulta.png", 20, 20));
                        btnVer.setPrefSize(40, 40);
                        btnVer.getStyleClass().addAll("boton-accion", "boton-ver");
                        btnVer.setOnAction(event -> {
                            Turista turista = getTableView().getItems().get(getIndex());
                            abrirConsultaTurista(turista);
                        });
                        TooltipHelper.registrarTooltipRapido(btnVer, "Ver detalles", 600);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnVer);
                        }
                    }
                }
        );
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

    private void cargarFiltroEstado() {
        comboEstado.getItems().clear();
        comboEstado.getItems().addAll("Todos", "Activos", "Inactivos");
        comboEstado.setValue("Activos"); // Valor por defecto
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
        comboEstado.setOnAction(event -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        String criterio = txtBuscar.getText().trim();
        Pais paisSeleccionado = comboPais.getValue();
        Provincia provinciaSeleccionada = comboProcedencia.getValue();
        String estadoSeleccionado = comboEstado.getValue();

        List<Turista> turistas;

        // FILTRO POR ESTADO + BÚSQUEDA:

        if ("Inactivos".equals(estadoSeleccionado)) {

            turistas = turistaDAO.listarInactivos();

        } else if ("Todos".equals(estadoSeleccionado)) {

            turistas = turistaDAO.listarTodos();

        } else {

            // Activos
            turistas = turistaDAO.listar();
        }


        // BÚSQUEDA:

        if (!criterio.isEmpty()) {

            String criterioNormalizado = criterio.toLowerCase();

            turistas.removeIf(turista ->
                    !turista.getNombre()
                            .toLowerCase()
                            .contains(criterioNormalizado)

                            &&

                            !turista.getApellido()
                                    .toLowerCase()
                                    .contains(criterioNormalizado)

                            &&

                            !turista.getNumeroDocumento()
                                    .toLowerCase()
                                    .contains(criterioNormalizado)

                            &&

                            (turista.getEmail() == null
                                    ||
                                    !turista.getEmail()
                                            .toLowerCase()
                                            .contains(criterioNormalizado))
            );
        }

        // FILTRO POR PAÍS:

        if (paisSeleccionado != null) {

            turistas.removeIf(turista ->
                            paisSeleccionado.getIdPais()
                                    != turista.getIdPais()
            );
        }

        // FILTRO POR PROCEDENCIA:

        if (provinciaSeleccionada != null) {

            turistas.removeIf(
                    turista ->
                            provinciaSeleccionada.getIdProvincia()
                                    != turista.getIdProvincia()
            );
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

    private void abrirConsultaTurista(Turista turistaSeleccionado) {

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
        }
    }


    private void actualizarTabla(List<Turista> lista) {
        // Guardamos la lista completa (ya sea todos, o los filtrados)
        this.listaActualTuristas = lista;

        if (paginador != null) {
            // Calcular páginas necesarias
            int totalTuristas = lista.size();
            int totalPaginas = (int) Math.ceil((double) totalTuristas / filasPorPagina);

            paginador.setPageCount(totalPaginas == 0 ? 1 : totalPaginas);
            paginador.setCurrentPageIndex(0); // Forzar a ir a la primera página
        }

        mostrarPagina(0); // Mostrar los primeros 5
    }

    private void mostrarPagina(int indicePagina) {
        if (listaActualTuristas == null || listaActualTuristas.isEmpty()) {
            tablaTuristas.getItems().clear();
            if (lblMostrando != null) lblMostrando.setText("Mostrando 0 turistas");
            return;
        }

        // Calcular desde qué registro hasta qué registro cortar la lista
        int desde = indicePagina * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, listaActualTuristas.size());

        // Extraer los 5 de esta página y mostrarlos
        List<Turista> subLista = listaActualTuristas.subList(desde, hasta);
        tablaTuristas.getItems().setAll(subLista);

        // Actualizar el texto del Label
        if (lblMostrando != null) {
            lblMostrando.setText(String.format("Mostrando %d a %d de %d turistas",
                    (desde + 1), hasta, listaActualTuristas.size()));
        }
    }

    private void configurarLimpiarFiltros() {
        TooltipHelper.registrarTooltipRapido(btnLimpiarFiltros, "Limpiar filtros");

        btnLimpiarFiltros.setOnAction(event -> {

            // Limpiar búsqueda:
            txtBuscar.clear();

            // Limpiar país:
            comboPais.setValue(null);

            // Limpiar procedencia:
            comboProcedencia.getItems().clear();
            comboProcedencia.setValue(null);
            comboProcedencia.setDisable(true);
            comboProcedencia.setPromptText("Todas");

            // Restaurar estado:
            comboEstado.setValue("Activos");

            // Actualizar tabla:
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

    @FXML
    private void abrirGestionPaises() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/sistema_municipalidad/paises-view.fxml"
                    )
            );

            Parent root = loader.load();

            Stage ventana = new Stage();

            ventana.setTitle("Gestión de países");

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.showAndWait();

            // Al cerrar Gestión de Países,
            // actualizamos la información del módulo.
            cargarPaises();
            cargarFiltroPaises();
            cargarFiltroProcedencias();
            actualizarDashboardTuristas();

        } catch (IOException e) {

            e.printStackTrace();
            AlertHelper.mostrarError("No se pudo abrir la gestión de países.");
        }
    }

}
