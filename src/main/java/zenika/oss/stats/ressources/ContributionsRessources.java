package zenika.oss.stats.ressources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.services.FirestoreServices;

@ApplicationScoped
@Path("/v1/contributions/")
public class ContributionsRessources {
    private final FirestoreServices firestoreServices;

    @jakarta.inject.Inject
    public ContributionsRessources(FirestoreServices firestoreServices) {
        this.firestoreServices = firestoreServices;
    }

    /**
     * Get contributions for one member.
     *
     * @param memberId of the member requested
     * @return
     */
    @GET
    @Path("/contributions/member/{memberId}")
    public Response getContributionsByMember(@PathParam("memberId") String memberId) throws DatabaseException {
        return Response.ok(firestoreServices.getContributionsForAMemberOrderByYear(memberId)).build();
    }

    @GET
    @Path("/contributions/month/{month}")
    public Response getContributionsForAMonth(@PathParam("month") int month) {

        return Response.ok()
                .build();
    }
}
