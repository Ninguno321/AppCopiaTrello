package umu.pds.app.application.commands;

public record PlantillaItemChecklistCommand(
        String texto,
        boolean completado
) {}
