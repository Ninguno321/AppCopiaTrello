package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlantillaTarjetaDto(
        @JsonProperty("titulo") String titulo,
        @JsonProperty("fechaVencimiento") String fechaVencimiento,
        @JsonProperty("etiquetas") List<PlantillaEtiquetaDto> etiquetas,
        @JsonProperty("checklist") List<PlantillaItemChecklistDto> checklist
) {}
