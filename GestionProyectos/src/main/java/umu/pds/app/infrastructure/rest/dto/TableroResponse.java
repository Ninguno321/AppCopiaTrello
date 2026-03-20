package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Tablero;

import java.util.List;

public record TableroResponse(
        String id,
        String nombre,
        String emailPropietario,
        boolean bloqueado,
        List<ListaResponse> listas,
        List<TarjetaResponse> tarjetasCompletadas,
        List<TrazaResponse> historial
) {
    public static TableroResponse from(Tablero tablero) {
        return new TableroResponse(
                tablero.getId().toString(),
                tablero.getNombre(),
                tablero.getEmailPropietario(),
                tablero.isBloqueado(),
                tablero.getListas().stream().map(ListaResponse::from).toList(),
                tablero.getTarjetasCompletadas().stream().map(TarjetaResponse::from).toList(),
                tablero.getHistorial().stream().map(TrazaResponse::from).toList()
        );
    }
}
