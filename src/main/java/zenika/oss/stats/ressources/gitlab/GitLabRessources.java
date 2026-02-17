package zenika.oss.stats.ressources.gitlab;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.services.GitLabServices;

@ApplicationScoped
@Path("/v1/gitlab/")
public class GitLabRessources {

    @Inject
    GitLabServices gitLabServices;

    @GET
    @Path("/user/{username}")
    public Response getUserInformation(@PathParam("username") String username) {
        return Response.ok(gitLabServices.getUserInformation(username))
                .build();
    }

    @GET
    @Path("/user/{username}/personal-projects")
    public Response getPersonalProjectsForAnUser(@PathParam("username") String username) {
        return Response.ok(gitLabServices.getPersonalProjectsForAnUser(username))
                .build();
    }
}
