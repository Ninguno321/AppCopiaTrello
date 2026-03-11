package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record ChecklistId(UUID value) {

    public ChecklistId {
        if (value == null) throw new IllegalArgumentException("ChecklistId no puede ser nulo");
    }

    public static ChecklistId nuevo() {
        return new ChecklistId(UUID.randomUUID());
    }

    public static ChecklistId de(String value) {
        return new ChecklistId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
