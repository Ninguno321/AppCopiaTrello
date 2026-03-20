package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.ItemChecklist;

public record ItemChecklistResponse(String descripcion, boolean completado) {
    public static ItemChecklistResponse from(ItemChecklist item) {
        return new ItemChecklistResponse(item.descripcion(), item.completado());
    }
}
