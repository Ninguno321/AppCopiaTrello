package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listas")
public class ListaJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "lista_id")
    @OrderColumn(name = "tarjeta_orden")
    private List<TarjetaJpaEntity> tarjetas = new ArrayList<>();

    public ListaJpaEntity() {}

    public ListaJpaEntity(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public List<TarjetaJpaEntity> getTarjetas() { return tarjetas; }
    public void setTarjetas(List<TarjetaJpaEntity> tarjetas) { this.tarjetas = tarjetas; }
}
