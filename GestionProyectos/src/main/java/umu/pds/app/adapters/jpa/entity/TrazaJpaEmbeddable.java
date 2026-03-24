package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class TrazaJpaEmbeddable {

    @Column(name = "traza_descripcion", length = 500)
    private String descripcion;

    @Column(name = "traza_timestamp")
    private LocalDateTime timestamp;

    protected TrazaJpaEmbeddable() {}

    public TrazaJpaEmbeddable(String descripcion, LocalDateTime timestamp) {
        this.descripcion = descripcion;
        this.timestamp = timestamp;
    }

    public String getDescripcion() { return descripcion; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
