package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Checklist;
import java.util.List;

public record ChecklistResponse(
        String id,
        String nombre,
        List<ItemChecklistResponse> items
) {
    public static ChecklistResponse from(Checklist c) {
        return new ChecklistResponse(
                c.getId().toString(),
                c.getNombre(),
                c.getItems().stream().map(ItemChecklistResponse::from).toList()
        );
    }
}
