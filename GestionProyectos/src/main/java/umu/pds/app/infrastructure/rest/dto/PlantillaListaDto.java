package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlantillaListaDto(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("maxTarjetas") Integer maxTarjetas,
        @JsonProperty("tarjetas") List<PlantillaTarjetaDto> tarjetas,
        @JsonProperty("listasRequeridas") List<String> listasRequeridas
) {}
