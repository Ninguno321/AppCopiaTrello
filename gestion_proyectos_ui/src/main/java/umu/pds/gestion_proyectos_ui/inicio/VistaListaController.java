// VistaListaController.java
package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class VistaListaController {

    @FXML private Button boton1;
    @FXML private Button boton2;
    @FXML private HBox hbox1;
    @FXML private HBox hbox2;
    @FXML private VBox lista;
    @FXML private Text texto;
    @FXML private ScrollPane scroll;
    @FXML private TextField titulo;

    // Placeholder visual: línea azul que se mueve entre tarjetas
    private HBox placeholder;

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
        placeholder = new HBox();
        placeholder.setPrefHeight(3);
        placeholder.setMaxHeight(3);
        placeholder.setMinHeight(3);
        placeholder.setStyle("-fx-background-color: #0079BF;");
        placeholder.setMaxWidth(Double.MAX_VALUE);

        // --- DRAG & DROP (placeholder) ---
        lista.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);

                int index = calcularIndice(event.getY());
                lista.getChildren().remove(placeholder);
                int clampedIndex = Math.min(index, lista.getChildren().size());
                lista.getChildren().add(clampedIndex, placeholder);
            }
            event.consume();
        });

        lista.setOnDragDropped(event -> {
            boolean success = false;
            Object dragged = lista.getScene().getUserData();

            if (dragged instanceof HBox tarjeta) {
                int dropIndex = lista.getChildren().indexOf(placeholder);
                lista.getChildren().remove(placeholder);

                if (tarjeta.getParent() instanceof VBox origen) {
                    origen.getChildren().remove(tarjeta);
                }

                if (dropIndex < 0) dropIndex = lista.getChildren().size();
                dropIndex = Math.min(dropIndex, lista.getChildren().size());
                lista.getChildren().add(dropIndex, tarjeta);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        lista.setOnDragExited(event -> lista.getChildren().remove(placeholder));

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
    }
    /**
     * Calcula en qué índice del VBox insertar la tarjeta según la posición Y del ratón.
     * Ignora el placeholder para no contar su propia posición.
     */
    private int calcularIndice(double mouseY) {
        int index = 0;
        for (int i = 0; i < lista.getChildren().size(); i++) {
            var child = lista.getChildren().get(i);
            if (child == placeholder) continue;

            double midY = child.getLayoutY() + child.getBoundsInParent().getHeight() / 2.0;
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
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaTarea.fxml")
            );
            HBox nuevaTarjeta = loader.load();
            lista.getChildren().add(nuevaTarjeta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}