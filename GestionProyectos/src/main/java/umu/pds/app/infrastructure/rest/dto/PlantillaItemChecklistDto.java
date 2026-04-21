package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantillaItemChecklistDto(
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("completado") boolean completado
) {}
