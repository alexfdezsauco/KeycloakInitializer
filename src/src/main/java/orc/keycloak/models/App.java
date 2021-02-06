package orc.keycloak.models;

import orc.keycloak.models.interfaces.Client;

import java.util.ArrayList;
import java.util.List;

public class App implements Client {

    private String name;
    private String baseUrl;
    private String[] roles;
    private String[] allowAccessTo;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public List<String> getRedirectUris() {
        ArrayList<String> redirectUris = new ArrayList<>();
        redirectUris.add(this.getBaseUrl() + "/*");
        return redirectUris;
    }

    @Override
    public List<String> getWebOrigins() {
        ArrayList<String> webOrigins = new ArrayList<>();
        webOrigins.add(this.getBaseUrl());
        return webOrigins;
    }

    public String[] getRoles() {
        return roles;
    }

    public String[] getAllowAccessTo() {
        return allowAccessTo;
    }
}
