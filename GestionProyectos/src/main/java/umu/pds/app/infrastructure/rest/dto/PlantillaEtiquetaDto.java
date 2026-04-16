package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantillaEtiquetaDto(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("color") String color
) {}
