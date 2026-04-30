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

    @Column(name = "max_tarjetas", nullable = false, columnDefinition = "integer default 5")
    private int maxTarjetas = 5;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "lista_id")
    @OrderColumn(name = "tarjeta_orden")
    private List<TarjetaJpaEntity> tarjetas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lista_prerequisitos", joinColumns = @JoinColumn(name = "lista_id"))
    @Column(name = "lista_requerida_id")
    private List<String> listasRequeridas = new ArrayList<>();

    public ListaJpaEntity() {}

    public ListaJpaEntity(String id, String nombre, int maxTarjetas) {
        this.id = id;
        this.nombre = nombre;
        this.maxTarjetas = maxTarjetas;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getMaxTarjetas() { return maxTarjetas; }
    public void setMaxTarjetas(int maxTarjetas) { this.maxTarjetas = maxTarjetas; }
    public List<TarjetaJpaEntity> getTarjetas() { return tarjetas; }
    public void setTarjetas(List<TarjetaJpaEntity> tarjetas) { this.tarjetas = tarjetas; }
    public List<String> getListasRequeridas() { return listasRequeridas; }
    public void setListasRequeridas(List<String> listasRequeridas) { this.listasRequeridas = listasRequeridas; }
}
