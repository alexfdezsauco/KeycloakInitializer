package orc.keycloak.models;

public class Keycloak {
    private String url;
    private String realm;
    private String username;
    private String password;
    private String clientId;

    public String getUrl() {
        return url;
    }

    public String getRealm() {
        return realm;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }
}
