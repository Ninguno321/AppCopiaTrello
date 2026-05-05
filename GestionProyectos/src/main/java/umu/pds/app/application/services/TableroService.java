package umu.pds.app.application.services;

import org.springframework.transaction.annotation.Transactional;
import umu.pds.app.application.commands.PlantillaEtiquetaCommand;
import umu.pds.app.application.commands.PlantillaItemChecklistCommand;
import umu.pds.app.application.commands.PlantillaListaCommand;
import umu.pds.app.application.commands.PlantillaTableroCommand;
import umu.pds.app.application.commands.PlantillaTarjetaCommand;
import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Checklist;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.FechaLimite;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.modelo.usuario.Usuario;
import umu.pds.app.domain.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.ports.output.TableroRepository;
import umu.pds.app.domain.ports.output.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @Transactional
    public Tablero crearTablero(String nombre, String email) {
        return crearNuevoTablero(nombre, email);
    }

    @Override
    @Transactional(readOnly = true)
    public Tablero obtenerTablero(TableroId id) {
        return cargarTablero(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tablero> obtenerTablerosPorEmail(String email) {
        return tableroRepository.buscarPorEmail(email);
    }

    @Override
    @Transactional
    public void renombrarTablero(TableroId id, String nuevoNombre) {
        Tablero tablero = cargarTablero(id);
        tablero.renombrar(nuevoNombre);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void bloquearTablero(TableroId id) {
        Tablero tablero = cargarTablero(id);
        tablero.bloquear();
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desbloquearTablero(TableroId id) {
        Tablero tablero = cargarTablero(id);
        tablero.desbloquear();
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void eliminarTablero(TableroId id) {
        tableroRepository.eliminar(id);
    }

    // --- Listas ---

    @Override
    @Transactional
    public Lista agregarLista(TableroId tableroId, String nombre, int maxTarjetas) {
        Tablero tablero = cargarTablero(tableroId);
        Lista lista = tablero.agregarLista(Lista.nueva(nombre, maxTarjetas));
        tableroRepository.guardar(tablero);
        return lista;
    }

    @Override
    @Transactional
    public void eliminarLista(TableroId tableroId, ListaId listaId) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.eliminarLista(listaId);
        tableroRepository.guardar(tablero);
    }

    // --- Tarjetas ---

    @Override
    @Transactional
    public Tarjeta agregarTarjeta(TableroId tableroId, ListaId listaId, String titulo) {
        Tablero tablero = cargarTablero(tableroId);
        try {
            Tarjeta tarjeta = tablero.agregarTarjeta(listaId, Tarjeta.nueva(titulo));
            tableroRepository.guardar(tablero);
            return tarjeta;
        } catch (TableroException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.eliminarTarjeta(listaId, tarjetaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void moverTarjeta(TableroId tableroId, TarjetaId tarjetaId, ListaId listaOrigenId, ListaId listaDestinoId) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.moverTarjeta(tarjetaId, listaOrigenId, listaDestinoId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void marcarTarjetaCompletada(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.completarTarjeta(tarjetaId, listaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void etiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.etiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desetiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.desetiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    // --- Checklist ---

    @Override
    @Transactional
    public Checklist asignarChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String nombre) {
        Tablero tablero = cargarTablero(tableroId);
        Checklist checklist = tablero.asignarChecklist(listaId, tarjetaId, nombre);
        tableroRepository.guardar(tablero);
        return checklist;
    }

    @Override
    @Transactional
    public void agregarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String descripcion) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.agregarItemChecklist(listaId, tarjetaId, descripcion);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void marcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.marcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desmarcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.desmarcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }

    // --- Fecha Límite ---

    @Override
    @Transactional
    public void asignarFechaLimite(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, FechaLimite fecha) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.asignarFechaLimiteTarjeta(tarjetaId, listaId, fecha);
        tableroRepository.guardar(tablero);
    }

    // --- Prerequisitos ---

    @Override
    @Transactional
    public void configurarListasRequeridas(TableroId tableroId, ListaId listaId, List<ListaId> listasRequeridas) {
        Tablero tablero = cargarTablero(tableroId);
        tablero.configurarListasRequeridas(listaId, listasRequeridas);
        tableroRepository.guardar(tablero);
    }

    // --- Importación desde plantilla ---

    @Override
    @Transactional
    public Tablero crearDesdePlantilla(PlantillaTableroCommand plantilla, String email) {
        Tablero tablero = crearNuevoTablero(plantilla.nombre(), email);
        agregarListasDesdeCmd(tablero, plantilla.listas());
        configurarPrerequisitosDesdeCmd(tablero, plantilla.listas());
        tableroRepository.guardar(tablero);
        return tablero;
    }

    // --- Helpers de plantilla: primera pasada (listas y tarjetas) ---

    private void agregarListasDesdeCmd(Tablero tablero, List<PlantillaListaCommand> listasCmd) {
        if (listasCmd == null) return;
        for (PlantillaListaCommand listaCmd : listasCmd) {
            agregarListaDesdeCmd(tablero, listaCmd);
        }
    }

    private void agregarListaDesdeCmd(Tablero tablero, PlantillaListaCommand listaCmd) {
        int maxTarjetas = calcularMaxTarjetas(listaCmd);
        Lista lista = tablero.agregarLista(Lista.nueva(listaCmd.nombre(), maxTarjetas));
        if (listaCmd.tarjetas() != null) {
            agregarTarjetasALista(tablero, lista, listaCmd.tarjetas());
        }
    }

    private int calcularMaxTarjetas(PlantillaListaCommand listaCmd) {
        int max = listaCmd.maxTarjetas() != null ? listaCmd.maxTarjetas() : 5;
        if (listaCmd.tarjetas() != null && listaCmd.tarjetas().size() > max) {
            max = listaCmd.tarjetas().size();
        }
        return max;
    }

    private void agregarTarjetasALista(Tablero tablero, Lista lista, List<PlantillaTarjetaCommand> tarjetasCmds) {
        for (PlantillaTarjetaCommand tarjetaCmd : tarjetasCmds) {
            try {
                Tarjeta tarjeta = crearTarjetaDesdeCmd(tarjetaCmd);
                tablero.agregarTarjeta(lista.getId(), tarjeta);
            } catch (RuntimeException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    private Tarjeta crearTarjetaDesdeCmd(PlantillaTarjetaCommand tarjetaCmd) {
        Tarjeta tarjeta = Tarjeta.nueva(tarjetaCmd.titulo());
        if (tarjetaCmd.checklist() != null && !tarjetaCmd.checklist().isEmpty()) {
            tarjeta.asignarChecklist(construirChecklist(tarjetaCmd));
        }
        if (tarjetaCmd.fechaLimite() != null) {
            tarjeta.asignarFechaLimite(FechaLimite.de(tarjetaCmd.fechaLimite()));
        }
        if (tarjetaCmd.etiquetas() != null) {
            for (PlantillaEtiquetaCommand etiquetaCmd : tarjetaCmd.etiquetas()) {
                tarjeta.agregarEtiqueta(Etiqueta.de(etiquetaCmd.nombre(), etiquetaCmd.color()));
            }
        }
        return tarjeta;
    }

    private Checklist construirChecklist(PlantillaTarjetaCommand tarjetaCmd) {
        Checklist checklist = Checklist.nuevo(tarjetaCmd.titulo());
        List<PlantillaItemChecklistCommand> items = tarjetaCmd.checklist();
        for (int i = 0; i < items.size(); i++) {
            PlantillaItemChecklistCommand itemCmd = items.get(i);
            checklist.agregarItem(itemCmd.descripcion());
            if (itemCmd.completado()) {
                checklist.marcarItem(i);
            }
        }
        return checklist;
    }

    // --- Helpers de plantilla: segunda pasada (prerequisitos) ---

    private void configurarPrerequisitosDesdeCmd(Tablero tablero, List<PlantillaListaCommand> listasCmd) {
        if (listasCmd == null) return;
        for (PlantillaListaCommand listaCmd : listasCmd) {
            if (listaCmd.listasRequeridas() != null && !listaCmd.listasRequeridas().isEmpty()) {
                configurarPrerequisitosLista(tablero, listaCmd);
            }
        }
    }

    private void configurarPrerequisitosLista(Tablero tablero, PlantillaListaCommand listaCmd) {
        Optional<Lista> listaActual = buscarListaPorNombre(tablero, listaCmd.nombre());
        if (listaActual.isEmpty()) return;
        List<ListaId> requeridasIds = resolverIdsListasRequeridas(tablero, listaCmd.listasRequeridas());
        try {
            tablero.configurarListasRequeridas(listaActual.get().getId(), requeridasIds);
        } catch (TableroException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private List<ListaId> resolverIdsListasRequeridas(Tablero tablero, List<String> nombresRequeridos) {
        List<ListaId> ids = new ArrayList<>();
        for (String nombreReq : nombresRequeridos) {
            buscarListaPorNombre(tablero, nombreReq)
                    .map(Lista::getId)
                    .ifPresent(ids::add);
        }
        return ids;
    }

    private Optional<Lista> buscarListaPorNombre(Tablero tablero, String nombre) {
        return tablero.getListas().stream()
                .filter(l -> l.getNombre().equals(nombre))
                .findFirst();
    }

    // --- Helpers de infraestructura (evitan self-calls transaccionales) ---

    private Tablero cargarTablero(TableroId id) {
        return tableroRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado: " + id));
    }

    private Tablero crearNuevoTablero(String nombre, String email) {
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
}
