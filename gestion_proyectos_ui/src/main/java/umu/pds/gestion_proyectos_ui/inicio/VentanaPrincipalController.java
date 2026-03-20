package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentanaPrincipalController {

    // Sidebar
    @FXML private VBox sidebarTableros;
    @FXML private Button btnCrearTablero;
    @FXML private Button btnConfiguracion;

    // Cabecera del contenido central
    @FXML private Label  lblTableroActual;
    @FXML private Button btnAutomatizaciones;

    // Pestañas de navegación
    @FXML private Button btnTabTablero;
    @FXML private Button btnTabCalendario;
    @FXML private Button btnTabTabla;

    // Área de contenido intercambiable
    @FXML private StackPane mainContentPane;

    private List<Button> tabButtons;
    private final List<Button> sidebarButtons = new ArrayList<>();
    private TableroDto tableroActual;
    private String emailActual;
    private final TableroApiClient apiClient = new TableroApiClient();

    @FXML
    public void initialize() {
        tabButtons = List.of(btnTabTablero, btnTabCalendario, btnTabTabla);
    }

    /**
     * Recibe el tablero recién creado desde VentanaInicioController
     * y actualiza la vista con sus datos.
     */
    public void setTablero(TableroDto tablero) {
        this.tableroActual = tablero;
        this.emailActual = tablero.emailPropietario;
        lblTableroActual.setText(tablero.nombre);
        agregarBotonSidebar(tablero);
        cargarVistaTablero();
    }

    // --- Sidebar ---

    private void agregarBotonSidebar(TableroDto tablero) {
        Button btn = new Button(tablero.nombre);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-item");
        btn.setOnAction(e -> {
            tableroActual = tablero;
            lblTableroActual.setText(tablero.nombre);
            activarSidebarButton(btn);
            cargarVistaTablero();
        });
        sidebarButtons.add(btn);
        sidebarTableros.getChildren().add(btn);
        activarSidebarButton(btn);
    }

    private void activarSidebarButton(Button activo) {
        for (Button btn : sidebarButtons) {
            btn.getStyleClass().remove("active");
        }
        if (!activo.getStyleClass().contains("active")) {
            activo.getStyleClass().add("active");
        }
    }

    @FXML
    void onCrearTablero(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo tablero");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre del tablero:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (nombre.isBlank()) return;

            Task<TableroDto> task = new Task<>() {
                @Override
                protected TableroDto call() throws Exception {
                    return apiClient.crearTablero(nombre, emailActual);
                }
            };

            task.setOnSucceeded(e -> {
                TableroDto nuevoTablero = task.getValue();
                agregarBotonSidebar(nuevoTablero);
                tableroActual = nuevoTablero;
                lblTableroActual.setText(nuevoTablero.nombre);
                cargarVistaTablero();
            });

            task.setOnFailed(e -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error al crear tablero: " + task.getException().getMessage());
                alert.showAndWait();
            });

            new Thread(task).start();
        });
    }

    @FXML
    void onConfiguracion(ActionEvent event) {
        // TODO: navegar a la pantalla de configuración
    }

    // --- Cabecera ---

    @FXML
    void onAutomatizaciones(ActionEvent event) {
        // TODO: abrir sección de automatizaciones del tablero actual
    }

    // --- Pestañas de navegación ---

    @FXML
    void onTabTablero(ActionEvent event) {
        activarTab(btnTabTablero);
        cargarVistaTablero();
    }

    @FXML
    void onTabCalendario(ActionEvent event) {
        activarTab(btnTabCalendario);
        // TODO: cargar vista de Calendario en mainContentPane
    }

    @FXML
    void onTabTabla(ActionEvent event) {
        activarTab(btnTabTabla);
        // TODO: cargar vista de Tabla en mainContentPane
    }

    // --- Helpers ---

    private void cargarVistaTablero() {
        if (tableroActual == null) return;

        // Obtener datos actualizados del backend en hilo de fondo
        Task<TableroDto> task = new Task<>() {
            @Override
            protected TableroDto call() throws Exception {
                return apiClient.obtenerTablero(tableroActual.id);
            }
        };

        task.setOnSucceeded(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/umu/pds/gestion_proyectos_ui/inicio/VentanaTablero.fxml"
                ));
                Node vista = loader.load();
                VentanaTableroController controller = loader.getController();
                controller.cargarDatos(task.getValue());
                mainContentPane.getChildren().setAll(vista);
                activarTab(btnTabTablero);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Error al obtener tablero: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void activarTab(Button tabActivo) {
        for (Button tab : tabButtons) {
            tab.getStyleClass().remove("active");
        }
        if (!tabActivo.getStyleClass().contains("active")) {
            tabActivo.getStyleClass().add("active");
        }
    }
}
