package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.exceptions.ListaException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TarjetaId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Entidad. Agrupa tarjetas dentro de un Tablero.
 *
 * El constructor y las mutaciones estructurales son package-private para
 * garantizar que solo el Aggregate Root (Tablero) puede crear listas
 * y añadir/eliminar tarjetas de ellas. Esto impone la frontera del agregado.
 */
public class Lista {

    private final ListaId id;
    private String nombre;
    private final int maxTarjetas;
    private final List<Tarjeta> tarjetas;
    private final List<ListaId> listasRequeridas;

    public Lista(ListaId id, String nombre, int maxTarjetas) {
        this(id, nombre, maxTarjetas, new ArrayList<>());
    }

    public Lista(ListaId id, String nombre, int maxTarjetas, List<ListaId> listasRequeridas) {
        if (id == null) throw new ListaException("ListaId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new ListaException("La lista debe tener un nombre");
        this.id = id;
        this.nombre = nombre;
        this.maxTarjetas = maxTarjetas;
        this.tarjetas = new ArrayList<>();
        this.listasRequeridas = listasRequeridas != null ? new ArrayList<>(listasRequeridas) : new ArrayList<>();
    }

    public static Lista nueva(String nombre, int maxTarjetas) {
        return new Lista(ListaId.nuevo(), nombre, maxTarjetas);
    }

    // --- Mutaciones package-private (solo el Tablero las invoca) ---

    public void agregarTarjeta(Tarjeta tarjeta) {
        if (tarjetas.size() >= maxTarjetas) {
            throw new ListaException("La lista ha alcanzado su máximo de tarjetas (" + maxTarjetas + ")");
        }
        tarjetas.add(tarjeta);
    }

    public boolean eliminarTarjeta(TarjetaId tarjetaId) {
        return tarjetas.removeIf(t -> t.getId().equals(tarjetaId));
    }

    public Optional<Tarjeta> buscarTarjeta(TarjetaId tarjetaId) {
        return tarjetas.stream()
                .filter(t -> t.getId().equals(tarjetaId))
                .findFirst();
    }

    // --- Mutación pública permitida (solo renombrar la propia lista) ---

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new ListaException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    // --- Getters ---

    public ListaId getId() { 
    	return id; 
    }
    public String getNombre() { 
    	return nombre; 
    }
    public int getMaxTarjetas() {
        return maxTarjetas;
    }
    public List<Tarjeta> getTarjetas() { 
    	return Collections.unmodifiableList(tarjetas); 
    }
    public int totalTarjetas() { 
    	return tarjetas.size(); 
    }

    // --- Prerequisitos ---

    public List<ListaId> getListasRequeridas() {
        return Collections.unmodifiableList(listasRequeridas);
    }

    public boolean tienePrerequisitos() {
        return !listasRequeridas.isEmpty();
    }

    public void configurarListasRequeridas(List<ListaId> nuevas) {
        this.listasRequeridas.clear();
        if (nuevas != null) {
            this.listasRequeridas.addAll(nuevas);
        }
    }

    // --- Identidad por ID ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lista other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Lista{id=" + id + ", nombre='" + nombre + "', tarjetas=" + tarjetas.size() + "}";
    }
}
