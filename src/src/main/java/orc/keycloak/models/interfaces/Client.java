package orc.keycloak.models.interfaces;

import java.util.List;

public interface Client {
    String getName();

    String getBaseUrl();

    List<String> getRedirectUris();

    List<String> getWebOrigins();
}
