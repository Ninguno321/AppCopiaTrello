package umu.pds.app.application.commands;

import java.util.List;

public record PlantillaTarjetaCommand(
        String titulo,
        String fechaVencimiento,
        List<PlantillaEtiquetaCommand> etiquetas,
        List<PlantillaItemChecklistCommand> checklist
) {}
