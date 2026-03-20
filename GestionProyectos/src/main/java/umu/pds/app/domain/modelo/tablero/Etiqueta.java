package umu.pds.app.domain.modelo.tablero;

/**
 * Value Object: clasifica una Tarjeta visualmente.
 * Igualdad por valor (nombre + color).
 */
public record Etiqueta(String nombre, String color) {

    public static class EtiquetaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public EtiquetaException(String msg) {
            super(msg);
        }
    }

    public Etiqueta {
        if (nombre == null || nombre.isBlank())
            throw new EtiquetaException("La etiqueta debe tener un nombre");
        if (color == null || color.isBlank())
            throw new EtiquetaException("La etiqueta debe tener un color");
    }

    public static Etiqueta de(String nombre, String color) {
        return new Etiqueta(nombre, color);
    }

    @Override
    public String toString() {
        return "Etiqueta{nombre='" + nombre + "', color='" + color + "'}";
    }
}
