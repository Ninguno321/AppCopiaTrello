package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record TableroId(UUID value) {

	public static class TableroIdException extends RuntimeException {
		public TableroIdException (String msg) {
			super (msg);
		}
	}

    public TableroId {
        if (value == null) throw new TableroIdException("TableroId no puede ser nulo");
    }

    public static TableroId nuevo() {
        return new TableroId(UUID.randomUUID());
    }

    public static TableroId de(String value) {
        if (value == null) {
            throw new TableroIdException("El valor no puede ser nulo");
        }
        try {
            return new TableroId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new TableroIdException("Formato de UUID inválido: " + value);
        }
    }


    @Override
    public String toString() {
        return value.toString();
    }
}
