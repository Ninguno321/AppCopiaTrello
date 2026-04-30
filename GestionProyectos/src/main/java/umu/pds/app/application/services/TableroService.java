package umu.pds.app.application.services;

import org.springframework.transaction.annotation.Transactional;
import umu.pds.app.application.commands.PlantillaEtiquetaCommand;
import umu.pds.app.application.commands.PlantillaItemChecklistCommand;
import umu.pds.app.application.commands.PlantillaListaCommand;
import umu.pds.app.application.commands.PlantillaTableroCommand;
import umu.pds.app.application.commands.PlantillaTarjetaCommand;
import umu.pds.app.application.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.exceptions.ChecklistException;
import umu.pds.app.domain.exceptions.ChecklistIndiceException;
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
    @Transactional
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
    @Transactional(readOnly = true)
    public Tablero obtenerTablero(TableroId id) {
        return tableroRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tablero> obtenerTablerosPorEmail(String email) {
        return tableroRepository.buscarPorEmail(email);
    }

    @Override
    @Transactional
    public void renombrarTablero(TableroId id, String nuevoNombre) {
        Tablero tablero = obtenerTablero(id);
        tablero.renombrar(nuevoNombre);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void bloquearTablero(TableroId id) {
        Tablero tablero = obtenerTablero(id);
        tablero.bloquear();
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desbloquearTablero(TableroId id) {
        Tablero tablero = obtenerTablero(id);
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
        Tablero tablero = obtenerTablero(tableroId);
        Lista lista = tablero.agregarLista(Lista.nueva(nombre, maxTarjetas));
        tableroRepository.guardar(tablero);
        return lista;
    }

    @Override
    @Transactional
    public void eliminarLista(TableroId tableroId, ListaId listaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.eliminarLista(listaId);
        tableroRepository.guardar(tablero);
    }

    // --- Tarjetas ---

    @Override
    @Transactional
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
    @Transactional
    public void eliminarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.eliminarTarjeta(listaId, tarjetaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void moverTarjeta(TableroId tableroId, TarjetaId tarjetaId, ListaId listaOrigenId, ListaId listaDestinoId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.moverTarjeta(tarjetaId, listaOrigenId, listaDestinoId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void marcarTarjetaCompletada(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.completarTarjeta(tarjetaId, listaId);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void etiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.etiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desetiquetarTarjeta(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, Etiqueta etiqueta) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.desetiquetarTarjeta(tarjetaId, listaId, etiqueta);
        tableroRepository.guardar(tablero);
    }

    // --- Checklist ---

    @Override
    @Transactional
    public Checklist asignarChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String nombre) {
        Tablero tablero = obtenerTablero(tableroId);
        Checklist checklist = tablero.asignarChecklist(listaId, tarjetaId, nombre);
        tableroRepository.guardar(tablero);
        return checklist;
    }

    @Override
    @Transactional
    public void agregarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, String descripcion) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.agregarItemChecklist(listaId, tarjetaId, descripcion);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void marcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.marcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }

    @Override
    @Transactional
    public void desmarcarItemChecklist(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.desmarcarItemChecklist(listaId, tarjetaId, indice);
        tableroRepository.guardar(tablero);
    }

    // --- Fecha Límite ---

    @Override
    @Transactional
    public void asignarFechaLimite(TableroId tableroId, ListaId listaId, TarjetaId tarjetaId, FechaLimite fecha) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.asignarFechaLimiteTarjeta(tarjetaId, listaId, fecha);
        tableroRepository.guardar(tablero);
    }

    // --- Prerequisitos ---

    @Override
    @Transactional
    public void configurarListasRequeridas(TableroId tableroId, ListaId listaId, List<ListaId> listasRequeridas) {
        Tablero tablero = obtenerTablero(tableroId);
        tablero.configurarListasRequeridas(listaId, listasRequeridas);
        tableroRepository.guardar(tablero);
    }

    // --- Importación desde plantilla ---

    @Override
    @Transactional
    public Tablero crearDesdePlantilla(PlantillaTableroCommand plantilla, String email) {
        Tablero tablero = crearTablero(plantilla.nombre(), email);

        if (plantilla.listas() != null) {
            for (PlantillaListaCommand listaCmd : plantilla.listas()) {
                int maxTarjetas = listaCmd.maxTarjetas() != null ? listaCmd.maxTarjetas() : 5;
                if (listaCmd.tarjetas() != null && listaCmd.tarjetas().size() > maxTarjetas) {
                    maxTarjetas = listaCmd.tarjetas().size();
                }
                Lista lista = tablero.agregarLista(Lista.nueva(listaCmd.nombre(), maxTarjetas));
                if (listaCmd.tarjetas() != null) {
                    for (PlantillaTarjetaCommand tarjetaCmd : listaCmd.tarjetas()) {
                        try {
                            Tarjeta tarjeta = Tarjeta.nueva(tarjetaCmd.titulo());

                            if (tarjetaCmd.checklist() != null && !tarjetaCmd.checklist().isEmpty()) {
                                Checklist checklist = Checklist.nuevo(tarjetaCmd.titulo());
                                List<PlantillaItemChecklistCommand> items = tarjetaCmd.checklist();
                                for (int i = 0; i < items.size(); i++) {
                                    PlantillaItemChecklistCommand itemCmd = items.get(i);
                                    checklist.agregarItem(itemCmd.descripcion());
                                    if (itemCmd.completado()) {
                                        checklist.marcarItem(i);
                                    }
                                }
                                tarjeta.asignarChecklist(checklist);
                            }

                            if (tarjetaCmd.fechaLimite() != null) {
                                tarjeta.asignarFechaLimite(FechaLimite.de(tarjetaCmd.fechaLimite()));
                            }

                            if (tarjetaCmd.etiquetas() != null) {
                                for (PlantillaEtiquetaCommand etiquetaCmd : tarjetaCmd.etiquetas()) {
                                    tarjeta.agregarEtiqueta(Etiqueta.de(etiquetaCmd.nombre(), etiquetaCmd.color()));
                                }
                            }

                            tablero.agregarTarjeta(lista.getId(), tarjeta);
                        } catch (TableroException | ChecklistException | ChecklistIndiceException e) {
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                }
            }
        }

        // Segunda pasada: Configurar listas requeridas por nombre
        if (plantilla.listas() != null) {
            for (PlantillaListaCommand listaCmd : plantilla.listas()) {
                if (listaCmd.listasRequeridas() != null && !listaCmd.listasRequeridas().isEmpty()) {
                    // Encontrar el ID de la lista actual
                    Lista listaActual = null;
                    for (Lista l : tablero.getListas()) {
                        if (l.getNombre().equals(listaCmd.nombre())) {
                            listaActual = l;
                            break;
                        }
                    }
                    if (listaActual != null) {
                        List<ListaId> requeridasIds = new java.util.ArrayList<>();
                        for (String reqNombre : listaCmd.listasRequeridas()) {
                            for (Lista l : tablero.getListas()) {
                                if (l.getNombre().equals(reqNombre)) {
                                    requeridasIds.add(l.getId());
                                    break;
                                }
                            }
                        }
                        try {
                            tablero.configurarListasRequeridas(listaActual.getId(), requeridasIds);
                        } catch (TableroException e) {
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                }
            }
        }

        tableroRepository.guardar(tablero);
        return tablero;
    }
}
