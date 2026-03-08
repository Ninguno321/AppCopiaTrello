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
    private final List<Lista> listas;

    public Tablero(String nombre) {
        this(TableroId.nuevo(), nombre);
    }

    public Tablero(TableroId id, String nombre) {
        if (id == null) throw new IllegalArgumentException("TableroId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El tablero debe tener un nombre");
        this.id = id;
        this.nombre = nombre;
        this.listas = new ArrayList<>();
    }

    // Operaciones sobre Listas

    public Lista agregarLista(String nombre) {
        Lista lista = Lista.nueva(nombre);
        listas.add(lista);
        return lista;
    }

    public boolean eliminarLista(ListaId listaId) {
        return listas.removeIf(l -> l.getId().equals(listaId));
    }

    public Optional<Lista> buscarLista(ListaId listaId) {
        return listas.stream()
                .filter(l -> l.getId().equals(listaId))
                .findFirst();
    }

    // Operaciones sobre Tarjetas (siempre a través del Tablero)

    public Tarjeta agregarTarjeta(ListaId listaId, String titulo) {
        Lista lista = buscarLista(listaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + listaId));
        Tarjeta tarjeta = Tarjeta.nueva(titulo);
        lista.agregarTarjeta(tarjeta);
        return tarjeta;
    }

    public boolean eliminarTarjeta(ListaId listaId, TarjetaId tarjetaId) {
        return buscarLista(listaId)
                .map(l -> l.eliminarTarjeta(tarjetaId))
                .orElse(false);
    }

    /**
     * Mueve una tarjeta de una lista a otra dentro del mismo tablero.
     * Lanza {@link IllegalArgumentException} si alguna lista o la tarjeta no existen.
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
    }

    // Mutaciones propias

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    // Getters

    public TableroId getId() { 
    	return id; 
    }
    public String getNombre() { 
    	return nombre; 
    }
    public List<Lista> getListas() { 
    	return Collections.unmodifiableList(listas); 
    }
    public int totalListas() { 
    	return listas.size(); 
    }

    // Identidad por ID (regla de Entidad / Aggregate Root en DDD)

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
        return "Tablero{id=" + id + ", nombre='" + nombre + "', listas=" + listas.size() + "}";
    }
}
