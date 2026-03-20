package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;

import java.util.Optional;

public class VentanaTableroController {

    @FXML private HBox contenedorListas;
    @FXML private Button btnCrearLista;
    @FXML private ScrollPane scrollTablero;
    @FXML private ScrollBar scrollBarH;

    private double dragStartX;
    private double hValueOnPress;

    private String tableroId;
    private final TableroApiClient apiClient = new TableroApiClient();

    public void setTableroId(String tableroId) {
        this.tableroId = tableroId;
    }

    @FXML
    public void initialize() {
        scrollTablero.getContent().boundsInLocalProperty().addListener((obs, o, n) -> actualizarScrollBar());
        scrollTablero.viewportBoundsProperty().addListener((obs, o, n) -> actualizarScrollBar());

        scrollBarH.valueProperty().addListener((obs, oldVal, newVal) ->
            scrollTablero.setHvalue(newVal.doubleValue()));

        scrollTablero.hvalueProperty().addListener((obs, oldVal, newVal) ->
            scrollBarH.setValue(newVal.doubleValue()));

        scrollTablero.getContent().setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            hValueOnPress = scrollTablero.getHvalue();
            scrollTablero.getContent().setCursor(Cursor.CLOSED_HAND);
        });

        scrollTablero.getContent().setOnMouseDragged(event -> {
            double contentWidth = scrollTablero.getContent().getBoundsInLocal().getWidth();
            double viewportWidth = scrollTablero.getViewportBounds().getWidth();
            double scrollableWidth = contentWidth - viewportWidth;
            if (scrollableWidth > 0) {
                double dx = event.getSceneX() - dragStartX;
                double deltaH = -dx / scrollableWidth;
                double newH = Math.max(0, Math.min(1, hValueOnPress + deltaH));
                scrollTablero.setHvalue(newH);
            }
        });

        scrollTablero.getContent().setOnMouseReleased(event ->
            scrollTablero.getContent().setCursor(Cursor.DEFAULT));
    }

    private void actualizarScrollBar() {
        double contentWidth = scrollTablero.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = scrollTablero.getViewportBounds().getWidth();
        if (contentWidth > 0 && viewportWidth > 0) {
            scrollBarH.setVisibleAmount(viewportWidth / contentWidth);
        }
        scrollBarH.setVisible(contentWidth > viewportWidth);
        scrollBarH.setManaged(contentWidth > viewportWidth);
    }

    @FXML
    void crearLista(MouseEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva lista");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre de la lista:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (nombre.isBlank()) return;

            Task<ListaDto> task = new Task<>() {
                @Override
                protected ListaDto call() throws Exception {
                    return apiClient.agregarLista(tableroId, nombre);
                }
            };

            task.setOnSucceeded(e -> mostrarLista(task.getValue()));
            task.setOnFailed(e -> System.err.println("Error al crear lista: " + task.getException().getMessage()));

            new Thread(task).start();
        });
    }

    private void mostrarLista(ListaDto lista) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaLista.fxml")
            );
            VBox nodoLista = loader.load();
            VentanaListaController controller = loader.getController();

            controller.setDatos(tableroId, lista.id, lista.nombre);
            controller.getScroll().maxHeightProperty()
                    .bind(scrollTablero.heightProperty().subtract(220));

            contenedorListas.getChildren().add(nodoLista);

        } catch (Exception e) {
            System.err.println("Error al mostrar lista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
