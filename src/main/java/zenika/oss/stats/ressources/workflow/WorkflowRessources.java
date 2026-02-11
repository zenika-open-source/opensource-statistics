package zenika.oss.stats.ressources.workflow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.mapper.StatsMapper;
import zenika.oss.stats.mapper.ZenikaMemberMapper;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

import java.util.List;

@ApplicationScoped
@Path("/v1/workflow/")
public class WorkflowRessources {

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    @POST
    @Path("members/save")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveMembers() throws DatabaseException {

        firestoreServices.deleteAllMembers();

        List<GitHubMember> gitHubMembers = gitHubServices.getZenikaOpenSourceMembers();
        // Use a for-loop to properly handle the checked DatabaseException
        for (GitHubMember gitHubMember : gitHubMembers) {
            firestoreServices.createMember(ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember));
        }

        return Response.ok().build();
    }

    @POST
    @Path("forked-projects/save")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveForkedProject() {
        return Response.ok("\uD83D\uDEA7 Not implemented yet").build();
    }

    @POST
    @Path("personal-projects/save")
    @Produces(MediaType.TEXT_PLAIN)
    public Response savePersonalProjects() throws DatabaseException {

        firestoreServices.deleteAllProjects();

        List<ZenikaMember> members = firestoreServices.getAllMembers();

        for (ZenikaMember member : members) {
            List<GitHubProject> gitHubProjects = gitHubServices
                    .getPersonalProjectForAnUser(member.getGitHubAccount().getLogin());

            for (GitHubProject project : gitHubProjects) {
                firestoreServices.createProject(project);
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("stats/save/{year}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveStatsForYear(@PathParam("year") int year) throws DatabaseException {

        firestoreServices.deleteStatsForAllGitHubAccountForAYear(year);

        List<ZenikaMember> zMembers = firestoreServices.getAllMembers();

        for (ZenikaMember zenikaMember : zMembers) {
            if (zenikaMember.getGitHubAccount() != null) {
                System.out.print("\uD83D\uDD0E Check information for " + zenikaMember.getGitHubAccount().getLogin());
                List<CustomStatsContributionsUserByMonth> stats = gitHubServices
                        .getContributionsForTheCurrentYear(zenikaMember.getGitHubAccount().getLogin(), year);
                List<StatsContribution> statsMap = StatsMapper.mapGithubStatisticsToStatsContribution(
                        zenikaMember.getGitHubAccount().getLogin(), year, stats);
                System.out.println("... ✅");

                if (!statsMap.isEmpty()) {
                    for (StatsContribution stat : statsMap) {
                        firestoreServices.saveStatsForAGitHubAccountForAYear(stat);
                    }
                }
            }
        }

        return Response.ok().build();
    }

    @POST
    @Path("stats/save/{githubMember}/{year}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveStatsForAGitHubAccountForAYear(@PathParam("githubMember") String githubMember,
            @PathParam("year") int year) throws DatabaseException {

        firestoreServices.deleteStatsForAGitHubAccountForAYear(githubMember, year);

        List<CustomStatsContributionsUserByMonth> stats = gitHubServices.getContributionsForTheCurrentYear(githubMember,
                year);

        List<StatsContribution> statsMap = StatsMapper.mapGithubStatisticsToStatsContribution(githubMember, year,
                stats);

        if (!statsMap.isEmpty()) {
            for (StatsContribution stat : statsMap) {
                firestoreServices.saveStatsForAGitHubAccountForAYear(stat);
            }
        }

        return Response.ok().build();
    }

}
