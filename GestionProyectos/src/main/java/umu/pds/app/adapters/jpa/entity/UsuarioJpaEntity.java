package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    private String email;

    @Column
    private String nombre;

    public UsuarioJpaEntity() {}

    public UsuarioJpaEntity(String email, String nombre) {
        this.email = email;
        this.nombre = nombre;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
