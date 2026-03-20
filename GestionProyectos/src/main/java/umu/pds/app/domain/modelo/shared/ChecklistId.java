package umu.pds.app.domain.modelo.shared;

import java.util.UUID;


public record ChecklistId(UUID value) {


	public static class ChecklistIdException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public ChecklistIdException (String msg) {
			super (msg);
		}
	}
	
    public ChecklistId {
        if (value == null) throw new ChecklistIdException("ChecklistId no puede ser nulo");
    }

    public static ChecklistId nuevo() {
        return new ChecklistId(UUID.randomUUID());
    }

    public static ChecklistId de(String value) {
        if (value == null) {
            throw new ChecklistIdException("El valor no puede ser nulo");
        }
        try {
        	return new ChecklistId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new ChecklistIdException("Formato de UUID inválido: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
