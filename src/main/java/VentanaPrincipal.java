import ActividadRecreativa.ActividadRecreativaDAO;
import Evento.EventoDAO;
import Gastronomia.GastronomiaDAO;
import PuntoTuristico.PuntoTuristicoDAO;
import Servicio.ServicioDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VentanaPrincipal {

    private Stage primaryStage;
    private BorderPane root;
    private TabPane tabPane;

    public VentanaPrincipal(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        this.tabPane = new TabPane();
    }

    public void mostrar() {
        HBox topBar = crearTopBar();
        root.setTop(topBar);

        VBox sidebar = crearSidebar();
        root.setLeft(sidebar);

        TabPane tabPane = crearTabPane();
        root.setCenter(tabPane);

        HBox statusBar = crearStatusBar();
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Sistema de Gestión Turística - San Antonio de Arredondo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox crearTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setStyle("-fx-background-color: #0284c7;");

        Label lblTitle = new Label("🏛 Sistema de Gestión Turística");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUser = new Label("👤 Administrador");
        lblUser.setFont(Font.font("System", 13));
        lblUser.setTextFill(Color.web("#bfdbfe"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #ef4444; -fx-background-radius: 4; -fx-padding: 4 12;");
        btnLogout.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cerrar sesión");
            confirm.setHeaderText("¿Está seguro que desea cerrar sesión?");
            confirm.setContentText("Todos los cambios guardados permanecerán.");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                primaryStage.close();
            }
        });

        topBar.getChildren().addAll(lblTitle, spacer, lblUser, btnLogout);
        return topBar;
    }

    private VBox crearSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(10, 10, 10, 15));
        sidebar.setStyle("-fx-background-color: #e0f2fe; -fx-border-color: #cbd5e1; -fx-border-width: 0 1 0 0;");

        Label lblLogo = new Label("Municipalidad");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblLogo.setTextFill(Color.web("#0369a1"));
        lblLogo.setPadding(new Insets(0, 0, 15, 0));

        Label lblSubLogo = new Label("San Antonio de Arredondo");
        lblSubLogo.setFont(Font.font("System", 11));
        lblSubLogo.setTextFill(Color.web("#64748b"));
        lblSubLogo.setPadding(new Insets(-10, 0, 15, 0));

        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 10, 0));

        Label lblTuristas = new Label("CATÁLOGO");
        lblTuristas.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblTuristas.setTextFill(Color.web("#64748b"));
        lblTuristas.setPadding(new Insets(10, 0, 5, 0));

        VBox menuBox = new VBox(3);

        Button btnActividades = crearBotonMenu("🏃 Actividades");
        Button btnAtractivos = crearBotonMenu("🏛 Atractivos");
        Button btnGastronomia = crearBotonMenu("🍽 Gastronomía");
        Button btnServicios = crearBotonMenu("🔧 Servicios");
        Button btnEventos = crearBotonMenu("🎪 Eventos");

        btnActividades.setOnAction(e -> seleccionarPestana(0));
        btnAtractivos.setOnAction(e -> seleccionarPestana(1));
        btnGastronomia.setOnAction(e -> seleccionarPestana(2));
        btnServicios.setOnAction(e -> seleccionarPestana(3));
        btnEventos.setOnAction(e -> seleccionarPestana(4));

        menuBox.getChildren().addAll(btnActividades, btnAtractivos, btnGastronomia,
                btnServicios, btnEventos);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label lblBackup = new Label("🔄 Backup automático cada 30 min");
        lblBackup.setFont(Font.font("System", 9));
        lblBackup.setTextFill(Color.web("#94a3b8"));
        lblBackup.setPadding(new Insets(10, 0, 5, 0));

        sidebar.getChildren().addAll(lblLogo, lblSubLogo, separator, lblTuristas,
                menuBox, spacer, lblBackup);
        return sidebar;
    }

    private Button crearBotonMenu(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 12, 8, 12));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #334155; -fx-font-size: 13px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #0369a1; -fx-font-size: 13px; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #334155; -fx-font-size: 13px;"));
        return btn;
    }

    private TabPane crearTabPane() {
        Tab tabActividades = new Tab("🏃 Actividades");
        tabActividades.setContent(new ActividadRecreativaDAO().getVista());
        tabActividades.setClosable(false);

        Tab tabAtractivos = new Tab("🏛 Atractivos");
        tabAtractivos.setContent(new PuntoTuristicoDAO().getVista());
        tabAtractivos.setClosable(false);

        Tab tabGastronomia = new Tab("🍽 Gastronomía");
        tabGastronomia.setContent(new GastronomiaDAO().getVista());
        tabGastronomia.setClosable(false);

        Tab tabServicios = new Tab("🔧 Servicios");
        tabServicios.setContent(new ServicioDAO().getVista());
        tabServicios.setClosable(false);

        Tab tabEventos = new Tab("🎪 Eventos");
        tabEventos.setContent(new EventoDAO().getVista());
        tabEventos.setClosable(false);

        tabPane.getTabs().addAll(tabActividades, tabAtractivos, tabGastronomia,
                tabServicios, tabEventos);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        return tabPane;
    }

    private void seleccionarPestana(int index) {
        if (index >= 0 && index < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(index);
        }
    }

    private HBox crearStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Label lblStatus = new Label("✅ Sistema operativo");
        lblStatus.setFont(Font.font("System", 11));
        lblStatus.setTextFill(Color.web("#64748b"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblVersion = new Label("Versión 1.0.0");
        lblVersion.setFont(Font.font("System", 11));
        lblVersion.setTextFill(Color.web("#94a3b8"));

        Label lblConexion = new Label("🟢 Conectado");
        lblConexion.setFont(Font.font("System", 11));
        lblConexion.setTextFill(Color.web("#22c55e"));

        statusBar.getChildren().addAll(lblStatus, spacer, lblVersion, lblConexion);
        return statusBar;
    }
}