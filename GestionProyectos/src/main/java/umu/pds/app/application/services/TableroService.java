package umu.pds.app.application.services;

import umu.pds.app.application.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.modelo.tablero.Checklist;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.modelo.usuario.Usuario;
import umu.pds.app.domain.ports.output.TableroRepository;
import umu.pds.app.domain.ports.output.UsuarioRepository;

import java.util.List;

/**
 * Caso de uso: orquesta el dominio para cumplir los requisitos de la aplicación.
 * No contiene lógica de negocio — toda la lógica vive en el dominio.
 */
public class TableroService implements GestionTableroUseCase {

    private final TableroRepository tableroRepository;
    private final UsuarioRepository usuarioRepository;

    public TableroService(TableroRepository tableroRepository, UsuarioRepository usuarioRepository) {
        this.tableroRepository = tableroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // --- Tablero ---

    @Override
    public Tablero crearTablero(String nombre, String email) {
        Usuario propietario = usuarioRepository.buscarPorEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario(email);
                    usuarioRepository.guardar(nuevo);
                    return nuevo;
                });
        Tablero tablero = new Tablero(nombre, propietario);
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
        Lista lista = tablero.agregarLista(Lista.nueva(nombre));
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
        try {
            Tarjeta tarjeta = tablero.agregarTarjeta(listaId, Tarjeta.nueva(titulo));
            tableroRepository.guardar(tablero);
            return tarjeta;
        } catch (TableroException e) {
            throw new IllegalStateException(e.getMessage());
        }
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
        tablero.completarTarjeta(tarjetaId, listaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void asignarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        etiquetarTarjeta(tableroId, listaId, tarjetaId, etiqueta);
    }

    @Override
    public void quitarEtiqueta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        desetiquetarTarjeta(tableroId, listaId, tarjetaId, etiqueta);
    }

    @Override
    public void etiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.etiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void desetiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.desetiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    // --- Checklist ---

    @Override
    public Checklist asignarChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String nombre) {
        Tablero tablero = obtenerTablero(tableroId);
        Checklist checklist = tablero.asignarChecklist(listaId, tarjetaId, nombre);
        tableroRepository.guardar(tablero);
        return checklist;
    }

    @Override
    public void agregarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String descripcion) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.agregarItemChecklist(listaId, tarjetaId, descripcion);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void marcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.marcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }

    @Override
    public void desmarcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.desmarcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }
}
