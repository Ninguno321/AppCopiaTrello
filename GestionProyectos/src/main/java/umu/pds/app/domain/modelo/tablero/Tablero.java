package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.exceptions.CheckListException;
import umu.pds.app.domain.exceptions.CheckListIndiceException;
import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root del bounded context Tablero.
 *
 * Toda mutación del agregado (crear listas, mover tarjetas, etc.)
 * pasa obligatoriamente por esta clase. Nada externo puede modificar
 * directamente una Lista o Tarjeta interna.
 */
public class Tablero {

    private final TableroId id;
    private String nombre;
    private final Usuario propietario;
    private boolean bloqueado;
    private final List<Lista> listas;
    private final List<Tarjeta> tarjetasCompletadas;
    private final List<Traza> historial;

    public Tablero(String nombre, Usuario propietario) {
        this(TableroId.nuevo(), nombre, propietario);
    }

    public Tablero(TableroId id, String nombre, Usuario propietario) {
        if (id == null) throw new IllegalArgumentException("TableroId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El tablero debe tener un nombre");
        if (propietario == null) throw new IllegalArgumentException("El tablero debe tener un propietario");
        this.id = id;
        this.nombre = nombre;
        this.propietario = propietario;
        this.bloqueado = false;
        this.listas = new ArrayList<>();
        this.tarjetasCompletadas = new ArrayList<>();
        this.historial = new ArrayList<>();
    }

    // --- Operaciones sobre Listas ---

    /**
     * Agrega una Lista ya construida al tablero y registra la accion en el historial.
     */
    public Lista agregarLista(Lista nuevaLista) {
        if (nuevaLista == null)
            throw new IllegalArgumentException("La lista no puede ser nula");
        listas.add(nuevaLista);
        historial.add(Traza.nueva("Lista '" + nuevaLista.getNombre() + "' agregada al tablero"));
        return nuevaLista;
    }

    public boolean eliminarLista(ListaId listaId) {
        Optional<Lista> lista = buscarLista(listaId);
        boolean eliminada = listas.removeIf(l -> l.getId().equals(listaId));
        if (eliminada) {
            lista.ifPresent(l ->
                historial.add(Traza.nueva("Lista '" + l.getNombre() + "' eliminada del tablero"))
            );
        }
        return eliminada;
    }

    public Optional<Lista> buscarLista(ListaId listaId) {
        return listas.stream()
                .filter(l -> l.getId().equals(listaId))
                .findFirst();
    }

    // --- Operaciones sobre Tarjetas (siempre a través del Tablero) ---

