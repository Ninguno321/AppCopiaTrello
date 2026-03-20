package umu.pds.gestion_proyectos_ui.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.List;

public class VentanaPrincipalController {

    // Sidebar
    @FXML private Button btnTablero1;
    @FXML private Button btnTablero2;
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

    @FXML
    public void initialize() {
        tabButtons = List.of(btnTabTablero, btnTabCalendario, btnTabTabla);
    }

    // Sidebar: selección de tablero

    @FXML
    void onTableroSeleccionado(ActionEvent event) {
        Button origen = (Button) event.getSource();
        lblTableroActual.setText(origen.getText());
        // TODO: llamar al backend para cargar el tablero seleccionado
    }

    @FXML
    void onCrearTablero(ActionEvent event) {
        // TODO: abrir diálogo de creación de tablero
    }

    @FXML
    void onConfiguracion(ActionEvent event) {
        // TODO: navegar a la pantalla de configuración
    }

    // Cabecera: automatizaciones
    @FXML
    void onAutomatizaciones(ActionEvent event) {
        // TODO: abrir sección de automatizaciones del tablero actual
    }

    // Pestañas de navegación

    @FXML
    void onTabTablero(ActionEvent event) {
        activarTab(btnTabTablero);
        // TODO: cargar VentanaTablero.fxml en mainContentPane
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

    // Helpers

    private void activarTab(Button tabActivo) {
        for (Button tab : tabButtons) {
            tab.getStyleClass().remove("active");
        }
        if (!tabActivo.getStyleClass().contains("active")) {
            tabActivo.getStyleClass().add("active");
        }
    }
}
