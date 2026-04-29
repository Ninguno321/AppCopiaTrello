package umu.pds.app.application.commands;

import java.util.List;

public record PlantillaListaCommand(
        String nombre,
        Integer maxTarjetas,
        List<PlantillaTarjetaCommand> tarjetas
) {}
