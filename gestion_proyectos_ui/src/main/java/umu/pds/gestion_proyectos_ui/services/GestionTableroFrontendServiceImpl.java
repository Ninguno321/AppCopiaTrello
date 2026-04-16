package umu.pds.gestion_proyectos_ui.services;

import javafx.concurrent.Task;
import umu.pds.gestion_proyectos_ui.api.TableroApiClient;
import umu.pds.gestion_proyectos_ui.api.dto.ChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.ItemChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TrazaDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link GestionTableroFrontendService}.
 * Es la única clase que instancia y conoce {@link TableroApiClient}.
 */
public class GestionTableroFrontendServiceImpl implements GestionTableroFrontendService {

    private final TableroApiClient apiClient = new TableroApiClient();

    // --- Tableros ---

    @Override
    public Task<List<TableroDto>> obtenerTablerosPorEmail(String email) {
        return new Task<>() {
            @Override
            protected List<TableroDto> call() throws Exception {
                return apiClient.obtenerTablerosPorEmail(email);
            }
        };
    }

    @Override
    public Task<TableroDto> obtenerTablero(String id) {
        return new Task<>() {
            @Override
            protected TableroDto call() throws Exception {
                return apiClient.obtenerTablero(id);
            }
        };
    }

    @Override
    public Task<TableroDto> crearTablero(String nombre, String email) {
        return new Task<>() {
            @Override
            protected TableroDto call() throws Exception {
                return apiClient.crearTablero(nombre, email);
            }
        };
    }

    @Override
    public Task<TableroDto> importarPlantilla(String yamlContent, String email) {
        return new Task<>() {
            @Override
            protected TableroDto call() throws Exception {
                return apiClient.importarPlantilla(yamlContent, email);
            }
        };
    }

    @Override
    public Task<Void> bloquearTablero(String tableroId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.bloquearTablero(tableroId);
                return null;
            }
        };
    }

    @Override
    public Task<Void> desbloquearTablero(String tableroId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.desbloquearTablero(tableroId);
                return null;
            }
        };
    }

    @Override
    public Task<List<TrazaDto>> obtenerHistorial(String tableroId) {
        return new Task<>() {
            @Override
            protected List<TrazaDto> call() throws Exception {
                return apiClient.obtenerHistorial(tableroId);
            }
        };
    }

    @Override
    public Task<Void> renombrarTablero(String tableroId, String nuevoNombre) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.renombrarTablero(tableroId, nuevoNombre);
                return null;
            }
        };
    }

    @Override
    public Task<Void> eliminarTablero(String tableroId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.eliminarTablero(tableroId);
                return null;
            }
        };
    }

    // --- Listas ---

    @Override
    public Task<ListaDto> agregarLista(String tableroId, String nombre) {
        return new Task<>() {
            @Override
            protected ListaDto call() throws Exception {
                return apiClient.agregarLista(tableroId, nombre);
            }
        };
    }

    @Override
    public Task<Void> eliminarLista(String tableroId, String listaId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.eliminarLista(tableroId, listaId);
                return null;
            }
        };
    }

    // --- Tarjetas ---

    @Override
    public Task<TarjetaDto> crearTarjetaCompleta(String tableroId, String listaId, String titulo,
                                                   boolean esChecklist, List<String> itemsChecklist) {
        return new Task<>() {
            @Override
            protected TarjetaDto call() throws Exception {
                TarjetaDto tarjeta = apiClient.agregarTarjeta(tableroId, listaId, titulo);
                if (esChecklist) {
                    ChecklistDto checklist = apiClient.crearChecklist(tableroId, listaId, tarjeta.id, "Checklist");
                    for (String desc : itemsChecklist) {
                        apiClient.agregarItemChecklist(tableroId, listaId, tarjeta.id, desc);
                    }
                    // Construir el DTO localmente para mostrarlo sin re-fetch
                    List<ItemChecklistDto> itemDtos = new ArrayList<>();
                    for (String desc : itemsChecklist) {
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
    }

    @Override
    public Task<Void> eliminarTarjeta(String tableroId, String listaId, String tarjetaId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.eliminarTarjeta(tableroId, listaId, tarjetaId);
                return null;
            }
        };
    }

    @Override
    public Task<Void> moverTarjeta(String tableroId, String tarjetaId,
                                    String listaOrigenId, String listaDestinoId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.moverTarjeta(tableroId, tarjetaId, listaOrigenId, listaDestinoId);
                return null;
            }
        };
    }

    @Override
    public Task<Void> completarTarjeta(String tableroId, String listaId, String tarjetaId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.completarTarjeta(tableroId, listaId, tarjetaId);
                return null;
            }
        };
    }

    // --- Checklist ---

    @Override
    public Task<Void> marcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.marcarItemChecklist(tableroId, listaId, tarjetaId, indice);
                return null;
            }
        };
    }

    @Override
    public Task<Void> desmarcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.desmarcarItemChecklist(tableroId, listaId, tarjetaId, indice);
                return null;
            }
        };
    }

    // --- Etiquetas ---

    @Override
    public Task<Void> etiquetarTarjeta(String tableroId, String listaId, String tarjetaId,
                                        String nombre, String color) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.etiquetarTarjeta(tableroId, listaId, tarjetaId, nombre, color);
                return null;
            }
        };
    }

    @Override
    public Task<Void> desetiquetarTarjeta(String tableroId, String listaId, String tarjetaId,
                                           String nombre, String color) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.desetiquetarTarjeta(tableroId, listaId, tarjetaId, nombre, color);
                return null;
            }
        };
    }

    // --- Fechas ---

    @Override
    public Task<Void> asignarFechaVencimiento(String tableroId, String listaId,
                                               String tarjetaId, String fechaIso) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.asignarFechaVencimiento(tableroId, listaId, tarjetaId, fechaIso);
                return null;
            }
        };
    }
}
