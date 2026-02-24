package umu.pds.gestion_proyectos_ui;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * Hello world!
 *
 */
public class App extends Application
{
    private static final Logger log = LoggerFactory.getLogger(App.class);
	
	@Override
    public void start(Stage stage) throws Exception {
    	
        // Inicializar Configuración
        //Configuracion.setInstancia(new ConfiguracionImpl());
    	//ControladorApp appController = Configuracion.getInstancia().getControladorApp();

    	// Cargar los datos desde JSON
    	//try {
    	//    appController.cargarDatos();
    	//} catch (IOException e) {
    	 //   e.printStackTrace();
    	//}
        
        log.debug("Test App");
        // Cargar vista
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(
            		"/umu/pds/gestion_proyectos_ui/inicio/VentanaInicio.fxml"
            )
        );

        Parent root = loader.load();

        // Inyectar controlador de aplicación
       // VentanaInicioController viewController = loader.getController();
        //viewController.setControlador(
        //    Configuracion.getInstancia().getControladorApp()
        //);

        stage.setScene(new Scene(root, 1200, 700));
        stage.setTitle("Gestión de Proyectos");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
