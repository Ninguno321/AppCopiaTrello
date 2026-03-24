package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tarjetas")
public class TarjetaJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String titulo;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private boolean completada;

    @Embedded
    private TareaJpaEmbeddable tarea;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "checklist_id")
    private ChecklistJpaEntity checklist;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tarjeta_etiquetas", joinColumns = @JoinColumn(name = "tarjeta_id"))
    private List<EtiquetaJpaEmbeddable> etiquetas = new ArrayList<>();

    public TarjetaJpaEntity() {}

    public TarjetaJpaEntity(String id, String titulo, String descripcion, boolean completada) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.completada = completada;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public boolean isCompletada() { return completada; }
    public TareaJpaEmbeddable getTarea() { return tarea; }
    public void setTarea(TareaJpaEmbeddable tarea) { this.tarea = tarea; }
    public ChecklistJpaEntity getChecklist() { return checklist; }
    public void setChecklist(ChecklistJpaEntity checklist) { this.checklist = checklist; }
    public List<EtiquetaJpaEmbeddable> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<EtiquetaJpaEmbeddable> etiquetas) { this.etiquetas = etiquetas; }
}
