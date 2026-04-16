package umu.pds.gestion_proyectos_ui.services;

import javafx.concurrent.Task;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TrazaDto;

import java.util.List;

/**
 * Fachada de servicios del frontend: abstrae la comunicación HTTP con el backend.
 * Cada método devuelve un {@link Task} listo para configurar callbacks y arrancar
 * en un hilo de fondo. Ningún controlador debe conocer {@code TableroApiClient}.
 */
public interface GestionTableroFrontendService {

    // --- Tableros ---
    Task<List<TableroDto>> obtenerTablerosPorEmail(String email);
    Task<TableroDto>       obtenerTablero(String id);
    Task<TableroDto>       crearTablero(String nombre, String email);
    Task<Void>             bloquearTablero(String tableroId);
    Task<Void>             desbloquearTablero(String tableroId);
    Task<List<TrazaDto>>   obtenerHistorial(String tableroId);
    Task<Void>             renombrarTablero(String tableroId, String nuevoNombre);
    Task<Void>             eliminarTablero(String tableroId);

    // --- Listas ---
    Task<ListaDto> agregarLista(String tableroId, String nombre);
    Task<Void>     eliminarLista(String tableroId, String listaId);

    // --- Tarjetas ---
    /**
     * Crea una tarjeta y, si {@code esChecklist} es {@code true}, crea también
     * el checklist y añade todos los ítems en una sola operación de fondo.
     */
    Task<TarjetaDto> crearTarjetaCompleta(String tableroId, String listaId, String titulo,
                                           boolean esChecklist, List<String> itemsChecklist);
    Task<Void> eliminarTarjeta(String tableroId, String listaId, String tarjetaId);
    Task<Void> moverTarjeta(String tableroId, String tarjetaId, String listaOrigenId, String listaDestinoId);
    Task<Void> completarTarjeta(String tableroId, String listaId, String tarjetaId);

    // --- Checklist ---
    Task<Void> marcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice);
    Task<Void> desmarcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice);

    // --- Etiquetas ---
    Task<Void> etiquetarTarjeta(String tableroId, String listaId, String tarjetaId, String nombre, String color);
    Task<Void> desetiquetarTarjeta(String tableroId, String listaId, String tarjetaId, String nombre, String color);

    // --- Fechas ---
    Task<Void> asignarFechaVencimiento(String tableroId, String listaId, String tarjetaId, String fechaIso);
}
