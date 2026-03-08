package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.prefs.Preferences;
public class VentanaInicioController {

    @FXML
    private Button boton1;

    @FXML
    private HBox hbox;
    
    @FXML
    private ColorPicker color;

    
    @FXML
    public void initialize() {

        Preferences prefs = Preferences.userNodeForPackage(VentanaInicioController.class);
        								//Devuelve el colorBoton y si no lo enncuentra pues el default que es el #4F29F0
        String colorGuardado = prefs.get("colorBoton", "#4F29F0");

        boton1.setStyle("-fx-background-color: " + colorGuardado + ";");

        color.setValue(Color.web(colorGuardado));
    }
    
    @FXML
    public void crearNuevaLista() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaLista.fxml"));            
            
            // La raíz del FXML es un VBox, así que lo cargamos como VBox
            VBox nodoLista = loader.load(); 
            
            // Pero lo añadimos al "hbox" (que es el contenedor horizontal)
            hbox.getChildren().add(nodoLista);
            cambiarColorGlobal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void cambiarColorGlobal() {

        Color colorC = color.getValue();

        String colorWeb = String.format("#%02X%02X%02X",
                (int)(colorC.getRed() * 255),
                (int)(colorC.getGreen() * 255),
                (int)(colorC.getBlue() * 255));

        boton1.setStyle("-fx-background-color: " + colorWeb + ";");

        // GUARDAR COLOR
        Preferences prefs = Preferences.userNodeForPackage(VentanaInicioController.class);
        prefs.put("colorBoton", colorWeb);
    }
    

}