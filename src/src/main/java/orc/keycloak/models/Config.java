package orc.keycloak.models;

public class Config {
    private Keycloak keycloak;
    private App[] apps;
    private Service[] services;

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public App[] getApps() {
        return apps;
    }

    public Service[] getServices() {
        return services;
    }
}
