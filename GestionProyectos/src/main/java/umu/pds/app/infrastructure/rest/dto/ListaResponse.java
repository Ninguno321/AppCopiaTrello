package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Lista;

import java.util.List;

public record ListaResponse(
        String id,
        String nombre,
        int maxTarjetas,
        List<TarjetaResponse> tarjetas,
        List<String> listasRequeridas
) {
    public static ListaResponse from(Lista lista) {
        return new ListaResponse(
                lista.getId().toString(),
                lista.getNombre(),
                lista.getMaxTarjetas(),
                lista.getTarjetas().stream().map(TarjetaResponse::from).toList(),
                lista.getListasRequeridas().stream().map(Object::toString).toList()
        );
    }
}
