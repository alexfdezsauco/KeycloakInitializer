package orc.keycloak.models;

import orc.keycloak.models.interfaces.Client;

import java.util.ArrayList;
import java.util.List;

public class Service implements Client {

    private String name;
    private String baseUrl;
    private String[] useRolesFrom;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public String[] getUseRolesFrom() {
        return useRolesFrom;
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
}
