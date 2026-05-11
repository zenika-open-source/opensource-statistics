package fr.zenika.opensource.stats.ressources.gitlab;

import fr.zenika.opensource.stats.services.GitLabServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/v1/gitlab/")
public class GitLabRessources {

    @Inject
    GitLabServices gitLabServices;

    @GET
    @Path("/user/{username}")
    public Response getUserInformation(@PathParam("username") String username) {
        return Response.ok(gitLabServices.getUserInformation(username).orElse(null))
                .build();
    }

    @GET
    @Path("/user/{username}/personal-projects")
    public Response getPersonalProjectsForAnUser(@PathParam("username") String username) {
        return Response.ok(gitLabServices.getPersonalProjectsForAnUser(username))
                .build();
    }
}
