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

import java.util.List;

public class VentanaInicioController {

    @FXML private TextField txtEmail;
    @FXML private Button btnEntrar;

    private final TableroApiClient apiClient = new TableroApiClient();

    @FXML
    void onEntrar() {
        String email = txtEmail.getText().trim();

        if (email.isBlank()) {
            mostrarError("Campo vacío", "El correo electrónico es obligatorio.");
            return;
        }

        btnEntrar.setDisable(true);

        Task<List<TableroDto>> task = new Task<>() {
            @Override
            protected List<TableroDto> call() throws Exception {
                return apiClient.obtenerTablerosPorEmail(email);
            }
        };

        task.setOnSucceeded(e -> navegarAVentanaPrincipal(email, task.getValue()));

        task.setOnFailed(e -> {
            btnEntrar.setDisable(false);
            mostrarError("Error al conectar", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void navegarAVentanaPrincipal(String email, List<TableroDto> tableros) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/umu/pds/gestion_proyectos_ui/inicio/VentanaPrincipal.fxml"
            ));
            Parent root = loader.load();

            VentanaPrincipalController controller = loader.getController();
            controller.setDatosUsuario(email, tableros);

            Stage stage = (Stage) btnEntrar.getScene().getWindow();
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
