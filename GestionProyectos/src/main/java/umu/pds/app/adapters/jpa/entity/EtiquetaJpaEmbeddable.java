package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EtiquetaJpaEmbeddable {

    @Column(name = "etiqueta_nombre")
    private String nombre;

    @Column(name = "etiqueta_color")
    private String color;

    protected EtiquetaJpaEmbeddable() {}

    public EtiquetaJpaEmbeddable(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String getNombre() { return nombre; }
    public String getColor() { return color; }
}
