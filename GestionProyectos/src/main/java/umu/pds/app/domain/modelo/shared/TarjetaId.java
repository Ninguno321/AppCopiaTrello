package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record TarjetaId(UUID value) {

    public TarjetaId {
        if (value == null) throw new IllegalArgumentException("TarjetaId no puede ser nulo");
    }

    public static TarjetaId nuevo() {
        return new TarjetaId(UUID.randomUUID());
    }

    public static TarjetaId de(String value) {
        return new TarjetaId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
