package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.Text;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.ItemChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

public class VentanaTarjetaController {

    @FXML private HBox root;
    @FXML private CheckBox check;
    @FXML private Text tituloTarjeta;
    @FXML private VBox contenedorItems;

    private String tableroId;
    private String listaId;
    private TarjetaDto tarjeta;

    private final TableroApiClient apiClient = new TableroApiClient();

    public void setDatos(String tableroId, String listaId, TarjetaDto tarjeta) {
        this.tableroId = tableroId;
        this.listaId = listaId;
        this.tarjeta = tarjeta;
        tituloTarjeta.setText(tarjeta.titulo);
        root.setUserData(this);

        // Estado inicial si ya está completada
        if (tarjeta.completada) {
            check.setSelected(true);
            check.setDisable(true);
            tituloTarjeta.setStyle("-fx-strikethrough: true;");
        }

        check.setOnAction(e -> onCompletar());

        // Mostrar ítems del checklist si los hay
        if (tarjeta.tieneChecklist && tarjeta.checklist != null && tarjeta.checklist.items != null) {
            contenedorItems.setVisible(true);
            contenedorItems.setManaged(true);
            for (int i = 0; i < tarjeta.checklist.items.size(); i++) {
                ItemChecklistDto item = tarjeta.checklist.items.get(i);
                agregarItemUI(item.descripcion, item.completado, i);
            }
        }
    }

    // --- Getters y setter para drag & drop ---

    public String getTarjetaId() { return tarjeta != null ? tarjeta.id : null; }
    public String getListaId()   { return listaId; }
    public String getTableroId() { return tableroId; }
    public void   setListaId(String listaId) { this.listaId = listaId; }

    // --- Lógica de completar tarjeta ---

    private void onCompletar() {
        tituloTarjeta.setStyle("-fx-strikethrough: true;");
        check.setDisable(true);

        Task<Void> t = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.completarTarjeta(tableroId, listaId, tarjeta.id);
                return null;
            }
        };
        t.setOnSucceeded(ev -> {
            if (root.getParent() instanceof VBox parent) {
                parent.getChildren().remove(root);
            }
        });
        t.setOnFailed(ev -> {
            check.setSelected(false);
            check.setDisable(false);
            tituloTarjeta.setStyle("");
            System.err.println("Error al completar tarjeta: " + t.getException().getMessage());
        });
        new Thread(t).start();
    }

    // --- Ítems del checklist ---

    private void agregarItemUI(String descripcion, boolean completado, int indice) {
        CheckBox itemCheck = new CheckBox(descripcion);
        itemCheck.setSelected(completado);

        itemCheck.setOnAction(e -> {
            boolean marcado = itemCheck.isSelected();
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (marcado) {
                        apiClient.marcarItemChecklist(tableroId, listaId, tarjeta.id, indice);
                    } else {
                        apiClient.desmarcarItemChecklist(tableroId, listaId, tarjeta.id, indice);
                    }
                    return null;
                }
            };
            t.setOnFailed(ev -> {
                itemCheck.setSelected(!marcado);
                System.err.println("Error al actualizar ítem: " + t.getException().getMessage());
            });
            new Thread(t).start();
        });

        contenedorItems.getChildren().add(itemCheck);
    }

    // --- Drag & drop ---

    @FXML
    public void initialize() {
        if (root == null) {
            System.out.println("root es null! Revisa fx:id en FXML.");
            return;
        }

        root.setOnDragDetected(event -> {
            root.setOpacity(0.35);

            WritableImage dragImage = crearDragImage();

            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(dragImage, event.getX(), event.getY());

            ClipboardContent content = new ClipboardContent();
            content.putString("tarjeta");
            db.setContent(content);

            root.getScene().setUserData(root);
            event.consume();
        });

        root.setOnDragDone(event -> {
            root.setOpacity(1.0);
            if (root.getScene() != null) {
                root.getScene().setUserData(null);
            }
            event.consume();
        });
    }

    /**
     * Genera un WritableImage de la tarjeta inclinada ~6° y con 85% de opacidad.
     */
    private WritableImage crearDragImage() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        root.setRotate(6);
        WritableImage rotatedSnapshot = root.snapshot(params, null);
        root.setRotate(0);

        double w = rotatedSnapshot.getWidth();
        double h = rotatedSnapshot.getHeight();

        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setGlobalAlpha(0.85);
        gc.drawImage(rotatedSnapshot, 0, 0);

        SnapshotParameters canvasParams = new SnapshotParameters();
        canvasParams.setFill(Color.TRANSPARENT);
        return canvas.snapshot(canvasParams, null);
    }
}
