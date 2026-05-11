package fr.zenika.opensource.stats.ressources.workflow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.beans.github.GitHubProject;
import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.mapper.MemberMapper;
import fr.zenika.opensource.stats.mapper.StatsMapper;
import fr.zenika.opensource.stats.services.FirestoreServices;
import fr.zenika.opensource.stats.services.GitHubServices;
import fr.zenika.opensource.stats.services.GitLabServices;

@ApplicationScoped
@Path("/v1/workflow/")
public class WorkflowRessources {

    @Inject
    GitHubServices gitHubServices;

    @Inject
    GitLabServices gitLabServices;

    @Inject
    FirestoreServices firestoreServices;

    @POST
    @Path("members/save")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveMembers() throws DatabaseException {

        // Load current state
        List<Member> existingMembers = firestoreServices.getAllMembers();
        List<GitHubMember> gitHubMembers = gitHubServices.getOrganizationMembersFromConfig();

        // Build a set of current GitHub logins in the organization
        Set<String> currentGitHubLogins = gitHubMembers.stream()
                .map(GitHubMember::getLogin)
                .collect(Collectors.toSet());

        // Upsert: add new GitHub members and update existing ones
        for (GitHubMember gitHubMember : gitHubMembers) {
            Member existing = existingMembers.stream()
                    .filter(m -> m.getGitHubAccount() != null
                            && gitHubMember.getLogin().equals(m.getGitHubAccount().getLogin()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                // New member from GitHub organization
                firestoreServices.createMember(MemberMapper.mapGitHubMemberToMember(gitHubMember));
            } else {
                // Keep the same Member (id, city, GitLab, etc.) but refresh GitHub data
                existing.setGitHubAccount(gitHubMember);
                firestoreServices.createMember(existing);
            }
        }

        // Cleanup: remove members whose GitHub account is no longer in the organization
        for (Member member : existingMembers) {
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

        List<Member> members = firestoreServices.getAllMembers();

        for (Member member : members) {
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

        int currentYear = LocalDate.now().getYear();
        boolean isCurrentYear = (year == currentYear);

        if (isCurrentYear) {
            firestoreServices.deleteStatsBySourceForYear(year, "GitHub");
            firestoreServices.deleteStatsBySourceForYear(year, "GitLab");
        }

        List<Member> zMembers = firestoreServices.getAllMembers();

        for (Member member : zMembers) {
            // GitHub
            if (member.getGitHubAccount() != null) {
                // For past years, skip if stats already exist
                if (!isCurrentYear && firestoreServices.hasStatsForMemberAndYear(member.getId(), year, "GitHub")) {
                    System.out.println("⏭️ Skip GitHub information for " + member.getGitHubAccount().getLogin()
                            + " (already exists)");
                } else {
                    System.out.print("🔎 Check GitHub information for " + member.getGitHubAccount().getLogin());
                    List<CustomStatsContributionsUserByMonth> stats = gitHubServices
                            .getContributionsForTheCurrentYear(member.getGitHubAccount().getLogin(), year);
                    List<StatsContribution> statsList = StatsMapper.mapGitHubStatisticsToStatsContributions(
                            member, year, stats);
                    System.out.println("... ✅");

                    if (!statsList.isEmpty()) {
                        for (StatsContribution stat : statsList) {
                            firestoreServices.saveStats(stat);
                        }
                    }
                }
            }

            // GitLab
            if (member.getGitlabAccount() != null) {
                // For past years, skip if stats already exist
                if (!isCurrentYear && firestoreServices.hasStatsForMemberAndYear(member.getId(), year, "GitLab")) {
                    System.out.println("⏭️ Skip GitLab information for " + member.getGitlabAccount().getUsername()
                            + " (already exists)");
                } else {
                    System.out.print("🔎 Check GitLab information for " + member.getGitlabAccount().getUsername());
                    List<CustomStatsContributionsUserByMonth> stats = gitLabServices
                            .getContributionsForTheCurrentYear(member.getGitlabAccount().getUsername(), year);
                    List<StatsContribution> statsList = StatsMapper.mapGitLabStatisticsToStatsContributions(
                            member, year, stats);
                    System.out.println("... ✅");

                    if (!statsList.isEmpty()) {
                        for (StatsContribution stat : statsList) {
                            firestoreServices.saveStats(stat);
                        }
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
        List<Member> members = firestoreServices.getAllMembers();
        Member member = members.stream()
                .filter(m -> m.getGitHubAccount() != null && githubMember.equals(m.getGitHubAccount().getLogin()))
                .findFirst()
                .orElse(null);

        if (member == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Member not found").build();
        }

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
