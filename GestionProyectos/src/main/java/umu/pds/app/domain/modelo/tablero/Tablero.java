package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;

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
    private final String emailPropietario;
    private boolean bloqueado;
    private final List<Lista> listas;
    private final List<Tarjeta> tarjetasCompletadas;
    private final List<Traza> historial;

    public Tablero(String nombre, String emailPropietario) {
        this(TableroId.nuevo(), nombre, emailPropietario);
    }

    public Tablero(TableroId id, String nombre, String emailPropietario) {
        if (id == null) throw new IllegalArgumentException("TableroId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El tablero debe tener un nombre");
        if (emailPropietario == null || emailPropietario.isBlank()) throw new IllegalArgumentException("El tablero debe tener un email de propietario");
        this.id = id;
        this.nombre = nombre;
        this.emailPropietario = emailPropietario;
        this.bloqueado = false;
        this.listas = new ArrayList<>();
        this.tarjetasCompletadas = new ArrayList<>();
        this.historial = new ArrayList<>();
    }

    // --- Operaciones sobre Listas ---

    public Lista agregarLista(String nombre) {
        Lista lista = Lista.nueva(nombre);
        listas.add(lista);
        historial.add(Traza.nueva("Lista '" + nombre + "' añadida al tablero"));
        return lista;
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

    public Tarjeta agregarTarjeta(ListaId listaId, String titulo) {
        if (bloqueado)
            throw new IllegalStateException("El tablero está bloqueado: no se pueden añadir tarjetas nuevas");
        Lista lista = buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId));
        Tarjeta tarjeta = Tarjeta.nueva(titulo);
        lista.agregarTarjeta(tarjeta);
        historial.add(Traza.nueva("Tarjeta '" + titulo + "' añadida a la lista '" + lista.getNombre() + "'"));
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
     * Marca una tarjeta como completada y la mueve a la lista especial de completadas.
     */
    public void marcarTarjetaCompletada(ListaId listaId, TarjetaId tarjetaId) {
        Lista lista = buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId));
        Tarjeta tarjeta = lista.buscarTarjeta(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada: " + tarjetaId));

        lista.eliminarTarjeta(tarjetaId);
        tarjeta.marcarCompletada();
        tarjetasCompletadas.add(tarjeta);
        historial.add(Traza.nueva(
            "Tarjeta '" + tarjeta.getTitulo() + "' marcada como completada desde la lista '" + lista.getNombre() + "'"
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
    public String getEmailPropietario() { 
    	return emailPropietario; 
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
