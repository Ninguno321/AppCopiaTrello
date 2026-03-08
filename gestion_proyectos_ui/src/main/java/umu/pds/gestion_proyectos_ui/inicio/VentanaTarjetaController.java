package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;

public class VentanaTarjetaController {

    @FXML
    private HBox root;

    @FXML
    public void initialize() {
        if (root == null) {
            System.out.println("root es null! Revisa fx:id en FXML.");
            return;
        }

        root.setOnDragDetected(event -> {
            // 1. Tarjeta original se queda semitransparente (fantasma)
            root.setOpacity(0.35);

            // 2. Crear imagen del drag: snapshot rotado + semitransparente
            WritableImage dragImage = crearDragImage();

            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);

            // 3. La imagen sigue al ratón con offset centrado en el punto de agarre
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
     * Genera un WritableImage de la tarjeta inclinada ~6° y con 75% de opacidad.
     */
    private WritableImage crearDragImage() {
        // Snapshot con fondo transparente
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // Rotar el nodo temporalmente para el snapshot
        root.setRotate(6);
        WritableImage rotatedSnapshot = root.snapshot(params, null);
        root.setRotate(0);

        // Dibujar sobre un Canvas aplicando alpha para la semitransparencia
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