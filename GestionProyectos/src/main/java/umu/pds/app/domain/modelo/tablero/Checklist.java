package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.exceptions.CheckListException;
import umu.pds.app.domain.exceptions.CheckListIndiceException;
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

    public Checklist(ChecklistId id, String nombre) throws CheckListException {
        if (id == null) throw new CheckListException("ChecklistId no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new CheckListException("El checklist debe tener un nombre");
        this.id = id;
        this.nombre = nombre;
        this.items = new ArrayList<>();
    }

    public static Checklist nuevo(String nombre) throws CheckListException {
        return new Checklist(ChecklistId.nuevo(), nombre);
    }

    // --- Mutaciones ---

    public void renombrar(String nuevoNombre) throws CheckListException {
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new CheckListException("El nombre no puede estar vacío");
        this.nombre = nuevoNombre;
    }

    public void agregarItem(String descripcion) {
        items.add(ItemChecklist.nuevo(descripcion));
    }

    public void marcarItem(int indice) throws CheckListIndiceException {
        validarIndice(indice);
        items.set(indice, items.get(indice).marcarCompletado());
    }

    public void desmarcarItem(int indice) throws CheckListIndiceException {
        validarIndice(indice);
        items.set(indice, items.get(indice).desmarcarCompletado());
    }

    public void eliminarItem(int indice) throws CheckListIndiceException {
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

    private void validarIndice(int indice) throws CheckListIndiceException {
        if (indice < 0 || indice >= items.size())
            throw new CheckListIndiceException("Índice de ítem inválido: " + indice);
    }
}
