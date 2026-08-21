package com.example.sistema_municipalidad.controller;

import com.example.sistema_municipalidad.model.Turista;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class TuristasController implements Initializable {

    // 1. Componentes de Filtrado y Búsqueda (JFoenix)
    @FXML private JFXTextField txtBuscar;
    @FXML private JFXComboBox<String> comboProcedencia;
    @FXML private JFXComboBox<String> comboPais;
    @FXML private JFXButton btnRegistrar;

    // 2. Tabla Principal de Turistas (JavaFX Nativo)
    // Nota: "Turista" será tu clase modelo dentro del paquete 'model'
    @FXML private TableView<Turista> tablaTuristas; // Cambia 'Object' por 'Turista' cuando crees el modelo
    @FXML private TableColumn<Turista, String> colNombre;
    @FXML private TableColumn<Turista, String> colApellido;
    @FXML private TableColumn<Turista, String> colDni;
    @FXML private TableColumn<Turista, String> colPais;

    /**
     * Este método se ejecuta automáticamente cuando la pantalla de Turistas se carga en el fondo blanco.
     * Ideal para cargar datos de la base de datos, inicializar tablas o rellenar ComboBoxes.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabla();
        cargarFiltros();
        listarTuristas();
    }

    // 3. Métodos de Inicialización Internos
    private void configurarTabla() {
        // Aquí le dirás a cada columna qué dato del modelo debe mostrar en un futuro
        System.out.println("Tabla configurada con éxito.");
    }

    private void cargarFiltros() {
        // Ejemplo para rellenar los desplegables de JFoenix
        if (comboPais != null) {
            comboPais.getItems().addAll("Argentina", "Brasil", "Chile", "Uruguay", "Otro");
        }
    }

    private void listarTuristas() {
        // Aquí llamarás a tu paquete 'dao' para traer los turistas de la Base de Datos
        System.out.println("Buscando turistas en la base de datos...");
    }

    // 4. Eventos de los Botones (Vincular en las propiedades 'On Action' o 'On Mouse Clicked' en Scene Builder)
    @FXML
    private void manejarRegistrarTurista(MouseEvent event) {
        System.out.println("Abriendo formulario para registrar un nuevo turista...");
        // Aquí puedes abrir una pequeña ventana emergente (Stage) para el formulario de registro
    }

    @FXML
    private void manejarBuscar(MouseEvent event) {
        String criterio = txtBuscar.getText();
        System.out.println("Filtrando tabla por: " + criterio);
    }
}
