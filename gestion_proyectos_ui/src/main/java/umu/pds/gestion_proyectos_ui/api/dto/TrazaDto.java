package umu.pds.gestion_proyectos_ui.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrazaDto {
    public String descripcion;
    public String timestamp;
}
