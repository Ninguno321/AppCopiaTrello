package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantillaItemChecklistDto(
        @com.fasterxml.jackson.annotation.JsonAlias({"texto", "descripcion"}) String descripcion,
        @JsonProperty("completado") boolean completado
) {}
