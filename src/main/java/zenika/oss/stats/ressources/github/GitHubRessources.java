package zenika.oss.stats.ressources.github;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.services.GitHubServices;

import java.time.Year;
import java.util.Calendar;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Path("/v1/github/")
public class GitHubRessources {

    @ConfigProperty(name = "organization.name")
    String organizationName;
    
    @Inject
    GitHubServices gitHubServices;

    @GET
    @Path("/organization/infos")
    public Response getOrganizationInformation() {
        return Response.ok(gitHubServices.getOrganizationInformation(organizationName)).build();
    }


    @GET
    @Path("/organization/members")
    public Response getOrganizationMembers() {
        return Response.ok(gitHubServices.getOrganizationMembers(organizationName)).build();
    }
    
    @GET
    @Path("/user/{id}")
    public Response getUserInformation(@PathParam("id") String id) {
        return Response.ok(gitHubServices.getUserInformation(id)).build();
    }

    @GET
    @Path("user/{login}/personal-projects")
    public Response getPersonalProjectForAnUser(@PathParam("login") String login) {

        return Response.ok(gitHubServices.getPersonalProjectForAnUser(login))
                .build();
    }

    @GET
    @Path("user/{login}/forked-projects")
    public Response getForkedProjectsForAnUser(@PathParam("login") String login) {

        return Response.ok(gitHubServices.getForkedProjectForAnUser(login))
                .build();
    }

    @GET
    @Path("user/{login}/contributions/year/current")
    public Response getContributionsData(@PathParam("login") String login) {
        return Response.ok(gitHubServices.getContributionsForTheCurrentYear(login, Year.now().getValue())).build();
    }


    @GET
    @Path("user/{login}/contributions-dynamic")
    public Response getContributionsDataDynamic(@PathParam("login") String login) {
        return Response.ok(gitHubServices.getContributionsDataDynamic(login)).build();
    }



}
