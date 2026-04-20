package umu.pds.app.application.commands;

import java.util.List;

public record PlantillaTableroCommand(
        String nombre,
        List<PlantillaListaCommand> listas
) {}
