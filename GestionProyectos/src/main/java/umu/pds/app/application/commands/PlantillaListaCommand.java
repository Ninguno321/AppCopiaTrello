package umu.pds.app.application.commands;

import java.util.List;

public record PlantillaListaCommand(
        String nombre,
        List<PlantillaTarjetaCommand> tarjetas
) {}
