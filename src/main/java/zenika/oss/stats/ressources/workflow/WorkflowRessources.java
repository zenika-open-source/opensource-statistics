package zenika.oss.stats.ressources.workflow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import zenika.oss.stats.beans.ZenikaMember;
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

import java.util.ArrayList;
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
        gitHubMembers.forEach(gitHubMember -> firestoreServices.createMember(ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember)));

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

        members.forEach(member -> {
            List<GitHubProject> gitHubProjects = gitHubServices.getPersonalProjectForAnUser(member.getGitHubAccount().getLogin());

            gitHubProjects.forEach(project -> {
                firestoreServices.createProject(project);
            });
        });
        return Response.ok().build();
    }

    @POST
    @Path("stats/save/{year}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveStatsForYear(@PathParam("year") int year) throws DatabaseException {
        List<CustomStatsContributionsUserByMonth> stats = new ArrayList<>();

        firestoreServices.deleteStatsForAllGitHubAccountForAYear(year);

        List<ZenikaMember> zMembers = firestoreServices.getAllMembers();

        for (ZenikaMember zenikaMember : zMembers) {
            if (zenikaMember.getGitHubAccount() != null) {
                System.out.println("\uD83D\uDD0E Check information for " + zenikaMember.getGitHubAccount().getLogin());
                stats.addAll(gitHubServices.getContributionsForTheCurrentYear(zenikaMember.getGitHubAccount().getLogin(), year));
                List<StatsContribution> statsMap = StatsMapper.mapGithubStatisticsToStatsContribution(zenikaMember.getGitHubAccount().getLogin(), year, stats);

                System.out.println("\uD83D\uDCBD Save information for " + zenikaMember.getGitHubAccount().getLogin());
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
    public Response saveStatsForAGitHubAccountForAYear(@PathParam("githubMember") String githubMember, @PathParam("year") int year) throws DatabaseException {

        firestoreServices.deleteStatsForAGitHubAccountForAYear(githubMember, year);

        List<CustomStatsContributionsUserByMonth> stats = gitHubServices.getContributionsForTheCurrentYear(githubMember, year);

        List<StatsContribution> statsMap = StatsMapper.mapGithubStatisticsToStatsContribution(githubMember, year, stats);

        if (!statsMap.isEmpty()) {
            for (StatsContribution stat : statsMap) {
                firestoreServices.saveStatsForAGitHubAccountForAYear(stat);
            }
        }

        return Response.ok().build();
    }

}
