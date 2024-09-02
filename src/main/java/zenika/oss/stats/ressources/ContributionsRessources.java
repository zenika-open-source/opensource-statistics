package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/v1/contributions/")
public class ContributionsRessources {
    /**
     * Get contributions for one member.
     *
     * @param memberId of the member requested
     * @return
     */
    @GET
    @Path("/contributions/member/{memberId}")
    public Response getContributionsByMember(@PathParam("memberId") String memberId) {

        return Response.ok()
                .build();

    }

    @GET
    @Path("/contributions/month/{month}")
    public Response getContributionsForAMonth(@PathParam("month") int month) {

        return Response.ok()
                .build();
    }
}
