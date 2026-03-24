package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoverTarjetaRequest(
        @JsonProperty("tarjetaId")      String tarjetaId,
        @JsonProperty("listaOrigenId")  String listaOrigenId,
        @JsonProperty("listaDestinoId") String listaDestinoId
) {}
