package umu.pds.app.application.commands;

public record PlantillaItemChecklistCommand(
        String descripcion,
        boolean completado
) {}
