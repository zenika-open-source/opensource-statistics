package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.services.GitHubServices;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Path("/v1/organization/")
public class OrganizationsRessources {

    @ConfigProperty(name = "organization.name")
    String organizationName;
    
    @Inject
    GitHubServices gitHubServices;

    @GET
    @Path("/infos")
    public Response getOrganizationInformation() {
        
        return Response.ok(gitHubServices.getOrganizationInformation(organizationName)).build();
    }

}
