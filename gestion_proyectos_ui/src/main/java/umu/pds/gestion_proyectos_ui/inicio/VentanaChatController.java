package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendService;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendServiceImpl;

public class VentanaChatController {
	@FXML private VBox chatContainer;
	@FXML private TextField inputField;
	@FXML private ScrollPane scrollPane;
	
	private final GestionTableroFrontendService apiClient = new GestionTableroFrontendServiceImpl();
	private TableroDto tablero;
	private String contextoGlobal;

	    @FXML
	    void onEnviarMensaje() {
	        String mensaje = inputField.getText().trim();
	        if (mensaje.isEmpty()) return;

	        // Mostrar mensaje del usuario
	        agregarMensaje(mensaje, true);
	        inputField.clear();

	        // Mostrar "escribiendo..."
	        Label escribiendo = agregarMensaje("Escribiendo...", false);

	       /* // Llamada a la API en segundo plano
	        Task<String> task = new Task<>() {
	            @Override
	            protected String call() throws Exception {
	            	String value = contextoGlobal + "Nuevo: Ultima pregunta: " + mensaje;
	                String res =  apiClient.preguntarAI(tablero, value);
	                contextoGlobal = value;
	                return res;
	            }
	        };
*/	            	
	        contextoGlobal = contextoGlobal + "Nuevo: Ultima pregunta: " + mensaje;

		    Task<String> task = apiClient.preguntarAI(tablero, contextoGlobal);

	        task.setOnSucceeded(e -> {
	            String respuesta = task.getValue();
	            contextoGlobal = contextoGlobal + "Ultima respuesta: " + respuesta;
	            // Reemplazar "Escribiendo..."
	            escribiendo.setText(respuesta);
	        });

	        task.setOnFailed(e -> {
	            Throwable ex = task.getException();
	            escribiendo.setText("Error:desconocido, prueba en unos segunos...");
	            ex.printStackTrace(); 
	        });

	        new Thread(task).start();
	    }
	    

		public void setNombreTablero(TableroDto nombreTablero) {
			this.tablero = nombreTablero;
		}
	    

	    /**
	     * Agrega un mensaje al chat
	     * @param texto contenido del mensaje
	     * @param esUsuario true = derecha, false = izquierda
	     * @return Label del mensaje (para poder actualizarlo si hace falta)
	     */
	    private Label agregarMensaje(String texto, boolean esUsuario) {
	        Label mensaje = new Label(texto);
	        mensaje.setWrapText(true);
	        mensaje.setMaxWidth(400);

	        HBox contenedor = new HBox(mensaje);

	        if (esUsuario) {
	            contenedor.setStyle("-fx-alignment: CENTER-RIGHT;");
	            mensaje.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 10;");
	        } else {
	            contenedor.setStyle("-fx-alignment: CENTER-LEFT;");
	            mensaje.setStyle("-fx-background-color: #e4e6eb; -fx-padding: 8; -fx-background-radius: 10;");
	        }

	        chatContainer.getChildren().add(contenedor);

	        // Auto-scroll
	        scrollPane.layout();
	        scrollPane.setVvalue(1.0);

	        return mensaje;
	    }

	    /**
	     * Enviar con ENTER
	     */
	    @FXML
	    void initialize() {
	        inputField.setOnAction(e -> onEnviarMensaje());
	    }
}

