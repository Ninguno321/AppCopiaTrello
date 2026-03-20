package umu.pds.app.domain.modelo.tablero;

import java.time.LocalDateTime;

/**
 * Value Object: registro inmutable de una acción ocurrida en el Tablero.
 * Se genera automáticamente ante cualquier operación relevante.
 */
public record Traza(String descripcion, LocalDateTime timestamp) {

    public static class TrazaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public TrazaException(String msg) {
            super(msg);
        }
    }

    public Traza {
        if (descripcion == null || descripcion.isBlank()) {
            throw new TrazaException("La traza debe tener una descripción");
        }
        if (timestamp == null) {
            throw new TrazaException("La traza debe tener un timestamp");
        }
    }

    public static Traza nueva(String descripcion) {
        return new Traza(descripcion, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + descripcion;
    }
}
