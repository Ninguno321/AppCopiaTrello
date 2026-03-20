package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;

public class VentanaInicioController {

    @FXML private TextField txtNombreTablero;
    @FXML private TextField txtEmail;
    @FXML private Button btnCrearTablero;

    private final TableroApiClient apiClient = new TableroApiClient();

    @FXML
    void onCrearTablero() {
        String nombre = txtNombreTablero.getText().trim();
        String email = txtEmail.getText().trim();

        if (nombre.isBlank() || email.isBlank()) {
            mostrarError("Campos vacíos", "El nombre del tablero y el correo son obligatorios.");
            return;
        }

        btnCrearTablero.setDisable(true);

        Task<TableroDto> task = new Task<>() {
            @Override
            protected TableroDto call() throws Exception {
                return apiClient.crearTablero(nombre, email);
            }
        };

        task.setOnSucceeded(e -> navegarAVentanaPrincipal(task.getValue()));

        task.setOnFailed(e -> {
            btnCrearTablero.setDisable(false);
            mostrarError("Error al crear tablero", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void navegarAVentanaPrincipal(TableroDto tablero) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/umu/pds/gestion_proyectos_ui/inicio/VentanaPrincipal.fxml"
            ));
            Parent root = loader.load();

            VentanaPrincipalController controller = loader.getController();
            controller.setTablero(tablero);

            Stage stage = (Stage) btnCrearTablero.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            mostrarError("Error de navegación", e.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
