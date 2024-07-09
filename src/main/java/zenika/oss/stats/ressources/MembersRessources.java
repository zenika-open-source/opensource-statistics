package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.services.GitHubServices;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Path("/v1/members/")
public class MembersRessources {

    @Inject
    GitHubServices gitHubServices;

    @GET
    @Path("/all")
    public Response getAllMembers() {
        return Response.ok().build();
    }

    @GET
    @Path("/{memberId}")
    public Response getMember(@PathParam("memberId") String memberId) {
        return Response.ok()
                .build();
    }

    @GET
    @Path("/inactives")
    public Response getInactivesMembers() {
        return Response.ok()
                .build();
    }
}
