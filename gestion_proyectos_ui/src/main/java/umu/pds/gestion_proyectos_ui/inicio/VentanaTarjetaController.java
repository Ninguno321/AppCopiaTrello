package umu.pds.gestion_proyectos_ui.inicio;

import java.time.LocalDate;
import java.util.Optional;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.Text;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.EtiquetaDto;
import umu.pds.gestion_proyectos_ui.api.dto.ItemChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

public class VentanaTarjetaController {

    @FXML private HBox root;
    @FXML private CheckBox check;
    @FXML private Text tituloTarjeta;
    @FXML private VBox contenedorItems;
    @FXML private FlowPane contenedorEtiquetas;
    @FXML private Button btnAnadirEtiqueta;
    @FXML private DatePicker datePickerVencimiento;

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
            tituloTarjeta.setStyle("-fx-strikethrough: true;");
            root.setOpacity(0.5);
        }

        check.setOnAction(e -> onToggleCompletada());

        // Mostrar ítems del checklist si los hay
        if (tarjeta.tieneChecklist && tarjeta.checklist != null && tarjeta.checklist.items != null) {
            contenedorItems.setVisible(true);
            contenedorItems.setManaged(true);
            for (int i = 0; i < tarjeta.checklist.items.size(); i++) {
                ItemChecklistDto item = tarjeta.checklist.items.get(i);
                agregarItemUI(item.descripcion, item.completado, i);
            }
        }
        
        // --- Pintar etiquetas existentes ---
        contenedorEtiquetas.getChildren().clear();
        if (tarjeta.etiquetas != null) {
            for (EtiquetaDto etiq : tarjeta.etiquetas) {
                agregarPastillaEtiqueta(etiq.nombre, etiq.color);
            }
        }

        // --- Fecha de vencimiento ---
        if (tarjeta.fechaVencimiento != null && tarjeta.fechaVencimiento.length() >= 10) {
            try {
                datePickerVencimiento.setValue(LocalDate.parse(tarjeta.fechaVencimiento.substring(0, 10)));
            } catch (Exception e) {
                System.err.println("Error al parsear fechaVencimiento: " + e.getMessage());
            }
        }

        datePickerVencimiento.setOnAction(e -> {
            LocalDate fecha = datePickerVencimiento.getValue();
            if (fecha == null) return;
            // El backend espera un campo "fecha" con formato ISO LocalDateTime
            String fechaIso = fecha.atStartOfDay().toString();
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.asignarFechaVencimiento(tableroId, listaId, tarjeta.id, fechaIso);
                    return null;
                }
            };
            t.setOnFailed(ev -> System.err.println("Error al asignar fecha de vencimiento: " + t.getException().getMessage()));
            new Thread(t).start();
        });
    }

    // --- Getters y setter para drag & drop ---

    public String getTarjetaId() { return tarjeta != null ? tarjeta.id : null; }
    public String getListaId()   { return listaId; }
    public String getTableroId() { return tableroId; }
    public void   setListaId(String listaId) { this.listaId = listaId; }

    // --- Lógica de marcar/desmarcar tarjeta ---

    private void onToggleCompletada() {
        if (check.isSelected()) {
            tituloTarjeta.setStyle("-fx-strikethrough: true;");
            root.setOpacity(0.5);
        } else {
            tituloTarjeta.setStyle("");
            root.setOpacity(1.0);
        }
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
    
 // --- Lógica de Etiquetas ---

    private void agregarPastillaEtiqueta(String nombre, String colorHex) {
        Label pastilla = new Label(nombre);
        // Estilo de la pastilla: fondo del color elegido, texto blanco, bordes redondeados
        pastilla.setStyle(
            "-fx-background-color: " + colorHex + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 2 6 2 6; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold;"
        );
        
        // Clic para eliminarla
        pastilla.setOnMouseClicked(e -> {
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.desetiquetarTarjeta(tableroId, listaId, tarjeta.id, nombre, colorHex);
                    return null;
                }
            };
            t.setOnSucceeded(ev -> contenedorEtiquetas.getChildren().remove(pastilla));
            t.setOnFailed(ev -> System.err.println("Error al borrar etiqueta"));
            new Thread(t).start();
        });
        
        contenedorEtiquetas.getChildren().add(pastilla);
    }

    @FXML
    void onAnadirEtiquetaClick() {
        // Crear un diálogo personalizado con nombre y color
        Dialog<EtiquetaDto> dialog = new Dialog<>();
        dialog.setTitle("Nueva Etiqueta");
        dialog.setHeaderText("Configura tu etiqueta");

        ButtonType guardarButtonType = new ButtonType("Guardar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreEtiqueta = new TextField();
        nombreEtiqueta.setPromptText("Ej: Urgente");
        ColorPicker colorPicker = new ColorPicker(Color.web("#e01e5a")); // Rojo por defecto

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreEtiqueta, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                EtiquetaDto dto = new EtiquetaDto();
                dto.nombre = nombreEtiqueta.getText();
                // Convertir Color de JavaFX a HEX (ej: #FF0000)
                dto.color = String.format("#%02X%02X%02X",
                        (int)(colorPicker.getValue().getRed() * 255),
                        (int)(colorPicker.getValue().getGreen() * 255),
                        (int)(colorPicker.getValue().getBlue() * 255));
                return dto;
            }
            return null;
        });

        Optional<EtiquetaDto> result = dialog.showAndWait();

        result.ifPresent(etiqueta -> {
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.etiquetarTarjeta(tableroId, listaId, tarjeta.id, etiqueta.nombre, etiqueta.color);
                    return null;
                }
            };
            t.setOnSucceeded(e -> agregarPastillaEtiqueta(etiqueta.nombre, etiqueta.color));
            t.setOnFailed(e -> System.err.println("Error al añadir etiqueta"));
            new Thread(t).start();
        });
    }
    
}
