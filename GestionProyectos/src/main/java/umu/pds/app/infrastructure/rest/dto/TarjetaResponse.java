package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Tarjeta;

import java.util.List;

public record TarjetaResponse(
        String id,
        String titulo,
        String descripcion,
        boolean completada,
        List<EtiquetaResponse> etiquetas,
        boolean tieneTarea,
        boolean tieneChecklist,
        ChecklistResponse checklist,
        String fechaVencimiento
) {
    public static TarjetaResponse from(Tarjeta tarjeta) {
        return new TarjetaResponse(
                tarjeta.getId().toString(),
                tarjeta.getTitulo(),
                tarjeta.getDescripcion(),
                tarjeta.estaCompletada(),
                tarjeta.getEtiquetas().stream().map(EtiquetaResponse::from).toList(),
                tarjeta.tieneTarea(),
                tarjeta.tieneChecklist(),
                tarjeta.getChecklist().map(ChecklistResponse::from).orElse(null),
                tarjeta.getFechaVencimiento() != null ? tarjeta.getFechaVencimiento().toString() : null
        );
    }
}
