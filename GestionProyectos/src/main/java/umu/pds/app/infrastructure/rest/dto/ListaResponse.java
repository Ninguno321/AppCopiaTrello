package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Lista;

import java.util.List;

public record ListaResponse(
        String id,
        String nombre,
        List<TarjetaResponse> tarjetas
) {
    public static ListaResponse from(Lista lista) {
        return new ListaResponse(
                lista.getId().toString(),
                lista.getNombre(),
                lista.getTarjetas().stream().map(TarjetaResponse::from).toList()
        );
    }
}
