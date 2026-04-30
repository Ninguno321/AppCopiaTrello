package umu.pds.app.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ConfigurarPrerequisitosRequest(
        @JsonProperty("listasRequeridas") List<String> listasRequeridas
) {}
