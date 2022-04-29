# Keycloak Extension Endpoints

The "missing things" from Keycloak Admin REST Endpoints.

## Endpoints

- GET users `/{cloak-x}/users?search=value&q=attr:value`

## How to compile and expose to Keycloak container

1. `mvn clean package`
2. `docker cp <target_dir>/<file.jar> <keycloak-container>:opt/keycloak/providers`
3. Turn off keycloak container and on again to allow Keycloak detect custom providers and build them.