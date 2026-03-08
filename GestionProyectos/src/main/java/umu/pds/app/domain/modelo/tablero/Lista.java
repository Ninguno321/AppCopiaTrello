package umu.pds.app.domain.modelo.tablero;

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
    private final List<Tarjeta> tarjetas;

    Lista(ListaId id, String nombre) {
        if (id == null) throw new IllegalArgumentException("ListaId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("La lista debe tener un nombre");
        this.id = id;
        this.nombre = nombre;
        this.tarjetas = new ArrayList<>();
    }

    static Lista nueva(String nombre) {
        return new Lista(ListaId.nuevo(), nombre);
    }

    // --- Mutaciones package-private (solo el Tablero las invoca) ---

    void agregarTarjeta(Tarjeta tarjeta) {
        tarjetas.add(tarjeta);
    }

    boolean eliminarTarjeta(TarjetaId tarjetaId) {
        return tarjetas.removeIf(t -> t.getId().equals(tarjetaId));
    }

    Optional<Tarjeta> buscarTarjeta(TarjetaId tarjetaId) {
        return tarjetas.stream()
                .filter(t -> t.getId().equals(tarjetaId))
                .findFirst();
    }

    // --- Mutación pública permitida (solo renombrar la propia lista) ---

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    // --- Getters ---

    public ListaId getId() { 
    	return id; 
    }
    public String getNombre() { 
    	return nombre; 
    }
    public List<Tarjeta> getTarjetas() { 
    	return Collections.unmodifiableList(tarjetas); 
    }
    public int totalTarjetas() { 
    	return tarjetas.size(); 
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
