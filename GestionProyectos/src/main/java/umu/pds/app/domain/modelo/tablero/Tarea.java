package umu.pds.app.domain.modelo.tablero;

import java.time.LocalDate;

/**
 * Value Object: inmutable, igualdad por valor de todos sus campos.
 * Las mutaciones devuelven una nueva instancia.
 */														// Fecha Limite tiene que ser VO. Asi podemos comprobar que no sea anterior a la actual
public record Tarea(String titulo, String descripcion, LocalDate fechaLimite, EstadoTarea estado) {

	public static class TareaException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public TareaException (String msg) {
			super (msg);
		}
	}
	
    public Tarea {
        if (titulo == null || titulo.isBlank())
            throw new TareaException("La tarea debe tener un título");
        if (estado == null)
            estado = EstadoTarea.PENDIENTE;
    }

    public static Tarea nueva(String titulo) {
        return new Tarea(titulo, null, null, EstadoTarea.PENDIENTE);
    }

    public Tarea conDescripcion(String descripcion) {
        return new Tarea(titulo, descripcion, fechaLimite, estado);
    }

    public Tarea conFechaLimite(LocalDate fecha) {
        return new Tarea(titulo, descripcion, fecha, estado);
    }

    public Tarea conEstado(EstadoTarea nuevoEstado) {
        if (nuevoEstado == null) throw new TareaException("El estado no puede ser nulo");
        return new Tarea(titulo, descripcion, fechaLimite, nuevoEstado);
    }

    public boolean estaCompletada() {
        return EstadoTarea.COMPLETADA.equals(estado);
    }
}
