package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CrearTableroRequest(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("email")  String email
) {}
