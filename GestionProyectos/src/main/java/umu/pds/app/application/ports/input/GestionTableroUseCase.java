package umu.pds.app.application.ports.input;

import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Checklist;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;

import java.util.List;

/**
 * Puerto de entrada: contrato que expone el sistema al exterior.
 * Los adaptadores (REST, UI) dependen de esta interfaz, nunca de la implementación.
 */
public interface GestionTableroUseCase {

    // --- Tablero ---

    Tablero crearTablero(String nombre, String email);

    Tablero obtenerTablero(TableroId id);

    List<Tablero> obtenerTablerosPorEmail(String email);

    void renombrarTablero(TableroId id, String nuevoNombre);

    void bloquearTablero(TableroId id);

    void desbloquearTablero(TableroId id);

    // --- Listas ---

    Lista agregarLista(TableroId tableroId, String nombre);

    void eliminarLista(TableroId tableroId, ListaId listaId);

    // --- Tarjetas ---

    Tarjeta agregarTarjeta(TableroId tableroId, ListaId listaId, String titulo);

    void eliminarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId);

    void moverTarjeta(TableroId tableroId, TarjetaId tarjetaId, ListaId listaOrigenId, ListaId listaDestinoId);

    void marcarTarjetaCompletada(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId);

    void asignarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta);

    void quitarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta);

    void etiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta);

    void desetiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta);

    // --- Checklist ---

    Checklist asignarChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String nombre);

    void agregarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String descripcion);

    void marcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice);

    void desmarcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice);
}
