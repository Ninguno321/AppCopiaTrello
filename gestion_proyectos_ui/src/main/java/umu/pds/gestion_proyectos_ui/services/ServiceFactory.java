package umu.pds.gestion_proyectos_ui.services;

public class ServiceFactory {

    private static GestionTableroFrontendService instance;

    private ServiceFactory() {}

    public static GestionTableroFrontendService getGestionTablero() {
        if (instance == null)
            instance = new GestionTableroFrontendServiceImpl();
        return instance;
    }
}
