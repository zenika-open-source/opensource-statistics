package zenika.oss.stats.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import zenika.oss.stats.beans.github.GitHubOrganization;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubProject;

import java.util.List;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey="github-api")
@RegisterClientHeaders(GitHuClientHeaderFactory.class)
@Path("/")
public interface GitHubClient {

    @GET
    @Path("/orgs/{organizationName}")
    GitHubOrganization getOrgnizationByName(@PathParam("organizationName") String organizationName);
    
    @GET
    @Path("/orgs/{organizationName}/members")
    List<GitHubMember> getOrganizationMembers(@PathParam("organizationName") String organizationName, @QueryParam("per_page") int page);

    @GET
    @Path("/user/{login}")
    GitHubMember getUserInformation(@PathParam("login") String login);

    @GET
    @Path("/users/{login}/repos")
    List<GitHubProject> getReposForAnUser(@PathParam("login") String login);
}
