package kresna.kc.custom.endpoints;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.utils.SearchQueryUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class KcResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final AuthResult auth;

    public KcResourceProvider(KeycloakSession session) {
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
                                          @QueryParam("username") String username,
                                          @QueryParam("q") String searchQuery) {
        if (this.auth == null || this.auth.getToken() == null) {
            throw new NotAuthorizedException("Bearer");
        }

        Map<String, String> attributes = new HashMap<>();

        Map<String, String> searchAttributes = searchQuery == null ? Collections.emptyMap() : SearchQueryUtils.getFields(searchQuery);

        if (search != null || username != null) {
            if (search != null) {
                attributes.put(UserModel.SEARCH, search);
            }

            if (username != null) {
                attributes.put(UserModel.USERNAME, username);
            }

//            attributes.putAll(searchAttributes);
        }

        Stream<UserModel> userModels = session.users().searchForUserStream(session.getContext().getRealm(), attributes);
        List<UserRepresentation> users =  userModels.map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
                .collect(Collectors.toList());

        return users;
//        return session.users().getUsersStream(session.getContext().getRealm())
//                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
//                .collect(Collectors.toList());
    }

}
