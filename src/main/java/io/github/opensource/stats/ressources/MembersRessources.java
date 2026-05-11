package io.github.opensource.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import io.github.opensource.stats.exception.DatabaseException;
import io.github.opensource.stats.services.FirestoreServices;

@ApplicationScoped
@Path("/v1/members/")
public class MembersRessources {

    @Inject
    FirestoreServices firestoreServices;

    @GET
    @Path("/all")
    public Response getAllMembers() throws DatabaseException {
        return Response.ok(firestoreServices.getAllMembers()).build();
    }

    @GET
    @Path("/{memberId}")
    public Response getMember(@PathParam("memberId") String memberId) {
        return Response.ok("\uD83D\uDEA7 Not implemented yet").build();
    }

}
