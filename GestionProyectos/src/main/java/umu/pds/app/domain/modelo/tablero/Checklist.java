package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.modelo.shared.ChecklistId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad local: vive únicamente dentro de una Tarjeta.
 * Tiene identidad propia (ChecklistId), pero no tiene repositorio propio.
 */
public class Checklist {

    private final ChecklistId id;
    private String nombre;
    private final List<ItemChecklist> items;

    public Checklist(ChecklistId id, String nombre) {
        if (id == null) throw new IllegalArgumentException("ChecklistId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El checklist debe tener un nombre");
        this.id = id;
        this.nombre = nombre;
        this.items = new ArrayList<>();
    }

    public static Checklist nuevo(String nombre) {
        return new Checklist(ChecklistId.nuevo(), nombre);
    }

    // --- Mutaciones ---

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    public void agregarItem(String descripcion) {
        items.add(ItemChecklist.nuevo(descripcion));
    }

    public void marcarItem(int indice) {
        validarIndice(indice);
        items.set(indice, items.get(indice).marcarCompletado());
    }

    public void desmarcarItem(int indice) {
        validarIndice(indice);
        items.set(indice, items.get(indice).desmarcarCompletado());
    }

    public void eliminarItem(int indice) {
        validarIndice(indice);
        items.remove(indice);
    }

    // --- Consultas ---

    public int totalItems() {
        return items.size();
    }

    public long itemsCompletados() {
        return items.stream().filter(ItemChecklist::completado).count();
    }

    public boolean estaCompleto() {
        return !items.isEmpty() && itemsCompletados() == items.size();
    }

    // --- Getters ---

    public ChecklistId getId() { return id; }
    public String getNombre() { return nombre; }
    public List<ItemChecklist> getItems() { return Collections.unmodifiableList(items); }

    // --- Identidad por ID (regla de Entidad en DDD) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Checklist other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Checklist{id=" + id + ", nombre='" + nombre + "', items=" + items.size() + "}";
    }

    // --- Privado ---

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= items.size())
            throw new IllegalArgumentException("Índice de ítem inválido: " + indice);
    }
}
