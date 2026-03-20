package umu.pds.gestion_proyectos_ui.inicio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/umu/pds/gestion_proyectos_ui/inicio/VentanaTarjeta.fxml")
            );
            HBox nuevaTarjeta = loader.load();
            contenedorTarjetas.getChildren().add(nuevaTarjeta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
