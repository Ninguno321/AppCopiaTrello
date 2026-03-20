package umu.pds.app.domain.modelo.shared;

import java.util.UUID;

public record TarjetaId(UUID value) {

	public static class TarjetaIdException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public TarjetaIdException (String msg) {
			super (msg);
		}
	}
	
	
	public TarjetaId {
        if (value == null) throw new TarjetaIdException("TarjetaId no puede ser nulo");
    }

    public static TarjetaId nuevo() {
        return new TarjetaId(UUID.randomUUID());
    }

    public static TarjetaId de(String value) {
    	if (value == null)
    		throw new TarjetaIdException("TarjetaId no puede ser null");
    	try{
    		return new TarjetaId(UUID.fromString(value));
    	}catch(IllegalArgumentException e) {
    		throw new TarjetaIdException("Formato de UUID no válido: " + e.getMessage());
    	}
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
