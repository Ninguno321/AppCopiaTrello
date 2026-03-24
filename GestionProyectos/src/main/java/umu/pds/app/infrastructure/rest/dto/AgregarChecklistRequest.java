package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgregarChecklistRequest(@JsonProperty("nombre") String nombre) {}
