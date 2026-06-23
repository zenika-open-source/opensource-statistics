package fr.zenika.opensource.stats.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.beans.github.GitHubOrganization;
import fr.zenika.opensource.stats.beans.github.GitHubProject;

import java.util.List;

@RegisterRestClient(configKey = "github-api")
@ClientHeaderParam(name = "Accept", value = "application/vnd.github+json")
@ClientHeaderParam(name = "X-GitHub-Api-Version", value = "2022-11-28")
@Path("/")
public interface GitHubClient {

    @GET
    @Path("/orgs/{organizationName}")
    GitHubOrganization getOrgnizationByName(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("organizationName") String organizationName);

    @GET
    @Path("/orgs/{organizationName}/members")
    List<GitHubMember> getOrganizationMembers(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("organizationName") String organizationName,
            @QueryParam("per_page") int perPage,
            @QueryParam("page") int page);

    @GET
    @Path("/user/{login}")
    GitHubMember getUserInformation(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("login") String login);

    @GET
    @Path("/users/{login}/repos")
    List<GitHubProject> getReposForAnUser(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("login") String login,
            @QueryParam("per_page") int perPage,
            @QueryParam("page") int page);

    @GET
    @Path("/orgs/{organizationName}/repos")
    List<GitHubProject> getOrganizationProjects(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("organizationName") String organizationName,
            @QueryParam("per_page") int perPage,
            @QueryParam("page") int page);
}
