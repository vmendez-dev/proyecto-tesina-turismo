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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Optional;

public class TuristasController {

    // Componentes de Filtrado y Búsqueda (JFoenix)
    @FXML private JFXTextField txtBuscar;
    @FXML private JFXComboBox<String> comboProcedencia;
    @FXML private JFXComboBox<String> comboPais;
    @FXML private JFXButton btnRegistrar;

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
        cargarTuristas();
        configurarColumnaAcciones();
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
        System.out.println("Cantidad de turistas encontrados: " + lista.size());
        tablaTuristas.getItems().setAll(lista);
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
                // 1. Configuramos el contenedor HBox (Alineación y espacio entre botones)
                contenedorBotonera.setAlignment(javafx.geometry.Pos.CENTER);
                contenedorBotonera.setSpacing(10);

                // 2. Aplicamos estilos CSS nativos para que queden redondos y limpios como tu imagen
                btnVer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #1a73e8;");
                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #1a73e8;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #d93025;");

                // 3. Colocamos los textos temporales (puedes reemplazarlos por imágenes/íconos más adelante)
                btnVer.setText("👁");
                btnEditar.setText("✏");
                btnEliminar.setText("🗑");

                // 4. Programamos las acciones de los clics para cada botón
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
                        cargarTuristas();

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
                            cargarTuristas(); // Actualizar la tabla

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
            cargarTuristas();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir formulario-turista-view.fxml");
        }
    }


}
