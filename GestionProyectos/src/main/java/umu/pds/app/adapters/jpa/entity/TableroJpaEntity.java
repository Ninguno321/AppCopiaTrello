package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tableros")
public class TableroJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "propietario_email", nullable = false)
    private UsuarioJpaEntity propietario;

    @Column(nullable = false)
    private boolean bloqueado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "tablero_id")
    @OrderColumn(name = "lista_orden")
    private List<ListaJpaEntity> listas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "tablero_completadas_id")
    private List<TarjetaJpaEntity> tarjetasCompletadas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tablero_historial", joinColumns = @JoinColumn(name = "tablero_id"))
    @OrderColumn(name = "traza_orden")
    private List<TrazaJpaEmbeddable> historial = new ArrayList<>();

    public TableroJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public UsuarioJpaEntity getPropietario() { return propietario; }
    public void setPropietario(UsuarioJpaEntity propietario) { this.propietario = propietario; }
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    public List<ListaJpaEntity> getListas() { return listas; }
    public void setListas(List<ListaJpaEntity> listas) { this.listas = listas; }
    public List<TarjetaJpaEntity> getTarjetasCompletadas() { return tarjetasCompletadas; }
    public void setTarjetasCompletadas(List<TarjetaJpaEntity> tarjetasCompletadas) { this.tarjetasCompletadas = tarjetasCompletadas; }
    public List<TrazaJpaEmbeddable> getHistorial() { return historial; }
    public void setHistorial(List<TrazaJpaEmbeddable> historial) { this.historial = historial; }
}
