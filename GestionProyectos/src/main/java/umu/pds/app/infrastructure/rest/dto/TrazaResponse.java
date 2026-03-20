package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Traza;

public record TrazaResponse(String descripcion, String timestamp) {

    public static TrazaResponse from(Traza traza) {
        return new TrazaResponse(traza.descripcion(), traza.timestamp().toString());
    }
}
