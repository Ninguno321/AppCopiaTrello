package umu.pds.gestion_proyectos_ui.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaDto {
    public String id;
    public String nombre;
    public List<TarjetaDto> tarjetas;
}
