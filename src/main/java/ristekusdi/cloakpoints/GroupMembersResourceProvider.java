package ristekusdi.cloakpoints;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.GroupModel;
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
public class GroupMembersResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final AuthResult auth;

    public GroupMembersResourceProvider(KeycloakSession session) {
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
    @Path("groups/{id}/members")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserRepresentation> groupMembers(@PathParam("id") String id,
                                                 @QueryParam("search") String search,
                                                 @QueryParam("q") String searchQuery,
                                                 @QueryParam("first") Integer firstResult,
                                                 @QueryParam("max") Integer maxResults) {
        if (this.auth == null || this.auth.getToken() == null) {
            throw new NotAuthorizedException("Bearer");
        }

        GroupModel group = session.getContext().getRealm().getGroupById(id);
        if (group == null) {
            throw new NotFoundException("Could not find group by id");
        }

//        Map<String, String> attributes = new HashMap<>();

        Map<String, List<String>> searchAttributes = setAttributes(searchQuery);

//        if (search != null) {
//            attributes.put(UserModel.SEARCH, search);
//        }

        Stream<UserModel> membersModel = session.users().getGroupMembersStream(session.getContext().getRealm(), group);
        List<UserRepresentation> users =  membersModel.map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
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
