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
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendService;
import umu.pds.gestion_proyectos_ui.services.ServiceFactory;

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
    @FXML private javafx.scene.control.Label lblMaxTarjetas;

    // Placeholder visual: línea azul que se mueve entre tarjetas.
    private HBox placeholder;

    private String tableroId;
    private String listaId;
    private TableroDto tablero;
    private VentanaTableroController tableroController;
    private final GestionTableroFrontendService service = ServiceFactory.getGestionTablero();

    public void setDatos(String tableroId, String listaId, String nombre, TableroDto tablero) {
        this.tableroId = tableroId;
        this.listaId = listaId;
        this.tablero = tablero;
        titulo.setText(nombre);

        if (tablero != null && tablero.listas != null) {
            for (ListaDto l : tablero.listas) {
                if (l.id != null && l.id.equals(listaId)) {
                    if (lblMaxTarjetas != null) {
                        lblMaxTarjetas.setText("(máx " + l.maxTarjetas + ")");
                    }
                    break;
                }
            }
        }

        if ("ESPECIAL_COMPLETADAS".equals(listaId)) {
            if (lblMaxTarjetas != null) lblMaxTarjetas.setVisible(false);
            btnAnadirTarjeta.setVisible(false);
            btnAnadirTarjeta.setManaged(false);
            btnMenuLista.setVisible(false);
            btnMenuLista.setManaged(false);
            footerLista.setVisible(false);
            footerLista.setManaged(false);
            titulo.setEditable(false);
            titulo.setStyle(
                "-fx-background-color: #27b1bf; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 4;"
            );
        }
    }

    public void setTableroController(VentanaTableroController tc) {
        this.tableroController = tc;
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

                if (tarjeta.getUserData() instanceof VentanaTarjetaController tc) {
                    String listaOrigen = tc.getListaId();
                    String tId = tc.getTarjetaId();
                    String tbId = tc.getTableroId();

                    if ("ESPECIAL_COMPLETADAS".equals(listaId)) {
                        // Soltar en lista de completadas
                        if (!Objects.equals(listaOrigen, listaId)) {
                            Task<Void> completar = service.completarTarjeta(tbId, listaOrigen, tId);
                            completar.setOnSucceeded(e -> tc.moverATarjetasCompletadas());
                            completar.setOnFailed(e -> System.err.println("Error: " + completar.getException().getMessage()));
                            new Thread(completar).start();
                        }
                    } else {
                        // Ver limite y prerequisitos
                        if (!Objects.equals(listaOrigen, listaId)) {
                            ListaDto listaDestinoDto = null;
                            if (tablero != null && tablero.listas != null) {
                                for (ListaDto l : tablero.listas) {
                                    if (l.id != null && l.id.equals(listaId)) {
                                        listaDestinoDto = l;
                                        break;
                                    }
                                }
                            }
                            
                            // Prerequisitos
                            if (listaDestinoDto != null && listaDestinoDto.listasRequeridas != null && !listaDestinoDto.listasRequeridas.isEmpty()) {
                                TarjetaDto tarjetaReal = tc.getTarjetaDto();
                                List<String> faltantes = new ArrayList<>();
                                for (String req : listaDestinoDto.listasRequeridas) {
                                    if (tarjetaReal.historialListas == null || !tarjetaReal.historialListas.contains(req)) {
                                        // Buscar nombre de la lista para el mensaje
                                        String nombreReq = req;
                                        if (tablero.listas != null) {
                                            for (ListaDto l : tablero.listas) {
                                                if (req.equals(l.id)) {
                                                    nombreReq = l.nombre;
                                                    break;
                                                }
                                            }
                                        }
                                        faltantes.add(nombreReq);
                                    }
                                }
                                
                                if (!faltantes.isEmpty()) {
                                    mostrarError("Prerequisitos no cumplidos", 
                                        "La tarjeta no puede entrar en esta lista porque no ha pasado por las siguientes listas requeridas:\n" + String.join("\n", faltantes));
                                    event.setDropCompleted(false);
                                    event.consume();
                                    
                                    if (tableroController != null) {
                                        tableroController.aplicarFiltrosCombinados();
                                    }
                                    return;
                                }
                            }
                            if (listaDestinoDto != null && listaDestinoDto.tarjetas != null && listaDestinoDto.tarjetas.size() >= listaDestinoDto.maxTarjetas) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.initOwner(contenedorTarjetas.getScene().getWindow());
                                alert.setTitle("Límite alcanzado");
                                alert.setHeaderText(null);
                                alert.setContentText("No se puede mover la tarjeta porque la lista de destino está llena (Máximo: " + listaDestinoDto.maxTarjetas + ").");
                                alert.showAndWait();
                                event.setDropCompleted(false);
                                event.consume();
                                
                                if (tableroController != null) {
                                    tableroController.aplicarFiltrosCombinados();
                                }
                                return;
                            }
                        }

                        // actualizamos en memoria ( dto ) 
                        if (tablero != null && tablero.listas != null) {
                            TarjetaDto tarjetaMovida = null;
                            
                            //buscar y quitar
                            for (ListaDto l : tablero.listas) {
                                if (l.id != null && l.id.equals(listaOrigen)) {
                                    if (l.tarjetas != null) {
                                        for (int i = 0; i < l.tarjetas.size(); i++) {
                                            if (l.tarjetas.get(i).id.equals(tId)) {
                                                tarjetaMovida = l.tarjetas.remove(i);
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            
                            // meterla en la pos correcta
                            if (tarjetaMovida != null) {
                                for (ListaDto l : tablero.listas) {
                                    if (l.id != null && l.id.equals(listaId)) {
                                        if (l.tarjetas == null) l.tarjetas = new ArrayList<>();
                                        int safeIndex = Math.min(dropIndex, l.tarjetas.size());
                                        l.tarjetas.add(safeIndex, tarjetaMovida);
                                        break;
                                    }
                                }
                            }
                        }

                        // actualizar backend
                        tc.setListaId(listaId); 

                        if (!Objects.equals(listaOrigen, listaId)) {
                            Task<Void> mover = service.moverTarjeta(tbId, tId, listaOrigen, listaId);
                            mover.setOnFailed(e -> System.err.println("Error al mover tarjeta: " + mover.getException().getMessage()));
                            new Thread(mover).start();
                        }
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

        MenuItem itemPrerequisitos = new MenuItem("Configurar prerequisitos");
        itemPrerequisitos.setOnAction(e -> configurarPrerequisitos());

        MenuItem itemEliminarLista = new MenuItem("Eliminar lista");
        itemEliminarLista.setOnAction(e -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.initOwner(scroll.getScene().getWindow());
            confirmacion.setTitle("Eliminar lista");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Estás seguro de que quieres eliminar esta lista?");
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    Task<Void> taskEliminar = service.eliminarLista(tableroId, listaId);
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

        ContextMenu contextMenu = new ContextMenu(itemAnadirTarjeta, itemPrerequisitos, itemEliminarLista);
        btnMenuLista.setOnAction(e -> contextMenu.show(btnMenuLista, Side.BOTTOM, 0, 0));
    }

    private void configurarPrerequisitos() {
        if (tablero == null || tablero.listas == null) return;
        
        ListaDto listaActualDto = null;
        for (ListaDto l : tablero.listas) {
            if (l.id != null && l.id.equals(listaId)) {
                listaActualDto = l;
                break;
            }
        }
        if (listaActualDto == null) return;

        javafx.scene.control.Dialog<List<String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.initOwner(scroll.getScene().getWindow());
        dialog.setTitle("Prerequisitos de la lista");
        dialog.setHeaderText("Selecciona las listas por las que debe pasar una tarjeta antes de entrar aquí:");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));

        List<javafx.scene.control.CheckBox> checkBoxes = new ArrayList<>();
        List<String> requeridasActuales = listaActualDto.listasRequeridas != null ? listaActualDto.listasRequeridas : new ArrayList<>();

        for (ListaDto l : tablero.listas) {
            if (l.id != null && !l.id.equals(listaId)) {
                javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox(l.nombre);
                cb.setUserData(l.id);
                if (requeridasActuales.contains(l.id)) {
                    cb.setSelected(true);
                }
                checkBoxes.add(cb);
                vbox.getChildren().add(cb);
            }
        }

        if (checkBoxes.isEmpty()) {
            vbox.getChildren().add(new javafx.scene.control.Label("No hay otras listas en el tablero."));
        }

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                List<String> seleccionadas = new ArrayList<>();
                for (javafx.scene.control.CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        seleccionadas.add((String) cb.getUserData());
                    }
                }
                return seleccionadas;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(seleccionadas -> {
            Task<Void> task = service.configurarPrerequisitos(tableroId, listaId, seleccionadas);
            task.setOnSucceeded(e -> {
                // Actualizar el DTO local
                for (ListaDto l : tablero.listas) {
                    if (l.id != null && l.id.equals(listaId)) {
                        l.listasRequeridas = seleccionadas;
                        break;
                    }
                }
            });
            task.setOnFailed(e -> mostrarError("Error", "No se pudieron configurar los prerequisitos: " + task.getException().getMessage()));
            new Thread(task).start();
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
        ListaDto listaActualDto = null;
        if (tablero != null && tablero.listas != null) {
            for (ListaDto l : tablero.listas) {
                if (l.id != null && l.id.equals(listaId)) {
                    listaActualDto = l;
                    break;
                }
            }
        }
        if (listaActualDto != null && listaActualDto.tarjetas != null && listaActualDto.tarjetas.size() >= listaActualDto.maxTarjetas) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(scroll.getScene().getWindow());
            alert.setTitle("Límite alcanzado");
            alert.setHeaderText(null);
            alert.setContentText("No se pueden añadir más tarjetas a esta lista. Límite máximo: " + listaActualDto.maxTarjetas);
            alert.showAndWait();
            return;
        }

        // 1. Elegir tipo
        ChoiceDialog<String> tipoDialog = new ChoiceDialog<>("Tarea simple", "Tarea simple", "Con checklist");
        tipoDialog.initOwner(scroll.getScene().getWindow());
        tipoDialog.setTitle("Nueva tarjeta");
        tipoDialog.setHeaderText(null);
        tipoDialog.setContentText("Tipo de tarjeta:");
        Optional<String> tipo = tipoDialog.showAndWait();
        if (tipo.isEmpty()) return;

        // 2. Pedir título
        TextInputDialog tituloDialog = new TextInputDialog();
        tituloDialog.initOwner(scroll.getScene().getWindow());
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
                itemDialog.initOwner(scroll.getScene().getWindow());
                itemDialog.setTitle("Ítem del checklist");
                itemDialog.setHeaderText("Deja vacío para terminar");
                itemDialog.setContentText("Descripción del ítem:");
                Optional<String> itemOpt = itemDialog.showAndWait();
                if (itemOpt.isEmpty() || itemOpt.get().isBlank()) break;
                items.add(itemOpt.get());
            }
        }

        // 4. Crear en backend (tarjeta + checklist + ítems en un solo Task del servicio)
        Task<TarjetaDto> task = service.crearTarjetaCompleta(tableroId, listaId, tituloTexto, esChecklist, items);

        task.setOnSucceeded(e -> {
            TarjetaDto nuevaTarjeta = task.getValue();
            
            
            if (tablero != null && tablero.listas != null) {
                for (ListaDto l : tablero.listas) {
                    if (l.id != null && l.id.equals(listaId)) {
                        if (l.tarjetas == null) l.tarjetas = new ArrayList<>();
                        l.tarjetas.add(nuevaTarjeta);
                        break;
                    }
                }
            }
            
            if (tableroController != null) {
                tableroController.aplicarFiltrosCombinados();
            } else {
                mostrarTarjeta(nuevaTarjeta); 
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
            controller.setDatos(tableroId, listaId, tarjeta, tablero);
            if (tableroController != null) {
                controller.setTableroController(this.tableroController);
            }else {
            	System.out.println("error - la lista no tiene controlador");
            }
            contenedorTarjetas.getChildren().add(nodoTarjeta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(scroll.getScene().getWindow());
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
