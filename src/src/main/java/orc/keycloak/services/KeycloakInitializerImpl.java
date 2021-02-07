package orc.keycloak.services;

import orc.keycloak.SpringBootConsoleApplication;
import orc.keycloak.models.App;
import orc.keycloak.models.Config;
import orc.keycloak.models.Service;
import orc.keycloak.models.interfaces.Client;
import orc.keycloak.services.interfaces.KeycloakInitializer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@org.springframework.stereotype.Service
public class KeycloakInitializerImpl implements KeycloakInitializer {

    private final Config config;

    private static Logger LOG = LoggerFactory
            .getLogger(KeycloakInitializerImpl.class);

    public KeycloakInitializerImpl(Config config) {
        this.config = config;
    }

    @Override
    public void execute() {
        orc.keycloak.models.Keycloak keycloak = config.getKeycloak();
        org.keycloak.admin.client.Keycloak realmClient = Keycloak.getInstance(keycloak.getUrl(),
                keycloak.getRealm(),
                keycloak.getUsername(),
                keycloak.getPassword(),
                keycloak.getClientId());

        RealmResource master = realmClient.realm(keycloak.getRealm());
        ClientsResource clientsResource = master.clients();
        ClientScopesResource clientScopesResource = master.clientScopes();

        removeClients(clientsResource);
        removeServicesAndScopes(clientsResource, clientScopesResource);
        addServicesAndScopes(clientsResource, clientScopesResource);
        addClients(clientsResource, clientScopesResource);
        createRoleMappings(clientsResource);
    }

    private void createRoleMappings(ClientsResource clientsResource) {
        App[] clients = config.getApps();
        Service[] services = config.getServices();
        if (services != null && clients != null) {
            LOG.info("Creating roles mappings");
            for (Service service : services) {
                String[] useRolesFrom = service.getUseRolesFrom();
                for (String client : useRolesFrom) {
                    Optional<App> appOptional = Arrays.stream(clients).filter(app -> app.getName().equals(client)).findFirst();
                    if (appOptional.isPresent()) {
                        Optional<ClientRepresentation> clientRepresentationOptional = clientsResource.findAll().stream().filter(c -> c.getName().equals(service.getName())).findFirst();
                        if (clientRepresentationOptional.isPresent()) {
                            LOG.info(String.format("Creating role mapping for service %s for users from client app %s", service.getName(), client));
                            ClientRepresentation clientRepresentation = clientRepresentationOptional.get();
                            ClientResource clientResource = clientsResource.get(clientRepresentation.getId());
                            ProtocolMappersResource protocolMappers = clientResource.getProtocolMappers();
                            ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
                            protocolMapperRepresentation.setName(String.format("User client roles %s", client));
                            protocolMapperRepresentation.setProtocol("openid-connect");
                            protocolMapperRepresentation.setProtocolMapper("oidc-usermodel-client-role-mapper");
                            HashMap<String, String> config = new HashMap<>();
                            config.put("access.token.claim", "true");
                            config.put("claim.name", "roles");
                            config.put("id.token.claim", "true");
                            config.put("jsonType.label", "String");
                            config.put("multivalued", "true");
                            config.put("userinfo.token.claim", "true");
                            config.put("usermodel.clientRoleMapping.clientId", client);
                            protocolMapperRepresentation.setConfig(config);
                            protocolMappers.createMapper(protocolMapperRepresentation);
                        }
                    }
                }
            }

        }
    }

    private void addClients(ClientsResource clientsResource, ClientScopesResource clientScopesResource) {
        App[] clients = config.getApps();
        if (clients != null) {
            LOG.info("Adding clients");
            for (App client : clients) {
                ClientRepresentation clientRepresentation = getClientRepresentation(client);
                LOG.info(String.format("Adding client %s", clientRepresentation.getName()));
                clientsResource.create(clientRepresentation);

                clientRepresentation = clientsResource.findAll().stream().filter(c -> c.getName().equals(client.getName())).findFirst().get();
                ClientResource clientResource = clientsResource.get(clientRepresentation.getId());

                LOG.info(String.format("Creating roles for client %s", client.getName()));
                RolesResource rolesResource = clientResource.roles();
                String[] roles = client.getRoles();
                for (String role : roles) {
                    RoleRepresentation roleRepresentation = new RoleRepresentation();
                    roleRepresentation.setName(role);
                    LOG.info(String.format("Creating role %s for client %s", role, clientRepresentation.getName()));
                    rolesResource.create(roleRepresentation);
                }

                LOG.info(String.format("Adding client scopes for audience for client %s", client.getName()));
                String[] serverAudiences = client.getAllowAccessTo();
                for (String audience : serverAudiences) {
                    Optional<ClientScopeRepresentation> clientScopeRepresentationOptional = clientScopesResource.findAll().stream().filter(c -> c.getName().equals(audience)).findFirst();
                    if (clientScopeRepresentationOptional.isPresent()) {
                        ClientScopeRepresentation clientScopeRepresentation = clientScopeRepresentationOptional.get();
                        LOG.info(String.format("Adding client scope %s for audience for client %s", audience, client.getName()));
                        clientResource.addOptionalClientScope(clientScopeRepresentation.getId());
                    }
                }
            }
        }
    }

