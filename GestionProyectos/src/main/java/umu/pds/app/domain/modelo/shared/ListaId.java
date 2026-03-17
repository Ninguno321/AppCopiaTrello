package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record ListaId(UUID value) {

	public static class ListaIdException extends RuntimeException {
		public ListaIdException (String msg) {
			super (msg);
		}
	}
	
    public ListaId {
        if (value == null) throw new ListaIdException("ListaId no puede ser nulo");
    }

    public static ListaId nuevo() {
        return new ListaId(UUID.randomUUID());
    }

    public static ListaId de(String value) {
        if (value == null) {
            throw new ListaIdException("El valor no puede ser nulo");
        }
        try {
        	return new ListaId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new ListaIdException("Formato de UUID inválido: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
