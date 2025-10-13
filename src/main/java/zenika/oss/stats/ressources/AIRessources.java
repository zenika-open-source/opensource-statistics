package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.mcp.GitHubAIService;

@ApplicationScoped
@Path("/v1/ai/")
public class AIRessources {

    @Inject
    GitHubAIService gitHubAIService;

    @GET
    @Path("/profile/{userHandle}")
    public Response getProfile(@PathParam("userHandle") String userHandle) {
        return Response.ok(gitHubAIService.getGitHubProfile(userHandle)).build();
    }

}
