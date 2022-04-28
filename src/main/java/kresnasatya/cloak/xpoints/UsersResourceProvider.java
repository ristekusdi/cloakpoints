package kresnasatya.cloak.xpoints;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class UsersResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final AuthResult auth;

    public UsersResourceProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserRepresentation> users(@QueryParam("search") String search,
                                          @QueryParam("q") String searchQuery,
                                          @QueryParam("first") Integer firstResult,
                                          @QueryParam("max") Integer maxResults) {
        if (this.auth == null || this.auth.getToken() == null) {
            throw new NotAuthorizedException("Bearer");
        }

        Map<String, String> attributes = new HashMap<>();

        Map<String, List<String>> searchAttributes = setAttributes(searchQuery);

        if (search != null) {
            attributes.put(UserModel.SEARCH, search);
        }

        Stream<UserModel> userModels = session.users().searchForUserStream(session.getContext().getRealm(), attributes);
        List<UserRepresentation> users =  userModels.map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
                .collect(Collectors.toList());

        List<UserRepresentation> selectedUsers = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : searchAttributes.entrySet()) {
            for (UserRepresentation user: users) {
                if (user.getAttributes().containsKey(entry.getKey())) {
                    if (user.getAttributes().containsValue(entry.getValue())) {
                        selectedUsers.add(user);
                    }
                }
            }
        }

        if (firstResult == null || firstResult <= 0) {
            firstResult = 0;
        }

        if (maxResults == null || maxResults <= 0) {
            maxResults = 20;
        }

        return selectedUsers.stream().distinct().skip(firstResult).limit(maxResults).collect(Collectors.toList());
    }


    private Map<String, List<String>> setAttributes(String q) {
        String[] splitKv = q.split(" ");
        Map<String, List<String>> attributes = new HashMap<>();
        for (String item: splitKv) {
            String[] kv = item.split(":");

            List<String> vList = new ArrayList<>(Arrays.asList(kv[1].trim().split(",")));
            attributes.put(kv[0].trim(), vList);
        }
        return attributes;
    }
}
