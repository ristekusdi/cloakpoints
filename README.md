# Keycloak Extension Endpoints

The "missing things" from Keycloak Admin REST Endpoints.

## Backgrounds

Keycloak has been implement Admin REST API to get several cases that we need as below:
- get list of users,
- get list of members belong to a group,
- get list of users belong to a client role.

But, those cases doesn't have search ability and although `get list of users` has search ability but it doesn't work with search and searchQuery (search by attributes).

This library has cover this case.

## Endpoints

- GET list of users `/{cloak-x}/users?search=value&q=attr:value`
- GET list of members belong to a group `/{cloak-x}/groups/{id}/members?search=value&q=attr:value`
- GET list of users belong to a client role `/{cloak-x}/clients/{id}/roles/{role-name}/users?search=value&q=attr:value`

Note: `search` param works with username, first or last name or email and `q` works with user attribute.

## How to compile and expose to Keycloak container

1. `mvn clean package`
2. `docker cp <target_dir>/<file.jar> <keycloak-container>:opt/keycloak/providers`
3. Turn off keycloak container and on again to allow Keycloak detect custom providers and build them.

Another link: https://www.keycloak.org/server/containers#_building_your_optimized_keycloak_docker_image