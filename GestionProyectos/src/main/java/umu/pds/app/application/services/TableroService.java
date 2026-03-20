package umu.pds.app.application.services;

import umu.pds.app.application.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.ports.output.TableroRepository;

import java.util.List;

/**
 * Caso de uso: orquesta el dominio para cumplir los requisitos de la aplicación.
 * No contiene lógica de negocio — toda la lógica vive en el dominio.
 */
public class TableroService implements GestionTableroUseCase {

    private final TableroRepository tableroRepository;

    public TableroService(TableroRepository tableroRepository) {
        this.tableroRepository = tableroRepository;
    }

    // --- Tablero ---

    @Override
    public Tablero crearTablero(String nombre, String email) {
        Tablero tablero = new Tablero(nombre, email);
        tableroRepository.guardar(tablero);
        return tablero;
    }

    @Override
    public Tablero obtenerTablero(TableroId id) {
        return tableroRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado: " + id));
    }

    @Override
    public List<Tablero> obtenerTablerosPorEmail(String email) {
        return tableroRepository.buscarPorEmail(email);
    }

    @Override
    public void renombrarTablero(TableroId id, String nuevoNombre) {
        Tablero tablero = obtenerTablero(id);
        tablero.renombrar(nuevoNombre);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void bloquearTablero(TableroId id) {
        Tablero tablero = obtenerTablero(id);
        tablero.bloquear();
        tableroRepository.guardar(tablero);
    }

    @Override
    public void desbloquearTablero(TableroId id) {
        Tablero tablero = obtenerTablero(id);
        tablero.desbloquear();
        tableroRepository.guardar(tablero);
    }

    // --- Listas ---

    @Override
    public Lista agregarLista(TableroId tableroId, String nombre) {
        Tablero tablero = obtenerTablero(tableroId);
        Lista lista = tablero.agregarLista(nombre);
        tableroRepository.guardar(tablero);
        return lista;
    }

    @Override
    public void eliminarLista(TableroId tableroId, ListaId listaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.eliminarLista(listaId);
        tableroRepository.guardar(tablero);
    }

    // --- Tarjetas ---

    @Override
    public Tarjeta agregarTarjeta(TableroId tableroId, ListaId listaId, String titulo) {
        Tablero tablero = obtenerTablero(tableroId);
        Tarjeta tarjeta = tablero.agregarTarjeta(listaId, titulo);
        tableroRepository.guardar(tablero);
        return tarjeta;
    }

    @Override
    public void eliminarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.eliminarTarjeta(listaId, tarjetaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void moverTarjeta(TableroId tableroId, TarjetaId tarjetaId, ListaId listaOrigenId, ListaId listaDestinoId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.moverTarjeta(tarjetaId, listaOrigenId, listaDestinoId);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void marcarTarjetaCompletada(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.marcarTarjetaCompletada(listaId, tarjetaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void asignarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId))
                .buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada: " + tarjetaId))
                .asignarEtiqueta(etiqueta);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void quitarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId))
                .buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada: " + tarjetaId))
                .quitarEtiqueta(etiqueta);
        tableroRepository.guardar(tablero);
    }
}
