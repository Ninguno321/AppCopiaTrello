package umu.pds.gestion_proyectos_ui.inicio;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

import java.util.Optional;

public class VentanaTableroController {

    @FXML private HBox contenedorListas;
    @FXML private Button btnCrearLista;
    @FXML private ScrollPane scrollTablero;
    @FXML private ScrollBar scrollBarH;
    @FXML private HBox zonaEliminar;

    private double dragStartX;
    private double hValueOnPress;
    private final PauseTransition hideTimer = new PauseTransition(Duration.millis(300));

    private String tableroId;
    private final TableroApiClient apiClient = new TableroApiClient();

    public void setTableroId(String tableroId) {
        this.tableroId = tableroId;
    }

    /**
     * Recibe un TableroDto completo y reconstruye las listas (con sus tarjetas).
     */
    public void cargarDatos(TableroDto tablero) {
        this.tableroId = tablero.id;
        if (tablero.listas == null) return;
        for (ListaDto lista : tablero.listas) {
            mostrarListaConTarjetas(lista);
        }
    }

    @FXML
    public void initialize() {
        // Timer para ocultar la zona de eliminación cuando el drag acaba
        hideTimer.setOnFinished(e -> {
            zonaEliminar.setVisible(false);
            zonaEliminar.setManaged(false);
            zonaEliminar.getStyleClass().remove("zona-eliminar-hover");
        });

        // Mostrar zona de eliminación cuando se arrastra algo sobre el tablero
        scrollTablero.getParent().addEventFilter(DragEvent.DRAG_OVER, e -> {
            if (e.getDragboard().hasString()) {
                zonaEliminar.setVisible(true);
                zonaEliminar.setManaged(true);
                hideTimer.playFromStart();
            }
        });

        // Zona de eliminación acepta drops
        zonaEliminar.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                if (!zonaEliminar.getStyleClass().contains("zona-eliminar-hover")) {
                    zonaEliminar.getStyleClass().add("zona-eliminar-hover");
                }
                hideTimer.stop();
            }
            e.consume();
        });

        zonaEliminar.setOnDragExited(e -> {
            zonaEliminar.getStyleClass().remove("zona-eliminar-hover");
            hideTimer.playFromStart();
        });

        zonaEliminar.setOnDragDropped(e -> {
            boolean success = false;
            Object dragged = zonaEliminar.getScene().getUserData();
            if (dragged instanceof HBox tarjeta && tarjeta.getUserData() instanceof VentanaTarjetaController tc) {
                // Eliminar visualmente
                if (tarjeta.getParent() instanceof VBox origen) {
                    origen.getChildren().remove(tarjeta);
                }
                // Eliminar en backend
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        apiClient.eliminarTarjeta(tc.getTableroId(), tc.getListaId(), tc.getTarjetaId());
                        return null;
                    }
                };
                task.setOnFailed(ev -> System.err.println("Error al eliminar tarjeta: " + task.getException().getMessage()));
                new Thread(task).start();
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
            zonaEliminar.setVisible(false);
            zonaEliminar.setManaged(false);
            zonaEliminar.getStyleClass().remove("zona-eliminar-hover");
        });

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

    private void mostrarListaConTarjetas(ListaDto lista) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaLista.fxml")
            );
            VBox nodoLista = loader.load();
            VentanaListaController controller = loader.getController();

            controller.setDatos(tableroId, lista.id, lista.nombre);
            controller.getScroll().maxHeightProperty()
                    .bind(scrollTablero.heightProperty().subtract(220));

            // Cargar tarjetas existentes
            if (lista.tarjetas != null) {
                for (TarjetaDto tarjeta : lista.tarjetas) {
                    controller.mostrarTarjeta(tarjeta);
                }
            }

            contenedorListas.getChildren().add(nodoLista);

        } catch (Exception e) {
            System.err.println("Error al mostrar lista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
