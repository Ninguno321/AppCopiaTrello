package umu.pds.app.domain.modelo.usuario;

import java.util.Objects;

/**
 * Entidad del dominio. Persona identificada por su correo electrónico.
 * El email actúa como identidad única (no hace falta un UUID aparte).
 * No contiene ninguna dependencia de Spring ni de Jakarta Persistence.
 */
public class Usuario {

    private final String email;
    private String nombre;

    public Usuario(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email del usuario no puede estar vacío");
        this.email = email;
    }

    public Usuario(String email, String nombre) {
        this(email);
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre;
        }
    }

    // --- Mutaciones ---

    public void cambiarNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nombre;
    }

    // --- Getters ---

    public String getEmail() { return email; }
    public String getNombre() { return nombre; }

    // --- Identidad por email (regla DDD: Entidad se identifica por su ID) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario other)) return false;
        return email.equals(other.email);
    }

    @Override
    public int hashCode() { return Objects.hash(email); }

    @Override
    public String toString() { return "Usuario{email='" + email + "'}"; }
}
