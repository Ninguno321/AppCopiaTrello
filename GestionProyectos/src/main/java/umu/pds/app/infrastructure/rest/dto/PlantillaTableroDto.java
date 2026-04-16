package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlantillaTableroDto(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("listas") List<PlantillaListaDto> listas
) {}
