package zenika.oss.stats.ressources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Member;
import java.util.List;

@Path("/v1/members/")
public class MembersRessources {

    @GET
    @Path("/all")
    public Response getAllMembers() {
        return Response.ok().build();
    }

    @GET
    @Path("/{memberId}")
    public Response getMember(String memberId) {
        return Response.ok()
                .build();
    }

    @GET
    @Path("/inactives")
    public Response getInactivesMembers() {
        return Response.ok()
                .build();
    }
}
