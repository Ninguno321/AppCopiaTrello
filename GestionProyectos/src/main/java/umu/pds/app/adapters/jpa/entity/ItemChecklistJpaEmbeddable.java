package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemChecklistJpaEmbeddable {

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "completado")
    private boolean completado;

    protected ItemChecklistJpaEmbeddable() {}

    public ItemChecklistJpaEmbeddable(String descripcion, boolean completado) {
        this.descripcion = descripcion;
        this.completado = completado;
    }

    public String getDescripcion() { return descripcion; }
    public boolean isCompletado() { return completado; }
}
