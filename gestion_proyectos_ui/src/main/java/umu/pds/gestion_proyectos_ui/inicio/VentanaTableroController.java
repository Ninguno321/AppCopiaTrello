package umu.pds.gestion_proyectos_ui.inicio;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TrazaDto;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendService;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendServiceImpl;

import java.util.List;
import java.util.Optional;

public class VentanaTableroController {

    @FXML private HBox contenedorListas;
    @FXML private Button btnCrearLista;
    @FXML private Button btnBloquear;
    @FXML private ScrollPane scrollTablero;
    @FXML private ScrollBar scrollBarH;
    @FXML private HBox zonaEliminar;

    private double dragStartX;
    private double hValueOnPress;
    private final PauseTransition hideTimer = new PauseTransition(Duration.millis(300));

    private String tableroId;
    private boolean tableroBlockeado = false;
    private TableroDto tableroDto;
    private final GestionTableroFrontendService service = new GestionTableroFrontendServiceImpl();

    public void setTableroId(String tableroId) {
        this.tableroId = tableroId;
    }

    /**
     * Recibe un TableroDto completo y reconstruye las listas (con sus tarjetas).
     */
    public void cargarDatos(TableroDto tablero) {
        this.tableroId = tablero.id;
        this.tableroDto = tablero;
        this.tableroBlockeado = tablero.bloqueado;
        actualizarBotonBloqueo();
        if (tablero.listas != null) {
            for (ListaDto lista : tablero.listas) {
                mostrarListaConTarjetas(lista);
            }
        }
        // Lista virtual de tarjetas completadas (siempre al final)
        ListaDto listaCompletadas = new ListaDto();
        listaCompletadas.id = "ESPECIAL_COMPLETADAS";
        listaCompletadas.nombre = "✓ COMPLETADAS";
        listaCompletadas.tarjetas = tablero.tarjetasCompletadas != null
                ? tablero.tarjetasCompletadas
                : new java.util.ArrayList<>();
        mostrarListaConTarjetas(listaCompletadas);
    }

    /** Limpia el contenedor y recarga la vista desde el TableroDto en memoria. */
    public void recargarVista() {
        contenedorListas.getChildren().clear();
        cargarDatos(tableroDto);
    }

    private void actualizarBotonBloqueo() {
        if (tableroBlockeado) {
            btnBloquear.setText("Desbloquear");
            btnBloquear.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white;");
        } else {
            btnBloquear.setText("Bloquear");
            btnBloquear.setStyle("");
        }
    }

    @FXML
    void toggleBloqueo() {
        Task<Void> task = tableroBlockeado
                ? service.desbloquearTablero(tableroId)
                : service.bloquearTablero(tableroId);

        task.setOnSucceeded(e -> {
            tableroBlockeado = !tableroBlockeado;
            actualizarBotonBloqueo();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(contenedorListas.getScene().getWindow());
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cambiar el estado del tablero: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    @FXML
    void verHistorial() {
        Task<List<TrazaDto>> task = service.obtenerHistorial(tableroId);

        task.setOnSucceeded(e -> {
            List<TrazaDto> trazas = task.getValue();

            ListView<String> listView = new ListView<>();
            listView.setPrefWidth(500);
            listView.setPrefHeight(350);

            if (trazas == null || trazas.isEmpty()) {
                listView.setItems(FXCollections.observableArrayList("No hay entradas en el historial."));
            } else {
                listView.setItems(FXCollections.observableArrayList(
                    trazas.stream()
                          .map(t -> formatearFecha(t.timestamp) + "  —  " + t.descripcion)
                          .toList()
                ));
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.initOwner(contenedorListas.getScene().getWindow());
            dialog.setTitle("Historial del tablero");
            dialog.setHeaderText(null);

            DialogPane pane = dialog.getDialogPane();
            pane.setContent(listView);
            pane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

            dialog.showAndWait();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(contenedorListas.getScene().getWindow());
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo obtener el historial: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    private String formatearFecha(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) return "—";
        // "2026-03-30T14:05:32" → "2026-03-30 14:05"
        String s = timestamp.replace("T", " ");
        if (s.length() > 16) s = s.substring(0, 16);
        return s;
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
                Task<Void> task = service.eliminarTarjeta(tc.getTableroId(), tc.getListaId(), tc.getTarjetaId());
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
        dialog.initOwner(contenedorListas.getScene().getWindow());
        dialog.setTitle("Nueva lista");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre de la lista:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (nombre.isBlank()) return;

            Task<ListaDto> task = service.agregarLista(tableroId, nombre);

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

            controller.setDatos(tableroId, lista.id, lista.nombre, tableroDto);
            controller.setTableroController(this);
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

            controller.setDatos(tableroId, lista.id, lista.nombre, tableroDto);
            controller.setTableroController(this);
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
