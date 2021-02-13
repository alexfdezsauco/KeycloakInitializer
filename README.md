[![Build Status](https://dev.azure.com/alexfdezsauco/External%20Repositories%20Builds/_apis/build/status/alexfdezsauco.KeycloakInitializer?branchName=master)](https://dev.azure.com/alexfdezsauco/External%20Repositories%20Builds/_build/latest?definitionId=5&branchName=master)

Keycloak Initializer
====================

This provides a basic configuration [Keycloak](https://www.keycloak.org/) to ensure the access to the specified services from the specified clients. 

This will create the clients, the roles for such clients, clients scopes for audience for all services, and also create the role mapper for all services. 

## How to use it

1) Create a `config.json` file with a content like this:

        {
            "keycloak": {
                "url": "http(s)://%KEYCLOAK_IP_ADDRESS%/auth",
                "realm": "master",
                "username": "%USERNAME%",
                "password": "%PASSWORD%",
                "clientId": "admin-cli"
            },
            "apps": [
                {
                    "name": "web-client-a",
                    "baseUrl": "http(s)://%WEB-CLIENT-ADDRESS%",
                    "roles": [
                        "Administrator",
                        "User"
                    ],
                    "allowAccessTo": [
                        "service-a",
                        "service-b"
                    ]
                }
            ],
            "services": [
                {
                    "name": "service=a",
                    "baseUrl": "http(s)://%SERVICE-A-ADDRESS%",
                    "useRolesFrom": [
                        "web-client-a"
                    ]
                },
                {
                    "name": "service=b",
                    "baseUrl": "http(s)://%SERVICE-B-ADDRESS%",
                    "useRolesFrom": [
                        "web-client-a"
                        ]
                }
            ]
        }

2) Run with docker using the following command line:

        docker run --rm -v %PATH%\config.json:/app/BOOT-INF/classes/config.json --network bridge alexfdezsauco/keycloak-initializer:latest
