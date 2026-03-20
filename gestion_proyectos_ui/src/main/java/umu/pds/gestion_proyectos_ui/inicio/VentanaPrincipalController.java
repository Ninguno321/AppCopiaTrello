package umu.pds.gestion_proyectos_ui.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;

import java.util.List;

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
    private TableroDto tableroActual;

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
            lblTableroActual.setText(tablero.nombre);
            cargarVistaTablero();
        });
        sidebarTableros.getChildren().add(btn);
    }

    @FXML
    void onCrearTablero(ActionEvent event) {
        // TODO: abrir diálogo de creación de tablero
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/umu/pds/gestion_proyectos_ui/inicio/VentanaTablero.fxml"
            ));
            Node vista = loader.load();
            VentanaTableroController controller = loader.getController();
            controller.setTableroId(tableroActual.id);
            mainContentPane.getChildren().setAll(vista);
            activarTab(btnTabTablero);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
