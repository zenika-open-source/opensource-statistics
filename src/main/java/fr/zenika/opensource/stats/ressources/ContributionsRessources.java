package fr.zenika.opensource.stats.ressources;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.beans.ExternalContribution;
import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.services.ExternalContributionService;
import fr.zenika.opensource.stats.services.FirestoreServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/v1/contributions/")
public class ContributionsRessources {
    private final FirestoreServices firestoreServices;
    private final ExternalContributionService externalContributionService;

    @jakarta.inject.Inject
    public ContributionsRessources(FirestoreServices firestoreServices,
                                  ExternalContributionService externalContributionService) {
        this.firestoreServices = firestoreServices;
        this.externalContributionService = externalContributionService;
    }

    @GET
    @Path("/member/{memberId}")
    public Response getContributionsByMember(@PathParam("memberId") String memberId) throws DatabaseException {
        return Response.ok(firestoreServices.getContributionsForAMemberOrderByYear(memberId)).build();
    }

    @GET
    @Path("/")
    public Response getContributionsForAMonth(@QueryParam("year") int year, @QueryParam("month") String month)
            throws DatabaseException {
        return Response.ok(firestoreServices.getContributionsForAYearAndMonthOrderByMonth(year, month)).build();
    }

    @GET
    @Path("/details")
    public Response getContributionsDetails(@QueryParam("year") Integer year,
            @QueryParam("memberId") String memberId) throws DatabaseException {
        List<StatsContribution> stats;
        if (memberId != null && !memberId.isBlank()) {
            stats = firestoreServices.getContributionsForMember(memberId);
            if (stats == null || stats.isEmpty()) {
                stats = firestoreServices.getContributionsForAMemberOrderByYear(memberId);
            }
            if (year != null) {
                String yearStr = String.valueOf(year);
                stats = Optional.ofNullable(stats).orElse(List.of()).stream()
                        .filter(s -> yearStr.equals(s.getYear()))
                        .collect(Collectors.toList());
            }
        } else if (year != null) {
            stats = firestoreServices.getStatsForYear(year);
        } else {
            stats = firestoreServices.getAllStats();
        }

        List<ContributionDetail> details = Optional.ofNullable(stats).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(StatsContribution::getContributionsDetails)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .sorted((d1, d2) -> {
                    String c1 = Optional.ofNullable(d1.getCreatedAt()).orElse("");
                    String c2 = Optional.ofNullable(d2.getCreatedAt()).orElse("");
                    int cmp = c2.compareTo(c1);
                    if (cmp != 0) {
                        return cmp;
                    }
                    return Optional.ofNullable(d1.getPrTitle()).orElse("")
                            .compareTo(Optional.ofNullable(d2.getPrTitle()).orElse(""));
                })
                .collect(Collectors.toList());

        return Response.ok(details).build();
    }

    @GET
    @Path("/external")
    public Response getExternalContributions(
            @QueryParam("year") Integer year,
            @QueryParam("state") String state,
            @QueryParam("memberId") String memberId,
            @QueryParam("limit") Integer limit) throws DatabaseException {
        List<ExternalContribution> contributions = externalContributionService.getExternalContributions(
                year, state, memberId, limit);
        return Response.ok(contributions).build();
    }

}