    /**
     * Agrega una Tarjeta ya construida a la lista indicada.
     * INVARIANTE: lanza TableroException si el tablero esta bloqueado.
     */
    public Tarjeta agregarTarjeta(ListaId listaId, Tarjeta tarjeta) throws TableroException {
        if (bloqueado)
            throw new TableroException("El tablero esta bloqueado: no se pueden agregar tarjetas nuevas");
        if (tarjeta == null)
            throw new IllegalArgumentException("La tarjeta no puede ser nula");
        Lista lista = buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId));
        lista.agregarTarjeta(tarjeta);
        historial.add(Traza.nueva("Tarjeta '" + tarjeta.getTitulo() + "' agregada a la lista '" + lista.getNombre() + "'"));
        return tarjeta;
    }

    public boolean eliminarTarjeta(ListaId listaId, TarjetaId tarjetaId) {
        Optional<Lista> lista = buscarLista(listaId);
        boolean eliminada = lista.map(l -> l.eliminarTarjeta(tarjetaId)).orElse(false);
        if (eliminada) {
            lista.ifPresent(l ->
                historial.add(Traza.nueva("Tarjeta eliminada de la lista '" + l.getNombre() + "'"))
            );
        }
        return eliminada;
    }

    /**
     * Mueve una tarjeta de una lista a otra dentro del mismo tablero.
     * Permitido aunque el tablero esté bloqueado (solo se prohíbe añadir nuevas).
     */
    public void moverTarjeta(TarjetaId tarjetaId, ListaId listaOrigenId, ListaId listaDestinoId) {
        Lista origen = buscarLista(listaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Lista origen no encontrada: " + listaOrigenId));
        Lista destino = buscarLista(listaDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Lista destino no encontrada: " + listaDestinoId));

        Tarjeta tarjeta = origen.buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada en la lista origen: " + tarjetaId));

        origen.eliminarTarjeta(tarjetaId);
        destino.agregarTarjeta(tarjeta);
        historial.add(Traza.nueva(
            "Tarjeta '" + tarjeta.getTitulo() + "' movida de '" + origen.getNombre() + "' a '" + destino.getNombre() + "'"
        ));
    }

    /**
     * Saca la tarjeta de su lista origen y la registra en tarjetasCompletadas.
     * Permitido aunque el tablero este bloqueado (solo agregar tarjetas nuevas esta restringido).
     */
    public void completarTarjeta(TarjetaId tarjetaId, ListaId listaOrigenId) {
        Lista lista = buscarLista(listaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaOrigenId));
        Tarjeta tarjeta = lista.buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada: " + tarjetaId));
        lista.eliminarTarjeta(tarjetaId);
        tarjeta.marcarCompletada();
        tarjetasCompletadas.add(tarjeta);
        historial.add(Traza.nueva(
            "Tarjeta '" + tarjeta.getTitulo() + "' completada desde la lista '" + lista.getNombre() + "'"
        ));
    }

    // --- Etiquetas ---

    /**
     * Asigna una etiqueta a la tarjeta indicada y registra la accion en el historial.
     * Toda modificacion pasa por el Aggregate Root para mantener las invariantes.
     */
    public void etiquetarTarjeta(TarjetaId tarjetaId, ListaId listaId, Etiqueta etiqueta) {
        if (etiqueta == null)
            throw new IllegalArgumentException("La etiqueta no puede ser nula");
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        tarjeta.agregarEtiqueta(etiqueta);
        historial.add(Traza.nueva(
            "Etiqueta '" + etiqueta.nombre() + "' asignada a la tarjeta '" + tarjeta.getTitulo() + "'"
        ));
    }

    /**
     * Quita una etiqueta de la tarjeta indicada y registra la accion en el historial.
     */
    public void desetiquetarTarjeta(TarjetaId tarjetaId, ListaId listaId, Etiqueta etiqueta) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        tarjeta.quitarEtiqueta(etiqueta);
        historial.add(Traza.nueva(
            "Etiqueta '" + etiqueta.nombre() + "' retirada de la tarjeta '" + tarjeta.getTitulo() + "'"
        ));
    }

    // --- Checklist ---

    public Checklist asignarChecklist(ListaId listaId, TarjetaId tarjetaId, String nombre) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        try {
            Checklist checklist = Checklist.nuevo(nombre);
            tarjeta.asignarChecklist(checklist);
            historial.add(Traza.nueva("Checklist '" + nombre + "' asignado a tarjeta '" + tarjeta.getTitulo() + "'"));
            return checklist;
        } catch (CheckListException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void agregarItemChecklist(ListaId listaId, TarjetaId tarjetaId, String descripcion) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        Checklist checklist = tarjeta.getChecklist()
                .orElseThrow(() -> new IllegalStateException("La tarjeta no tiene checklist"));
        checklist.agregarItem(descripcion);
    }

    public void marcarItemChecklist(ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        Checklist checklist = tarjeta.getChecklist()
                .orElseThrow(() -> new IllegalStateException("La tarjeta no tiene checklist"));
        try {
            checklist.marcarItem(indice);
        } catch (CheckListIndiceException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void desmarcarItemChecklist(ListaId listaId, TarjetaId tarjetaId, int indice) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        Checklist checklist = tarjeta.getChecklist()
                .orElseThrow(() -> new IllegalStateException("La tarjeta no tiene checklist"));
        try {
            checklist.desmarcarItem(indice);
        } catch (CheckListIndiceException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // --- Fecha de Vencimiento ---

    public void asignarFechaVencimientoTarjeta(TarjetaId tarjetaId, ListaId listaId, LocalDateTime fecha) {
        Tarjeta tarjeta = buscarTarjetaEnLista(listaId, tarjetaId);
        tarjeta.asignarFechaVencimiento(fecha);
        historial.add(Traza.nueva(
            "Fecha de vencimiento '" + fecha + "' asignada a la tarjeta '" + tarjeta.getTitulo() + "'"
        ));
    }

    // --- Bloqueo ---

    public void bloquear() {
        this.bloqueado = true;
        historial.add(Traza.nueva("Tablero bloqueado: no se pueden añadir nuevas tarjetas"));
    }

    public void desbloquear() {
        this.bloqueado = false;
        historial.add(Traza.nueva("Tablero desbloqueado"));
    }

    // --- Mutaciones propias ---

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    // --- Getters ---

    public TableroId getId() {
    	return id;
    }
    public String getNombre() {
    	return nombre;
    }
    public Usuario getPropietario() {
        return propietario;
    }
    /** Convenience: devuelve el email del propietario sin exponer el objeto Usuario. */
    public String getEmailPropietario() {
    	return propietario.getEmail();
    }
    public boolean isBloqueado() {
    	return bloqueado; 
    }
    public List<Lista> getListas() { 
    	return Collections.unmodifiableList(listas); 
    }
    public List<Tarjeta> getTarjetasCompletadas() { 
    	return Collections.unmodifiableList(tarjetasCompletadas); 
    }
    public List<Traza> getHistorial() { 
    	return Collections.unmodifiableList(historial); 
    }
    public int totalListas() { 
    	return listas.size(); 
    }

    // --- Helpers privados ---

    private Tarjeta buscarTarjetaEnLista(ListaId listaId, TarjetaId tarjetaId) {
        return buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId))
                .buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada: " + tarjetaId));
    }

    // --- Identidad por ID (regla de Entidad / Aggregate Root en DDD) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tablero other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tablero{id=" + id + ", nombre='" + nombre + "', listas=" + listas.size() + ", bloqueado=" + bloqueado + "}";
    }
}
