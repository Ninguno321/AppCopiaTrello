package umu.pds.gestion_proyectos_ui.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChecklistDto {
    public String id;
    public String nombre;
    public List<ItemChecklistDto> items;
}
