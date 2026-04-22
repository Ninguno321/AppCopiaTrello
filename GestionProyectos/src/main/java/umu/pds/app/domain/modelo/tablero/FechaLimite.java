package umu.pds.app.domain.modelo.tablero;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object: fecha límite de una tarjeta.
 * nuevo() valida que la fecha no sea pasada.
 * reconstitute() omite esa validación (solo para carga desde persistencia).
 */
public final class FechaLimite {

    private final LocalDate date;

    private FechaLimite(LocalDate date) {
        this.date = date;
    }

    public static class FechaLimiteException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public FechaLimiteException(String msg) { super(msg); }
    }

    public static FechaLimite nuevo(LocalDate date) {
        if (date == null)
            throw new FechaLimiteException("La fecha no puede ser null");
        if (date.isBefore(LocalDate.now()))
            throw new FechaLimiteException("La fecha no puede ser anterior a la actual");
        return new FechaLimite(date);
    }

    public static FechaLimite de(String date) {
        if (date == null)
            throw new FechaLimiteException("El valor no puede ser nulo");
        try {
            return nuevo(LocalDate.parse(date));
        } catch (java.time.format.DateTimeParseException e) {
            throw new FechaLimiteException("Formato de fecha no válido: " + date);
        }
    }

    /** Solo para reconstruir desde persistencia — no valida fecha pasada. */
    public static FechaLimite reconstitute(LocalDate date) {
        if (date == null)
            throw new FechaLimiteException("La fecha no puede ser null");
        return new FechaLimite(date);
    }

    public LocalDate date() { return date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FechaLimite other)) return false;
        return date.equals(other.date);
    }

    @Override
    public int hashCode() { return Objects.hash(date); }

    @Override
    public String toString() { return date.toString(); }
}
