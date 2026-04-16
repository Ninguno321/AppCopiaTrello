package umu.pds.gestion_proyectos_ui.inicio;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendService;
import umu.pds.gestion_proyectos_ui.services.GestionTableroFrontendServiceImpl;
import javafx.scene.Node;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentanaPrincipalController {

    // Sidebar
    @FXML private VBox sidebarTableros;
    @FXML private Button btnCrearTablero;
    @FXML private Button btnImportarPlantilla;
    @FXML private Button btnConfiguracion;

    // Cabecera del contenido central
    @FXML private Label  lblTableroActual;
    @FXML private Button btnAutomatizaciones;

    // Pestañas de navegación
    @FXML private Button btnTabTablero;
    @FXML private Button btnTabCalendario;
    @FXML private Button btnTabTabla;

    // Área de contenido intercambiable
    @FXML private StackPane mainContentPane;

    private List<Button> tabButtons;
    private final List<HBox> sidebarItems = new ArrayList<>();
    private final List<TableroDto> listaTableros = new ArrayList<>();
    private TableroDto tableroActual;
    private String emailActual;
    private final GestionTableroFrontendService service = new GestionTableroFrontendServiceImpl();

    @FXML
    public void initialize() {
        tabButtons = List.of(btnTabTablero, btnTabCalendario, btnTabTabla);
    }

    /**
     * Recibe el email del usuario y su lista de tableros desde VentanaInicioController.
     * Puebla el sidebar y carga el primer tablero si existe.
     */
    public void setDatosUsuario(String email, List<TableroDto> tableros) {
        this.emailActual = email;
        for (TableroDto tablero : tableros) {
            agregarItemSidebar(tablero);
        }
        if (!tableros.isEmpty()) {
            tableroActual = tableros.get(0);
            lblTableroActual.setText(tableroActual.nombre);
            cargarVistaTablero();
        }
    }

    // --- Sidebar ---

    private void agregarItemSidebar(TableroDto tablero) {
        // Botón con el nombre, ocupa todo el espacio disponible
        Button btnNombre = new Button(tablero.nombre);
        btnNombre.setMaxWidth(Double.MAX_VALUE);
        btnNombre.getStyleClass().add("sidebar-item-btn");
        HBox.setHgrow(btnNombre, Priority.ALWAYS);

        // MenuButton "⋮" con las acciones del tablero
        MenuButton menuBtn = new MenuButton("⋮");
        menuBtn.getStyleClass().add("sidebar-menu-btn");

        MenuItem itemRenombrar = new MenuItem("Editar nombre");
        MenuItem itemEliminar  = new MenuItem("Eliminar tablero");
        menuBtn.getItems().addAll(itemRenombrar, itemEliminar);

        HBox hbox = new HBox(btnNombre, menuBtn);
        hbox.getStyleClass().add("sidebar-item");

        // Clic en el nombre → abrir tablero
        btnNombre.setOnAction(e -> {
            tableroActual = tablero;
            lblTableroActual.setText(tablero.nombre);
            activarSidebarItem(hbox);
            cargarVistaTablero();
        });

        // Editar nombre
        itemRenombrar.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(tablero.nombre);
            dialog.initOwner(hbox.getScene().getWindow());
            dialog.setTitle("Renombrar tablero");
            dialog.setHeaderText(null);
            dialog.setContentText("Nuevo nombre:");

            Optional<String> resultado = dialog.showAndWait();
            resultado.ifPresent(nuevoNombre -> {
                if (nuevoNombre.isBlank()) return;

                Task<Void> task = service.renombrarTablero(tablero.id, nuevoNombre);

                task.setOnSucceeded(ev -> {
                    tablero.nombre = nuevoNombre;
                    btnNombre.setText(nuevoNombre);
                    if (tableroActual != null && tableroActual.id.equals(tablero.id)) {
                        lblTableroActual.setText(nuevoNombre);
                    }
                });

                task.setOnFailed(ev -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(hbox.getScene().getWindow());
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error al renombrar: " + task.getException().getMessage());
                    alert.showAndWait();
                });

                new Thread(task).start();
            });
        });

        // Eliminar tablero
        itemEliminar.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(hbox.getScene().getWindow());
            confirm.setTitle("Eliminar tablero");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Eliminar \"" + tablero.nombre + "\"? Esta acción no se puede deshacer.");

            confirm.showAndWait().ifPresent(respuesta -> {
                if (respuesta != ButtonType.OK) return;

                Task<Void> task = service.eliminarTablero(tablero.id);

                task.setOnSucceeded(ev -> {
                    int indice = sidebarItems.indexOf(hbox);
                    sidebarItems.remove(hbox);
                    listaTableros.remove(tablero);
                    sidebarTableros.getChildren().remove(hbox);

                    if (tableroActual != null && tableroActual.id.equals(tablero.id)) {
                        if (!sidebarItems.isEmpty()) {
                            // Seleccionar el elemento adyacente más cercano
                            int siguienteIndice = Math.min(indice, sidebarItems.size() - 1);
                            HBox siguiente = sidebarItems.get(siguienteIndice);
                            TableroDto siguienteTablero = listaTableros.get(siguienteIndice);
                            tableroActual = siguienteTablero;
                            lblTableroActual.setText(siguienteTablero.nombre);
                            activarSidebarItem(siguiente);
                            cargarVistaTablero();
                        } else {
                            tableroActual = null;
                            lblTableroActual.setText("");
                            mainContentPane.getChildren().clear();
                        }
                    }
                });

                task.setOnFailed(ev -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(hbox.getScene().getWindow());
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error al eliminar: " + task.getException().getMessage());
                    alert.showAndWait();
                });

                new Thread(task).start();
            });
        });

        listaTableros.add(tablero);
        sidebarItems.add(hbox);
        sidebarTableros.getChildren().add(hbox);
        activarSidebarItem(hbox);
    }

    private void activarSidebarItem(HBox activo) {
        for (HBox item : sidebarItems) {
            item.getStyleClass().remove("active");
        }
        if (!activo.getStyleClass().contains("active")) {
            activo.getStyleClass().add("active");
        }
    }

    @FXML
    void onCrearTablero(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(btnCrearTablero.getScene().getWindow());
        dialog.setTitle("Nuevo tablero");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre del tablero:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (nombre.isBlank()) return;

            Task<TableroDto> task = service.crearTablero(nombre, emailActual);

            task.setOnSucceeded(e -> {
                TableroDto nuevoTablero = task.getValue();
                agregarItemSidebar(nuevoTablero);
                tableroActual = nuevoTablero;
                lblTableroActual.setText(nuevoTablero.nombre);
                cargarVistaTablero();
            });

            task.setOnFailed(e -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(btnCrearTablero.getScene().getWindow());
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error al crear tablero: " + task.getException().getMessage());
                alert.showAndWait();
            });

            new Thread(task).start();
        });
    }

    @FXML
    void onImportarPlantillaClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar plantilla YAML");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos YAML (*.yaml, *.yml)", "*.yaml", "*.yml")
        );

        File archivo = fileChooser.showOpenDialog(btnImportarPlantilla.getScene().getWindow());
        if (archivo == null) return;

        String yamlContent;
        try {
            yamlContent = Files.readString(archivo.toPath());
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(btnImportarPlantilla.getScene().getWindow());
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo leer el archivo: " + ex.getMessage());
            alert.showAndWait();
            return;
        }

        Task<TableroDto> task = service.importarPlantilla(yamlContent, emailActual);

        task.setOnSucceeded(e -> {
            TableroDto nuevoTablero = task.getValue();
            agregarItemSidebar(nuevoTablero);
            tableroActual = nuevoTablero;
            lblTableroActual.setText(nuevoTablero.nombre);
            cargarVistaTablero();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(btnImportarPlantilla.getScene().getWindow());
            alert.setTitle("Error al importar plantilla");
            alert.setHeaderText(null);
            alert.setContentText("El formato YAML es inválido o hubo un error en el servidor:\n"
                    + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    @FXML
    void onConfiguracion(ActionEvent event) {
        // TODO: navegar a la pantalla de configuración
    }

    // --- Cabecera ---

    @FXML
    void onAutomatizaciones(ActionEvent event) {
        // TODO: abrir sección de automatizaciones del tablero actual
    }

    // --- Pestañas de navegación ---

    @FXML
    void onTabTablero(ActionEvent event) {
        activarTab(btnTabTablero);
        cargarVistaTablero();
    }

    @FXML
    void onTabCalendario(ActionEvent event) {
        activarTab(btnTabCalendario);
        cargarVistaCalendario();
    }

    @FXML
    void onTabTabla(ActionEvent event) {
        activarTab(btnTabTabla);
        cargarVistaTabla();
    }

    // --- Helpers ---

    private void cargarVistaTabla() {
        if (tableroActual == null) return;

        Task<TableroDto> task = service.obtenerTablero(tableroActual.id);

        task.setOnSucceeded(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/umu/pds/gestion_proyectos_ui/inicio/VentanaTabla.fxml"
                ));
                Node vista = loader.load();
                VentanaTablaController controller = loader.getController();
                controller.cargarDatos(task.getValue());
                mainContentPane.getChildren().setAll(vista);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Error al obtener tablero: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void cargarVistaCalendario() {
        if (tableroActual == null) return;

        Task<TableroDto> task = service.obtenerTablero(tableroActual.id);

        task.setOnSucceeded(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/umu/pds/gestion_proyectos_ui/inicio/VentanaCalendario.fxml"
                ));
                Node vista = loader.load();
                VentanaCalendarioController controller = loader.getController();
                controller.setDatos(task.getValue());
                mainContentPane.getChildren().setAll(vista);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Error al obtener tablero para calendario: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void cargarVistaTablero() {
        if (tableroActual == null) return;

        Task<TableroDto> task = service.obtenerTablero(tableroActual.id);

        task.setOnSucceeded(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/umu/pds/gestion_proyectos_ui/inicio/VentanaTablero.fxml"
                ));
                Node vista = loader.load();
                VentanaTableroController controller = loader.getController();
                controller.cargarDatos(task.getValue());
                mainContentPane.getChildren().setAll(vista);
                activarTab(btnTabTablero);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Error al obtener tablero: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void activarTab(Button tabActivo) {
        for (Button tab : tabButtons) {
            tab.getStyleClass().remove("active");
        }
        if (!tabActivo.getStyleClass().contains("active")) {
            tabActivo.getStyleClass().add("active");
        }
    }
}
