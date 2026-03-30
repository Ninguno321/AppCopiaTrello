package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.ChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.ItemChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VentanaListaController {

    @FXML private Button btnMenuLista;
    @FXML private Button btnAnadirTarjeta;
    @FXML private HBox headerLista;
    @FXML private HBox footerLista;
    @FXML private VBox contenedorTarjetas;
    @FXML private ScrollPane scroll;
    @FXML private TextField titulo;

    // Placeholder visual: línea azul que se mueve entre tarjetas.
    private HBox placeholder;

    private String tableroId;
    private String listaId;
    private final TableroApiClient apiClient = new TableroApiClient();

    public void setDatos(String tableroId, String listaId, String nombre) {
        this.tableroId = tableroId;
        this.listaId = listaId;
        titulo.setText(nombre);
    }

    public ScrollPane getScroll() {
        return scroll;
    }

    public void limitarAltura(double alturaMaxima) {
        scroll.setMaxHeight(alturaMaxima);
    }

    private void confirmarTitulo() {
        titulo.setEditable(false);
        titulo.deselect();
        // Mover el foco al contenedor padre para quitar el :focused del TextField
        if (titulo.getParent() != null) {
            titulo.getParent().requestFocus();
        }
    }

    @FXML
    public void initialize() {
        // Inicializamos el contenedor del placeholder (se configurará dinámicamente al arrastrar)
        placeholder = new HBox();

        // --- DRAG & DROP (Placeholder con apariencia de tarjeta) ---
        contenedorTarjetas.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);

                // Obtenemos la tarjeta real que se está arrastrando desde el UserData de la Scene
                Object dragged = contenedorTarjetas.getScene().getUserData();

                if (dragged instanceof HBox tarjetaOriginal) {
                    // 1. Clonamos dimensiones para que la lista no dé saltos
                    placeholder.setPrefHeight(tarjetaOriginal.getHeight());
                    placeholder.setMinHeight(tarjetaOriginal.getHeight());
                    placeholder.setMaxHeight(tarjetaOriginal.getHeight());
                    placeholder.setPrefWidth(tarjetaOriginal.getWidth());

                    // 2. Estética: Color de fondo suave, borde punteado y opacidad
                    placeholder.setStyle(
                        "-fx-background-color: #ebecf0; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-color: #0079BF; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: segments(5, 5); " +
                        "-fx-border-radius: 5;"
                    );
                    placeholder.setOpacity(0.5);

                    // 3. Calculamos la posición y movemos el placeholder si es necesario
                    int index = calcularIndice(event.getY());

                    if (!contenedorTarjetas.getChildren().contains(placeholder)) {
                        contenedorTarjetas.getChildren().add(Math.min(index, contenedorTarjetas.getChildren().size()), placeholder);
                    } else {
                        int currentIndex = contenedorTarjetas.getChildren().indexOf(placeholder);
                        if (currentIndex != index) {
                            contenedorTarjetas.getChildren().remove(placeholder);
                            int newIndex = calcularIndice(event.getY());
                            contenedorTarjetas.getChildren().add(Math.min(newIndex, contenedorTarjetas.getChildren().size()), placeholder);
                        }
                    }
                }
            }
            event.consume();
        });

        contenedorTarjetas.setOnDragDropped(event -> {
            boolean success = false;
            Object dragged = contenedorTarjetas.getScene().getUserData();

            if (dragged instanceof HBox tarjeta) {
                int dropIndex = contenedorTarjetas.getChildren().indexOf(placeholder);

                contenedorTarjetas.getChildren().remove(placeholder);

                if (tarjeta.getParent() instanceof VBox origen) {
                    origen.getChildren().remove(tarjeta);
                }

                if (dropIndex < 0) dropIndex = contenedorTarjetas.getChildren().size();
                contenedorTarjetas.getChildren().add(Math.min(dropIndex, contenedorTarjetas.getChildren().size()), tarjeta);

                // Notificar al backend si la tarjeta cambió de lista
                if (tarjeta.getUserData() instanceof VentanaTarjetaController tc) {
                    String listaOrigen = tc.getListaId();
                    if (!Objects.equals(listaOrigen, listaId)) {
                        tc.setListaId(listaId);
                        String tId = tc.getTarjetaId();
                        String tbId = tc.getTableroId();
                        Task<Void> mover = new Task<>() {
                            @Override
                            protected Void call() throws Exception {
                                apiClient.moverTarjeta(tbId, tId, listaOrigen, listaId);
                                return null;
                            }
                        };
                        mover.setOnFailed(e -> System.err.println("Error al mover tarjeta: " + mover.getException().getMessage()));
                        new Thread(mover).start();
                    }
                }

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // Si el ratón sale de la lista sin soltar, quitamos la sombra
        contenedorTarjetas.setOnDragExited(event -> {
            if (!event.isDropCompleted()) {
                contenedorTarjetas.getChildren().remove(placeholder);
            }
        });

        // --- TÍTULO: ENTER o TAB para confirmar ---
        titulo.setOnAction(e -> confirmarTitulo());

        // --- TÍTULO: clic fuera (pierde foco) para confirmar ---
        titulo.focusedProperty().addListener((obs, teniafoco, tienefoco) -> {
            if (!tienefoco) confirmarTitulo();
        });

        // --- TÍTULO: clic para editar ---
        titulo.setOnMouseClicked(e -> {
            titulo.setEditable(true);
            titulo.selectAll();
        });

        // --- MENÚ DE LISTA ---
        MenuItem itemAnadirTarjeta = new MenuItem("Añadir tarjeta");
        itemAnadirTarjeta.setOnAction(e -> crearTarjeta());

        MenuItem itemEliminarLista = new MenuItem("Eliminar lista");
        itemEliminarLista.setOnAction(e -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar lista");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Estás seguro de que quieres eliminar esta lista?");
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    Task<Void> taskEliminar = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            apiClient.eliminarLista(tableroId, listaId);
                            return null;
                        }
                    };
                    taskEliminar.setOnSucceeded(ev -> {
                        Node raizLista = scroll.getParent();
                        if (raizLista != null && raizLista.getParent() instanceof Pane padre) {
                            padre.getChildren().remove(raizLista);
                        }
                    });
                    taskEliminar.setOnFailed(ev ->
                        System.err.println("Error al eliminar lista: " + taskEliminar.getException().getMessage())
                    );
                    new Thread(taskEliminar).start();
                }
            });
        });

        ContextMenu contextMenu = new ContextMenu(itemAnadirTarjeta, itemEliminarLista);
        btnMenuLista.setOnAction(e -> contextMenu.show(btnMenuLista, Side.BOTTOM, 0, 0));
    }

    /**
     * Calcula en qué índice del VBox insertar la tarjeta según la posición Y del ratón.
     * Ignora el placeholder para no contar su propia posición.
     */
    private int calcularIndice(double mouseY) {
        int index = 0;
        for (int i = 0; i < contenedorTarjetas.getChildren().size(); i++) {
            var child = contenedorTarjetas.getChildren().get(i);

            if (child == placeholder) continue;

            double childY = child.getLayoutY();
            double childHeight = child.getBoundsInParent().getHeight();
            double midY = childY + childHeight / 2.0;

            if (mouseY > midY) {
                index = i + 1;
            } else {
                break;
            }
        }
        return index;
    }

    @FXML
    void crearTarjeta() {
        // 1. Elegir tipo
        ChoiceDialog<String> tipoDialog = new ChoiceDialog<>("Tarea simple", "Tarea simple", "Con checklist");
        tipoDialog.setTitle("Nueva tarjeta");
        tipoDialog.setHeaderText(null);
        tipoDialog.setContentText("Tipo de tarjeta:");
        Optional<String> tipo = tipoDialog.showAndWait();
        if (tipo.isEmpty()) return;

        // 2. Pedir título
        TextInputDialog tituloDialog = new TextInputDialog();
        tituloDialog.setTitle("Nueva tarjeta");
        tituloDialog.setHeaderText(null);
        tituloDialog.setContentText("Título de la tarjeta:");
        Optional<String> tituloOpt = tituloDialog.showAndWait();
        if (tituloOpt.isEmpty() || tituloOpt.get().isBlank()) return;
        String tituloTexto = tituloOpt.get();

        boolean esChecklist = "Con checklist".equals(tipo.get());

        // 3. Si es checklist, pedir ítems
        List<String> items = new ArrayList<>();
        if (esChecklist) {
            while (true) {
                TextInputDialog itemDialog = new TextInputDialog();
                itemDialog.setTitle("Ítem del checklist");
                itemDialog.setHeaderText("Deja vacío para terminar");
                itemDialog.setContentText("Descripción del ítem:");
                Optional<String> itemOpt = itemDialog.showAndWait();
                if (itemOpt.isEmpty() || itemOpt.get().isBlank()) break;
                items.add(itemOpt.get());
            }
        }

        // 4. Crear en backend
        Task<TarjetaDto> task = new Task<>() {
            @Override
            protected TarjetaDto call() throws Exception {
                TarjetaDto tarjeta = apiClient.agregarTarjeta(tableroId, listaId, tituloTexto);
                if (esChecklist) {
                    ChecklistDto checklist = apiClient.crearChecklist(tableroId, listaId, tarjeta.id, "Checklist");
                    for (String desc : items) {
                        apiClient.agregarItemChecklist(tableroId, listaId, tarjeta.id, desc);
                    }
                    // Construir el DTO localmente para mostrarlo sin re-fetch
                    List<ItemChecklistDto> itemDtos = new ArrayList<>();
                    for (String desc : items) {
                        ItemChecklistDto dto = new ItemChecklistDto();
                        dto.descripcion = desc;
                        dto.completado = false;
                        itemDtos.add(dto);
                    }
                    checklist.items = itemDtos;
                    tarjeta.checklist = checklist;
                    tarjeta.tieneChecklist = true;
                }
                return tarjeta;
            }
        };

        task.setOnSucceeded(e -> mostrarTarjeta(task.getValue()));
        task.setOnFailed(e -> {
            String msg = task.getException().getMessage();
            if (msg != null && (msg.contains("409") || msg.contains("Conflict"))) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Tablero bloqueado");
                alert.setHeaderText(null);
                alert.setContentText("El tablero esta bloqueado y no admite nuevas tarjetas.");
                alert.showAndWait();
            } else {
                System.err.println("Error al crear tarjeta: " + msg);
            }
        });

        new Thread(task).start();
    }

    void mostrarTarjeta(TarjetaDto tarjeta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaTarjeta.fxml")
            );
            HBox nodoTarjeta = loader.load();
            VentanaTarjetaController controller = loader.getController();
            controller.setDatos(tableroId, listaId, tarjeta);  // también hace nodoTarjeta.setUserData(this)
            contenedorTarjetas.getChildren().add(nodoTarjeta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
