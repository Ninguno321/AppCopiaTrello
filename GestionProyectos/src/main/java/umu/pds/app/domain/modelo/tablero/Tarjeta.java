package umu.pds.app.domain.modelo.tablero;

import umu.pds.app.domain.modelo.shared.TarjetaId;

import java.util.Objects;
import java.util.Optional;

/**
 * Entidad. Actúa como contenedor: puede tener una Tarea (VO) y/o un Checklist (Entidad local).
 * Su ciclo de vida está gestionado por el Aggregate Root Tablero.
 */
public class Tarjeta {

    private final TarjetaId id;
    private String titulo;
    private String descripcion;
    private Tarea tarea;
    private Checklist checklist;

    public Tarjeta(TarjetaId id, String titulo) {
        if (id == null) throw new IllegalArgumentException("TarjetaId no puede ser nulo");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("La tarjeta debe tener un título");
        this.id = id;
        this.titulo = titulo;
    }

    public static Tarjeta nueva(String titulo) {
        return new Tarjeta(TarjetaId.nuevo(), titulo);
    }

    // --- Mutaciones públicas ---

    public void cambiarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("El título no puede estar vacío");
        this.titulo = titulo;
    }

    public void cambiarDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void asignarTarea(Tarea tarea) {
        if (tarea == null) throw new IllegalArgumentException("La tarea no puede ser nula");
        this.tarea = tarea;
    }

    public void quitarTarea() {
        this.tarea = null;
    }

    public void asignarChecklist(Checklist checklist) {
        if (checklist == null) throw new IllegalArgumentException("El checklist no puede ser nulo");
        this.checklist = checklist;
    }

    public void quitarChecklist() {
        this.checklist = null;
    }

    // --- Consultas ---

    public boolean tieneTarea() { return tarea != null; }
    public boolean tieneChecklist() { return checklist != null; }

    // --- Getters ---

    public TarjetaId getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Optional<Tarea> getTarea() { return Optional.ofNullable(tarea); }
    public Optional<Checklist> getChecklist() { return Optional.ofNullable(checklist); }

    // --- Identidad por ID ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tarjeta other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tarjeta{id=" + id + ", titulo='" + titulo + "'}";
    }
}
