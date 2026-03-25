package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class TareaJpaEmbeddable {

    @Column(name = "tarea_titulo")
    private String titulo;

    @Column(name = "tarea_descripcion")
    private String descripcion;

    @Column(name = "tarea_fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "tarea_estado")
    private String estado;

    protected TareaJpaEmbeddable() {}

    public TareaJpaEmbeddable(String titulo, String descripcion, LocalDate fechaLimite, String estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
}
