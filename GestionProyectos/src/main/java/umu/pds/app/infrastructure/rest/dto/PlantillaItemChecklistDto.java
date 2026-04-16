package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantillaItemChecklistDto(
        @JsonProperty("texto") String texto,
        @JsonProperty("completado") boolean completado
) {}
