package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class VentanaInicioController {

    @FXML private TextField txtNombreTablero;
    @FXML private TextField txtEmail;
    @FXML private Button btnCrearTablero;

    @FXML
    void onCrearTablero() {
        // TODO: validar que nombre y email no estén vacíos
        // TODO: llamar al backend POST /tableros con nombre y email
        // TODO: navegar a VentanaPrincipal con el tablero creado
    }
}
