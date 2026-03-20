package umu.pds.app.infrastructure.rest.dto;

import umu.pds.app.domain.modelo.tablero.Etiqueta;

public record EtiquetaResponse(String nombre, String color) {

    public static EtiquetaResponse from(Etiqueta etiqueta) {
        return new EtiquetaResponse(etiqueta.nombre(), etiqueta.color());
    }
}
