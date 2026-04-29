package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlantillaTarjetaDto(
        @JsonProperty("titulo") String titulo,
        @com.fasterxml.jackson.annotation.JsonAlias({"fechaVencimiento", "fechaLimite"}) String fechaLimite,
        @JsonProperty("etiquetas") List<PlantillaEtiquetaDto> etiquetas,
        @JsonProperty("checklist") List<PlantillaItemChecklistDto> checklist
) {}
