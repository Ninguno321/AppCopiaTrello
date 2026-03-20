package umu.pds.app.domain.modelo.tablero;

import java.time.LocalDate;
import java.util.UUID;

import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.ListaId.ListaIdException;

/**
 * Value Object: representa una fecha limite.
 */
public record FechaLimite(LocalDate date) {

	
	public static class FechaLimiteException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public FechaLimiteException (String msg) {
			super (msg);
		}
	}
	
	public static FechaLimite de(String date) {
	    if (date == null) {
	        throw new FechaLimiteException("El valor no puede ser nulo");
	    }
	    try {
	    	return new FechaLimite(LocalDate.parse(date));
	    } catch (IllegalArgumentException e) {
	        throw new FechaLimiteException("Formato de fecha no válido: " + date);
	    }
	}
	
    public FechaLimite {
        if (date == null)
            throw new FechaLimiteException("La fecha no puede ser null");
        if (date.isBefore(LocalDate.now()))
        	throw new FechaLimiteException("La fecha no puede ser anterior a la acutal");
    }

    public static FechaLimite nuevo(LocalDate date) {
        return new FechaLimite(date);
    }

    @Override
    public String toString() {
        return date.toString();
    }
    
}


