package zenika.oss.stats.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubOrganization;
import zenika.oss.stats.beans.github.GitHubProject;

import java.util.List;

@RegisterRestClient(configKey="github-api")
@ClientHeaderParam(name = "Accept", value = "application/vnd.github+json")
@ClientHeaderParam(name = "X-GitHub-Api-Version", value = "2022-11-28")
@Path("/")
public interface GitHubClient {
    
    default String prepareToken() {
        String token = ConfigProvider.getConfig().getValue("github.token", String.class);
        return "Bearer " + token;
    }

    @GET
    @Path("/orgs/{organizationName}")
    GitHubOrganization getOrgnizationByName(@PathParam("organizationName") String organizationName);
    
    @GET
    @ClientHeaderParam(name = "Authorization", value = "{zenika.oss.stats.config.GitHubClient.prepareToken}")
    @Path("/orgs/{organizationName}/members")
    List<GitHubMember> getOrganizationMembers(@PathParam("organizationName") String organizationName, @QueryParam("per_page") int page);

    @GET
    @Path("/user/{login}")
    GitHubMember getUserInformation(@PathParam("login") String login);

    @GET
    @Path("/users/{login}/repos")
    List<GitHubProject> getReposForAnUser(@PathParam("login") String login);
}
