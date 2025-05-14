package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.services.FirestoreServices;

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

}
