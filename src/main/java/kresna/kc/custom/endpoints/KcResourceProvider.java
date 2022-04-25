package kresna.kc.custom.endpoints;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class KcResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

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
    public Stream<UserModel> users() {
        return session.users().getUsersStream(session.getContext().getRealm());
    }

    private AuthResult checkAuth() {
        AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getIssuedFor() == null) {
            throw new ForbiddenException("Token is not properly issued");
        }

        return auth;
    }
}
