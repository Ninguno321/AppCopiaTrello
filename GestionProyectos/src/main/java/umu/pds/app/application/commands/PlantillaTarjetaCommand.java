package umu.pds.app.application.commands;

import java.util.List;

public record PlantillaTarjetaCommand(
        String titulo,
        String fechaLimite,
        List<PlantillaEtiquetaCommand> etiquetas,
        List<PlantillaItemChecklistCommand> checklist
) {}
