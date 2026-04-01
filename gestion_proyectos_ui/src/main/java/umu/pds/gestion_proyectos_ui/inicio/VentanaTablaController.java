package umu.pds.gestion_proyectos_ui.inicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import umu.pds.gestion_proyectos_ui.api.dto.EtiquetaDto;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

import java.util.stream.Collectors;

public class VentanaTablaController {

    private static final Logger log = LoggerFactory.getLogger(VentanaTablaController.class);

    @FXML private TableView<FilaTarjeta> tablaTarjetas;
    @FXML private TableColumn<FilaTarjeta, String> colTitulo;
    @FXML private TableColumn<FilaTarjeta, String> colLista;
    @FXML private TableColumn<FilaTarjeta, String> colCompletada;
    @FXML private TableColumn<FilaTarjeta, String> colTipo;
    @FXML private TableColumn<FilaTarjeta, String> colEtiquetas;

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(data -> data.getValue().titulo);
        colLista.setCellValueFactory(data -> data.getValue().lista);
        colCompletada.setCellValueFactory(data -> data.getValue().completada);
        colTipo.setCellValueFactory(data -> data.getValue().tipo);
        colEtiquetas.setCellValueFactory(data -> data.getValue().etiquetas);
    }

    /**
     * para rellenar la tabla
     */
    public void cargarDatos(TableroDto tablero) {
        log.info("Renderizando tabla - Listas normales: {} | Tarjetas completadas: {}",
                tablero.listas.size(),
                tablero.tarjetasCompletadas != null ? tablero.tarjetasCompletadas.size() : "null");
        ObservableList<FilaTarjeta> filas = FXCollections.observableArrayList();

        if (tablero.listas != null) {
            for (ListaDto lista : tablero.listas) {
                if (lista.tarjetas != null) {
                    for (TarjetaDto tarjeta : lista.tarjetas) {
                        filas.add(new FilaTarjeta(tarjeta, lista.nombre));
                    }
                }
            }
        }

        if (tablero.tarjetasCompletadas != null) {
            for (TarjetaDto tarjeta : tablero.tarjetasCompletadas) {
                filas.add(new FilaTarjeta(tarjeta, "Completadas"));
            }
        }

        tablaTarjetas.setItems(filas);
    }


    public static class FilaTarjeta {
        public final SimpleStringProperty titulo;
        public final SimpleStringProperty lista;
        public final SimpleStringProperty completada;
        public final SimpleStringProperty tipo;
        public final SimpleStringProperty etiquetas;

        public FilaTarjeta(TarjetaDto tarjeta, String nombreLista) {
            this.titulo = new SimpleStringProperty(tarjeta.titulo);
            this.lista = new SimpleStringProperty(nombreLista);
            this.completada = new SimpleStringProperty(tarjeta.completada ? "✓ Sí" : "No");

            String tipoTexto;
            if (tarjeta.tieneChecklist) {
                tipoTexto = "Checklist";
            } else if (tarjeta.tieneTarea) {
                tipoTexto = "Tarea";
            } else {
                tipoTexto = "Simple";
            }
            this.tipo = new SimpleStringProperty(tipoTexto);

            String etiquetasTexto = "";
            if (tarjeta.etiquetas != null && !tarjeta.etiquetas.isEmpty()) {
                etiquetasTexto = tarjeta.etiquetas.stream()
                        .map(e -> e.nombre)
                        .collect(Collectors.joining(", "));
            }
            this.etiquetas = new SimpleStringProperty(etiquetasTexto);
        }
    }
}
