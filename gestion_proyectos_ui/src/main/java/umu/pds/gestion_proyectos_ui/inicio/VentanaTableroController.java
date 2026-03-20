package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VentanaTableroController {

    @FXML private HBox contenedorListas;
    @FXML private Button btnCrearLista;
    @FXML private ScrollPane scrollTablero;
    @FXML private ScrollBar scrollBarH;

    private double dragStartX;
    private double hValueOnPress;

    @FXML
    public void initialize() {

        // Actualiza visibleAmount y visibilidad cuando cambie el contenido o el viewport
        scrollTablero.getContent().boundsInLocalProperty().addListener((obs, o, n) -> actualizarScrollBar());
        scrollTablero.viewportBoundsProperty().addListener((obs, o, n) -> actualizarScrollBar());

        // ScrollBar manual → mueve el ScrollPane
        scrollBarH.valueProperty().addListener((obs, oldVal, newVal) -> {
            scrollTablero.setHvalue(newVal.doubleValue());
        });

        // ScrollPane → actualiza el ScrollBar
        scrollTablero.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            scrollBarH.setValue(newVal.doubleValue());
        });

        // Drag sobre el contenido → panning
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
            scrollTablero.getContent().setCursor(Cursor.DEFAULT)
        );
    }

    private void actualizarScrollBar() {
        double contentWidth = scrollTablero.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = scrollTablero.getViewportBounds().getWidth();

        if (contentWidth > 0 && viewportWidth > 0) {
            double visible = viewportWidth / contentWidth;
            scrollBarH.setVisibleAmount(visible);
        }

        scrollBarH.setVisible(contentWidth > viewportWidth);
        scrollBarH.setManaged(contentWidth > viewportWidth);
    }

    @FXML
    void crearLista(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaLista.fxml")
            );

            VBox nodoLista = loader.load();

            VentanaListaController controller = loader.getController();

            controller.getScroll().maxHeightProperty()
                    .bind(scrollTablero.heightProperty().subtract(220));

            contenedorListas.getChildren().add(nodoLista);

        } catch (Exception e) {
            System.err.println("Error al crear lista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
