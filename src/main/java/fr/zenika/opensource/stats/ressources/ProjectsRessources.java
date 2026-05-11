package fr.zenika.opensource.stats.ressources;

import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.services.FirestoreServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/v1/projects/")
public class ProjectsRessources {

    @Inject
    FirestoreServices firestoreServices;

    @GET
    @Path("/all")
    public Response getAllProjects() throws DatabaseException {
        return Response.ok(firestoreServices.getAllProjects()).build();
    }

    @GET
    @Path("/{memberId}")
    public Response getAllProjectsForAnUser(@PathParam("memberId") String memberId) {
        return Response.ok("\uD83D\uDEA7 Not implemented yet").build();
    }

    @GET
    @Path("/organization")
    public Response getOrganizationProjects() throws DatabaseException {
        return Response.ok(firestoreServices.getAllProjects().stream()
                .filter(p -> "GitHub Organization".equals(p.getSource()))
                .collect(java.util.stream.Collectors.toList())).build();
    }

}
