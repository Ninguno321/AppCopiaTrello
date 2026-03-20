package umu.pds.gestion_proyectos_ui.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableroDto {
    public String id;
    public String nombre;
    public String emailPropietario;
    public boolean bloqueado;
    public List<ListaDto> listas;
    public List<TarjetaDto> tarjetasCompletadas;
}
