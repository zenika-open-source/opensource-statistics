package fr.zenika.opensource.stats.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import fr.zenika.opensource.stats.beans.gitlab.GitLabEvent;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;

import java.util.List;

@RegisterRestClient(configKey = "gitlab-api")
@Path("/")
public interface GitLabClient {

    @GET
    @Path("/users")
    List<GitLabMember> getUserInformations(
            @HeaderParam("Authorization") String authHeader,
            @QueryParam("username") String username);

    @GET
    @Path("/users/{id}")
    GitLabMember getUserInformationById(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("id") String id);

    @GET
    @Path("/users/{id}/projects")
    List<GitLabProject> getProjectsForAnUser(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("id") String id);

    @GET
    @Path("/users/{id}/events")
    List<GitLabEvent> getEventsForAnUser(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("id") String id,
            @QueryParam("after") String after,
            @QueryParam("before") String before,
            @QueryParam("per_page") int perPage,
            @QueryParam("page") int page);

    @GET
    @Path("/merge_requests")
    Response getMergeRequests(
            @HeaderParam("Authorization") String authHeader,
            @QueryParam("author_id") String authorId,
            @QueryParam("state") String state,
            @QueryParam("updated_after") String updatedAfter,
            @QueryParam("updated_before") String updatedBefore,
            @QueryParam("scope") String scope,
            @QueryParam("per_page") int perPage);
}
