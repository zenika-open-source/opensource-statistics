package zenika.oss.stats.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import zenika.oss.stats.beans.gitlab.GitLabMember;
import zenika.oss.stats.beans.gitlab.GitLabProject;
import zenika.oss.stats.beans.gitlab.GitLabEvent;

import java.util.List;

@RegisterRestClient(configKey = "gitlab-api")
@Path("/")
public interface GitLabClient {

    default String prepareToken() {
        String token = ConfigProvider.getConfig().getOptionalValue("gitlab.token", String.class).orElse("");
        return "Bearer " + token;
    }

    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitLabClient.prepareToken}")
    @Path("/users")
    List<GitLabMember> getUserInformations(@QueryParam("username") String username);

    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitLabClient.prepareToken}")
    @Path("/users/{id}")
    GitLabMember getUserInformationById(@PathParam("id") String id);

    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitLabClient.prepareToken}")
    @Path("/users/{id}/projects")
    List<GitLabProject> getProjectsForAnUser(@PathParam("id") String id);

    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitLabClient.prepareToken}")
    @Path("/users/{id}/events")
    List<GitLabEvent> getEventsForAnUser(@PathParam("id") String id,
            @QueryParam("after") String after,
            @QueryParam("before") String before,
            @QueryParam("per_page") int perPage,
            @QueryParam("page") int page);

    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitLabClient.prepareToken}")
    @Path("/merge_requests")
    Response getMergeRequests(@QueryParam("author_id") String authorId,
            @QueryParam("state") String state,
            @QueryParam("updated_after") String updatedAfter,
            @QueryParam("updated_before") String updatedBefore,
            @QueryParam("scope") String scope,
            @QueryParam("per_page") int perPage);
}
