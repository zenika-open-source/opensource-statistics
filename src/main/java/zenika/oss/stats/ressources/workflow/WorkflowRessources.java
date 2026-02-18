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
import java.util.Set;
import java.util.stream.Collectors;

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

        // Load current state
        List<ZenikaMember> existingMembers = firestoreServices.getAllMembers();
        List<GitHubMember> gitHubMembers = gitHubServices.getZenikaOpenSourceMembers();

        // Build a set of current GitHub logins in the organization
        Set<String> currentGitHubLogins = gitHubMembers.stream()
                .map(GitHubMember::getLogin)
                .collect(Collectors.toSet());

        // Upsert: add new GitHub members and update existing ones
        for (GitHubMember gitHubMember : gitHubMembers) {
            ZenikaMember existing = existingMembers.stream()
                    .filter(m -> m.getGitHubAccount() != null
                            && gitHubMember.getLogin().equals(m.getGitHubAccount().getLogin()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                // New member from GitHub organization
                firestoreServices.createMember(ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember));
            } else {
                // Keep the same ZenikaMember (id, city, GitLab, etc.) but refresh GitHub data
                existing.setGitHubAccount(gitHubMember);
                firestoreServices.createMember(existing);
            }
        }

        // Cleanup: remove members whose GitHub account is no longer in the organization
        for (ZenikaMember member : existingMembers) {
            if (member.getGitHubAccount() != null
                    && !currentGitHubLogins.contains(member.getGitHubAccount().getLogin())) {
                firestoreServices.deleteMember(member.getId());
            }
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

        firestoreServices.deleteStatsBySourceForYear(year, "GitHub");

        List<ZenikaMember> zMembers = firestoreServices.getAllMembers();

        for (ZenikaMember zenikaMember : zMembers) {
            if (zenikaMember.getGitHubAccount() != null) {
                System.out.print("🔎 Check information for " + zenikaMember.getGitHubAccount().getLogin());
                List<CustomStatsContributionsUserByMonth> stats = gitHubServices
                        .getContributionsForTheCurrentYear(zenikaMember.getGitHubAccount().getLogin(), year);
                List<StatsContribution> statsList = StatsMapper.mapGitHubStatisticsToStatsContributions(
                        zenikaMember, year, stats);
                System.out.println("... ✅");

                if (!statsList.isEmpty()) {
                    for (StatsContribution stat : statsList) {
                        firestoreServices.saveStats(stat);
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

        // Find member by handle to get ID
        List<ZenikaMember> members = firestoreServices.getAllMembers();
        ZenikaMember member = members.stream()
                .filter(m -> m.getGitHubAccount() != null && githubMember.equals(m.getGitHubAccount().getLogin()))
                .findFirst()
                .orElse(null);

        if (member == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Member not found").build();
        }

        // Specific delete for this member is tricky with the new deterministic ID
        // without a specific method,
        // but for now we can just use the Upsert behavior of set() in saveStats.
        // Or we could implement deleteStatsByMemberAndSourceForYear.

        List<CustomStatsContributionsUserByMonth> stats = gitHubServices.getContributionsForTheCurrentYear(githubMember,
                year);

        List<StatsContribution> statsList = StatsMapper.mapGitHubStatisticsToStatsContributions(member, year,
                stats);

        if (!statsList.isEmpty()) {
            for (StatsContribution stat : statsList) {
                firestoreServices.saveStats(stat);
            }
        }

        return Response.ok().build();
    }
}