    private void addServicesAndScopes(ClientsResource clientsResource, ClientScopesResource clientScopesResource) {
        Service[] servers = config.getServices();
        if (servers != null) {
            LOG.info("Adding servers and client scopes");

            for (Service server : servers) {
                ClientRepresentation clientRepresentation = getClientRepresentation(server);
                clientsResource.create(clientRepresentation);

                ClientScopeRepresentation clientScopeRepresentation = getClientScopeRepresentation(server);
                clientScopesResource.create(clientScopeRepresentation);
            }
        }
    }

    private void removeServicesAndScopes(ClientsResource clientsResource, ClientScopesResource clientScopesResource) {
        Service[] servers = config.getServices();
        if (servers != null) {
            LOG.info("Removing existing servers and client scopes");

            for (Service server : servers) {
                Optional<ClientRepresentation> clientRepresentationOptional = clientsResource.findAll().stream().filter(c -> c.getName().equals(server.getName())).findFirst();
                if (clientRepresentationOptional.isPresent()) {
                    ClientRepresentation clientRepresentation = clientRepresentationOptional.get();
                    LOG.info(String.format("Removing server %s", clientRepresentation.getName()));
                    clientsResource.get(clientRepresentation.getId()).remove();
                }

                Optional<ClientScopeRepresentation> clientScopeRepresentationOptional = clientScopesResource.findAll().stream().filter(c -> c.getName().equals(server.getName())).findFirst();
                if (clientScopeRepresentationOptional.isPresent()) {
                    ClientScopeRepresentation clientScopeRepresentation = clientScopeRepresentationOptional.get();
                    LOG.info(String.format("Removing client scope %s", clientScopeRepresentation.getName()));
                    clientScopesResource.get(clientScopeRepresentation.getId()).remove();
                }
            }
        }
    }

    private void removeClients(ClientsResource clientsResource) {
        App[] clients = config.getApps();
        if (clients != null) {
            LOG.info("Removing existing clients");

            for (App client : clients) {
                Optional<ClientRepresentation> clientRepresentationOptional = clientsResource.findAll().stream().filter(c -> c.getName().equals(client.getName())).findFirst();
                if (clientRepresentationOptional.isPresent()) {
                    ClientRepresentation clientRepresentation = clientRepresentationOptional.get();
                    clientsResource.get(clientRepresentation.getId()).remove();
                }
            }
        }
    }

    private ClientScopeRepresentation getClientScopeRepresentation(Service server) {
        ClientScopeRepresentation clientScopeRepresentation = new ClientScopeRepresentation();
        clientScopeRepresentation.setName(server.getName());
        clientScopeRepresentation.setProtocol("openid-connect");
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("include.in.token.scope", "true");
        attributes.put("display.on.consent.screen", "true");
        clientScopeRepresentation.setAttributes(attributes);

        ArrayList<ProtocolMapperRepresentation> protocolMappers = new ArrayList<>();
        ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        protocolMapperRepresentation.setName(server.getName());
        protocolMapperRepresentation.setProtocol("openid-connect");
        protocolMapperRepresentation.setProtocolMapper("oidc-audience-mapper");
        HashMap<String, String> config = new HashMap<>();
        protocolMapperRepresentation.setConfig(config);
        config.put("included.client.audience", server.getName());
        config.put("id.token.claim", "false");
        config.put("access.token.claim", "true");
        protocolMappers.add(protocolMapperRepresentation);
        clientScopeRepresentation.setProtocolMappers(protocolMappers);

        return clientScopeRepresentation;
    }

    private ClientRepresentation getClientRepresentation(Client client) {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(client.getName());
        clientRepresentation.setName(client.getName());
        clientRepresentation.setRootUrl(client.getBaseUrl());
        clientRepresentation.setRedirectUris(client.getRedirectUris());
        clientRepresentation.setWebOrigins(client.getWebOrigins());
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
        return clientRepresentation;
    }
}
