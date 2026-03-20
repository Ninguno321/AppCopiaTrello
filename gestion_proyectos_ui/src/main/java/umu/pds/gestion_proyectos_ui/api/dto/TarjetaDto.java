package umu.pds.gestion_proyectos_ui.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TarjetaDto {
    public String id;
    public String titulo;
    public String descripcion;
    public boolean completada;
    public List<EtiquetaDto> etiquetas;
    public boolean tieneTarea;
    public boolean tieneChecklist;
    public ChecklistDto checklist;
}
