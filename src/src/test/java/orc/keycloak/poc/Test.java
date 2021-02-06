package orc.keycloak.poc;

import org.junit.Ignore;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test {

    @org.junit.Test
    @Ignore
    public void initialization() {
        Keycloak realmClient = Keycloak.getInstance("http://127.0.0.1:5004/auth", "master", "admin", "Password123!", "admin-cli");
        RealmResource master = realmClient.realm("master");

        ArrayList<String> clientIds = new ArrayList<>();
        clientIds.add("webgums-portal-server");
        clientIds.add("webgums-licensing-server");
        clientIds.add("webgums-auditing-server");
        clientIds.add("webgums-support-server");

        for (String clientId : clientIds) {
            ClientsResource clients = master.clients();
            ClientRepresentation clientRepresentation = new ClientRepresentation();
            clientRepresentation.setClientId(clientId);
            clients.create(clientRepresentation);
        }
    }

    @org.junit.Test
    public void initializationClientAndClientScopes() {
        Keycloak realmClient = Keycloak.getInstance("http://127.0.0.1:5004/auth", "master", "admin", "Password123!", "admin-cli");
        RealmResource master = realmClient.realm("master");

        ClientsResource clients = master.clients();
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId("webgums-licensing-server");
        clientRepresentation.setName("webgums-licensing-server");
        clientRepresentation.setRootUrl("http://localhost:5001");

        ArrayList<String> redirectUris = new ArrayList<>();
        redirectUris.add("http://localhost:5001/*");
        clientRepresentation.setRedirectUris(redirectUris);

        clientRepresentation.setAdminUrl("http://localhost:5001");
        ArrayList<String> webOrigins = new ArrayList<>();
        webOrigins.add("http://localhost:5001");
        clientRepresentation.setWebOrigins(webOrigins);

        clientRepresentation.setClientAuthenticatorType("client-secret");
        clientRepresentation.setStandardFlowEnabled(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setBearerOnly(false);
        clientRepresentation.setConsentRequired(false);
        clientRepresentation.setFrontchannelLogout(false);
        clientRepresentation.setFullScopeAllowed(true);
        clientRepresentation.setNotBefore(0);
        clientRepresentation.setNodeReRegistrationTimeout(-1);
        clientRepresentation.setPublicClient(false);
        clientRepresentation.setServiceAccountsEnabled(true);
        clientRepresentation.setSurrogateAuthRequired(false);
        clientRepresentation.setAuthorizationSettings(new ResourceServerRepresentation());
        clients.create(clientRepresentation);


        ClientScopesResource clientScopes = master.clientScopes();
        ClientScopeRepresentation clientScopeRepresentation = new ClientScopeRepresentation();
        clientScopeRepresentation.setName("webgums-licensing-server");
        clientScopeRepresentation.setProtocol("openid-connect");
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("include.in.token.scope", "true");
        attributes.put("display.on.consent.screen", "true");
        clientScopeRepresentation.setAttributes(attributes);

        ArrayList<ProtocolMapperRepresentation> protocolMappers = new ArrayList<>();
        ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        protocolMapperRepresentation.setName("webgums-licensing-server");
        protocolMapperRepresentation.setProtocol("openid-connect");
        protocolMapperRepresentation.setProtocolMapper("oidc-audience-mapper");
        HashMap<String, String> config = new HashMap<>();
        protocolMapperRepresentation.setConfig(config);
        config.put("included.client.audience", "webgums-licensing-server");
        config.put("id.token.claim", "false");
        config.put("access.token.claim", "true");
        protocolMappers.add(protocolMapperRepresentation);
        clientScopeRepresentation.setProtocolMappers(protocolMappers);
        clientScopes.create(clientScopeRepresentation);


//        clientScopeRepresentation = clientScopes.findAll().stream().filter(c -> c.getName().equals("webgums-licensing-server")).findFirst().get();
//        ClientScopeResource clientScopeResource = clientScopes.get(clientScopeRepresentation.getId());
//        ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
//        protocolMapperRepresentation.setName("webgums-licensing-server");
//        protocolMapperRepresentation.setProtocol("openid-connect");
//        protocolMapperRepresentation.setProtocolMapper("oidc-audience-mapper");
//        HashMap<String, String> config = new HashMap<>();
//        protocolMapperRepresentation.setConfig(config);
//        config.put("included.client.audience", "webgums-licensing-server");
//        config.put("id.token.claim", "false");
//        config.put("access.token.claim", "true");
//        clientScopeResource.getProtocolMappers().createMapper(protocolMapperRepresentation);

        // WG::Administrator
        // SES::Reseller employee
        // SES::Reseller administrator
        // SES::Company employee
    }
}
