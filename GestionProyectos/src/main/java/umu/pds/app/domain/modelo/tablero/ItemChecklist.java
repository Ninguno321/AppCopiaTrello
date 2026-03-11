package umu.pds.app.domain.modelo.tablero;

/**
 * Value Object: representa un ítem de un Checklist.
 * Inmutable; marcar/desmarcar devuelve una nueva instancia.
 */
public record ItemChecklist(String descripcion, boolean completado) {

    public ItemChecklist {
        if (descripcion == null || descripcion.isBlank())
            throw new IllegalArgumentException("El ítem debe tener una descripción");
    }

    public static ItemChecklist nuevo(String descripcion) {
        return new ItemChecklist(descripcion, false);
    }

    public ItemChecklist marcarCompletado() {
        return new ItemChecklist(descripcion, true);
    }

    public ItemChecklist desmarcarCompletado() {
        return new ItemChecklist(descripcion, false);
    }
}
