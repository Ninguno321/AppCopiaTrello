package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record TableroId(UUID value) {

    public TableroId {
        if (value == null) throw new IllegalArgumentException("TableroId no puede ser nulo");
    }

    public static TableroId nuevo() {
        return new TableroId(UUID.randomUUID());
    }

    public static TableroId de(String value) {
        return new TableroId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
