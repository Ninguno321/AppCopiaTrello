package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record ListaId(UUID value) {

    public ListaId {
        if (value == null) throw new IllegalArgumentException("ListaId no puede ser nulo");
    }

    public static ListaId nuevo() {
        return new ListaId(UUID.randomUUID());
    }

    public static ListaId de(String value) {
        return new ListaId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
