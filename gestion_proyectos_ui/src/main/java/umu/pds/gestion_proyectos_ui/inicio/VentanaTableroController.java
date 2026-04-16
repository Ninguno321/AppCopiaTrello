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
import javafx.scene.control.TextField;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentanaTableroController {

    @FXML private HBox contenedorListas;
    @FXML private Button btnCrearLista;
    @FXML private Button btnBloquear;
    @FXML private ScrollPane scrollTablero;
    @FXML private ScrollBar scrollBarH;
    @FXML private HBox zonaEliminar;
    @FXML private TextField txtFiltro;
    @FXML private javafx.scene.control.ComboBox<String> cmbEtiquetas;

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

    public void cargarDatos(TableroDto tablero) {
        this.tableroId = tablero.id;
        this.tableroDto = tablero;
        this.tableroBlockeado = tablero.bloqueado;
        actualizarBotonBloqueo();
        actualizarDesplegableEtiquetas();
        aplicarFiltrosCombinados();
    }

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

        task.setOnFailed(e -> mostrarError("Error", "No se pudo cambiar el estado: " + task.getException().getMessage()));
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
                    trazas.stream().map(t -> formatearFecha(t.timestamp) + "  —  " + t.descripcion).toList()
                ));
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.initOwner(contenedorListas.getScene().getWindow());
            dialog.setTitle("Historial del tablero");
            dialog.getDialogPane().setContent(listView);
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dialog.showAndWait();
        });

        task.setOnFailed(e -> mostrarError("Error", "No se pudo obtener historial: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private String formatearFecha(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) return "—";
        String s = timestamp.replace("T", " ");
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(contenedorListas.getScene().getWindow());
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        hideTimer.setOnFinished(e -> ocultarZonaEliminar());

        scrollTablero.getParent().addEventFilter(DragEvent.DRAG_OVER, e -> {
            if (e.getDragboard().hasString()) {
                zonaEliminar.setVisible(true);
                zonaEliminar.setManaged(true);
                hideTimer.playFromStart();
            }
        });

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
            if (dragged instanceof HBox tarjetaNodo && tarjetaNodo.getUserData() instanceof VentanaTarjetaController tc) {
                
                // quitamos de la memoria la eliminada
                if (tableroDto != null && tableroDto.listas != null) {
                    for (ListaDto lista : tableroDto.listas) {
                        if (lista.id != null && lista.id.equals(tc.getListaId())) {
                            if (lista.tarjetas != null) {
                                lista.tarjetas.removeIf(t -> t.id.equals(tc.getTarjetaId()));
                            }
                            break;
                        }
                    }
                }

                // eliminamos dle backend
                Task<Void> task = service.eliminarTarjeta(tc.getTableroId(), tc.getListaId(), tc.getTarjetaId());
                task.setOnFailed(ev -> System.err.println("Error backend: " + task.getException().getMessage()));
                new Thread(task).start();

                // actualizamos
                actualizarDesplegableEtiquetas();
                aplicarFiltrosCombinados();
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
            ocultarZonaEliminar();
        });

        scrollTablero.getContent().boundsInLocalProperty().addListener((obs, o, n) -> actualizarScrollBar());
        scrollTablero.viewportBoundsProperty().addListener((obs, o, n) -> actualizarScrollBar());
        scrollBarH.valueProperty().addListener((obs, oldVal, newVal) -> scrollTablero.setHvalue(newVal.doubleValue()));
        scrollTablero.hvalueProperty().addListener((obs, oldVal, newVal) -> scrollBarH.setValue(newVal.doubleValue()));

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
                scrollTablero.setHvalue(Math.max(0, Math.min(1, hValueOnPress + deltaH)));
            }
        });

        scrollTablero.getContent().setOnMouseReleased(event -> scrollTablero.getContent().setCursor(Cursor.DEFAULT));
    }

    private void ocultarZonaEliminar() {
        zonaEliminar.setVisible(false);
        zonaEliminar.setManaged(false);
        zonaEliminar.getStyleClass().remove("zona-eliminar-hover");
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
            task.setOnSucceeded(e -> {
                if (tableroDto.listas == null) tableroDto.listas = new ArrayList<>();
                tableroDto.listas.add(task.getValue());
                aplicarFiltrosCombinados();
            });
            task.setOnFailed(e -> System.err.println("Error al crear lista: " + task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    public void actualizarDesplegableEtiquetas() {
        java.util.Set<String> etiquetasUnicas = new java.util.TreeSet<>();
        etiquetasUnicas.add("Todas");

        if (tableroDto != null && tableroDto.listas != null) {
            for (ListaDto lista : tableroDto.listas) {
                if (lista.tarjetas != null) {
                    for (TarjetaDto tarjeta : lista.tarjetas) {
                        if (tarjeta.etiquetas != null) {
                            for (umu.pds.gestion_proyectos_ui.api.dto.EtiquetaDto etiq : tarjeta.etiquetas) {
                                if (etiq.nombre != null && !etiq.nombre.isBlank()) {
                                    etiquetasUnicas.add(etiq.nombre);
                                }
                            }
                        }
                    }
                }
            }
        }

        String seleccionActual = cmbEtiquetas.getValue();
        if (seleccionActual == null) seleccionActual = "Todas";

        cmbEtiquetas.setOnAction(null);
        cmbEtiquetas.setItems(FXCollections.observableArrayList(etiquetasUnicas));
        
        if (etiquetasUnicas.contains(seleccionActual)) {
            cmbEtiquetas.getSelectionModel().select(seleccionActual);
        } else {
            cmbEtiquetas.getSelectionModel().select("Todas");
        }
        
        cmbEtiquetas.setOnAction(e -> aplicarFiltrosCombinados());
    }

    public void aplicarFiltrosCombinados() {
        if (tableroDto == null) return;
        contenedorListas.getChildren().clear();

        String textoFiltro = txtFiltro.getText() == null ? "" : txtFiltro.getText().toLowerCase().trim();
        String etiquetaSeleccionada = cmbEtiquetas.getValue();
        if (etiquetaSeleccionada == null) etiquetaSeleccionada = "Todas";

        if (tableroDto.listas != null) {
            for (ListaDto lista : tableroDto.listas) {
                renderizarLista(lista, textoFiltro, etiquetaSeleccionada);
            }
        }

        ListaDto listaCompletadas = new ListaDto();
        listaCompletadas.id = "ESPECIAL_COMPLETADAS";
        listaCompletadas.nombre = "✓ COMPLETADAS";
        listaCompletadas.tarjetas = tableroDto.tarjetasCompletadas != null ? tableroDto.tarjetasCompletadas : new ArrayList<>();
        renderizarLista(listaCompletadas, textoFiltro, etiquetaSeleccionada);
    }

    private void renderizarLista(ListaDto lista, String textoFiltro, String etiquetaSeleccionada) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaLista.fxml"));
            VBox nodoLista = loader.load();
            VentanaListaController controller = loader.getController();

            controller.setDatos(tableroId, lista.id, lista.nombre, tableroDto);
            controller.setTableroController(this);
            controller.getScroll().maxHeightProperty().bind(scrollTablero.heightProperty().subtract(220));

            if (lista.tarjetas != null) {
                for (TarjetaDto tarjeta : lista.tarjetas) {
                    boolean pasaFiltroTexto = textoFiltro.isEmpty() || 
                        (tarjeta.titulo != null && tarjeta.titulo.toLowerCase().contains(textoFiltro));
                    
                    boolean pasaFiltroEtiqueta = etiquetaSeleccionada.equals("Todas");
                    if (!pasaFiltroEtiqueta && tarjeta.etiquetas != null) {
                        for (umu.pds.gestion_proyectos_ui.api.dto.EtiquetaDto etiq : tarjeta.etiquetas) {
                            if (etiq.nombre != null && etiq.nombre.equals(etiquetaSeleccionada)) {
                                pasaFiltroEtiqueta = true;
                                break;
                            }
                        }
                    }

                    if (pasaFiltroTexto && pasaFiltroEtiqueta) {
                        controller.mostrarTarjeta(tarjeta);
                    }
                }
            }
            contenedorListas.getChildren().add(nodoLista);
        } catch (Exception e) {
            System.err.println("Error al mostrar lista filtrada: " + e.getMessage());
            e.printStackTrace();
        }
    }
}