package zenika.oss.stats.ui;

import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.javelit.core.JtContainer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.mapper.ZenikaMemberMapper;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.mapper.StatsMapper;

import java.time.Year;
import java.util.List;

@ApplicationScoped
public class JavelitDashboard {

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    void onStart(@Observes StartupEvent ev) {
        Server.builder(() -> {
            Jt.header("Opensource Statistics Dashbord 📊").use();
            Jt.markdown(
                    "Welcome to the Zenika Open Source contributions dashboard. Use the tabs below to manage and sync data.")
                    .use();

            var tabs = Jt.tabs(List.of("Members", "Projects", "Contributions")).use();

            renderMembersTab(tabs.tab("Members"));
            renderProjectsTab(tabs.tab("Projects"));
            renderContributionsTab(tabs.tab("Contributions"));

        }, 8888).build().start();
    }

    private void renderMembersTab(JtContainer membersTab) {
        Jt.subheader("Zenika Members").use(membersTab);

        if (Jt.button("🔄 Sync Members from GitHub").use(membersTab)) {
            try {
                firestoreServices.deleteAllMembers();
                List<GitHubMember> gitHubMembers = gitHubServices.getZenikaOpenSourceMembers();
                gitHubMembers.forEach(gitHubMember -> firestoreServices
                        .createMember(ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember)));
                Jt.success("Successfully synced " + gitHubMembers.size() + " members!").use(membersTab);
            } catch (Exception e) {
                Jt.error("Error syncing members: " + e.getMessage()).use(membersTab);
            }
        }

        try {
            List<ZenikaMember> members = firestoreServices.getAllMembers();
            Jt.text("**Total Members:** " + members.size()).use(membersTab);

            if (!members.isEmpty()) {
                var membersExpander = Jt.expander("Show Members List (" + members.size() + ")").use(membersTab);

                StringBuilder sb = new StringBuilder();
                sb.append("| Firstname | Lastname | GitHub handle | GitLab handle |\n");
                sb.append("| --- | --- | --- | --- |\n");

                for (ZenikaMember m : members) {
                    String githubHandle = m.getGitHubAccount() != null ? m.getGitHubAccount().getLogin() : "";
                    String gitlabHandle = m.getGitlabAccount() != null ? m.getGitlabAccount().getUsername() : "";
                    String firstname = m.getFirstname() != null ? m.getFirstname() : "";
                    String lastname = m.getName() != null ? m.getName() : "";

                    sb.append(String.format("| %s | %s | %s | %s |\n", firstname, lastname, githubHandle,
                            gitlabHandle));
                }
                Jt.markdown(sb.toString()).use(membersExpander);
            }

        } catch (Exception e) {
            Jt.warning("Could not load current members: " + e.getMessage()).use(membersTab);
        }
    }

    private void renderProjectsTab(JtContainer projectsTab) {
        Jt.subheader("User Projects Management").use(projectsTab);

        try {
            List<GitHubProject> allProjects = firestoreServices.getAllProjects();
            Jt.text("**Total Projects:** " + allProjects.size()).use(projectsTab);
        } catch (Exception e) {
            Jt.warning("Could not load current projects").use(projectsTab);
        }

        Jt.text("Fetch and save personal projects (not forked) for all saved members.").use(projectsTab);

        if (Jt.button("🚀 Sync Personal Projects").use(projectsTab)) {
            try {
                firestoreServices.deleteAllProjects();
                List<ZenikaMember> members = firestoreServices.getAllMembers();
                int totalProjects = 0;
                for (ZenikaMember member : members) {
                    if (member.getGitHubAccount() != null) {
                        List<GitHubProject> gitHubProjects = gitHubServices
                                .getPersonalProjectForAnUser(member.getGitHubAccount().getLogin());
                        gitHubProjects.forEach(firestoreServices::createProject);
                        totalProjects += gitHubProjects.size();
                    }
                }
                Jt.success("Successfully synced " + totalProjects + " projects for " + members.size() + " members!")
                        .use(projectsTab);
            } catch (Exception e) {
                Jt.error("Error syncing projects: " + e.getMessage()).use(projectsTab);
            }
        }
    }

    private void renderContributionsTab(JtContainer contributionsTab) {
        Jt.subheader("User Contributions History").use(contributionsTab);

        Integer yearValue = Jt.numberInput("Year", Integer.class)
                .minValue(Year.now().getValue() - 5)
                .maxValue(Year.now().getValue() + 1)
                .value(Year.now().getValue())
                .use(contributionsTab);

        Jt.text("Fetch and save contribution statistics for the specified year.").use(contributionsTab);

        if (Jt.button("📈 Sync Contributions for " + yearValue).use(contributionsTab)) {
            try {
                int year = yearValue;
                firestoreServices.deleteStatsForAllGitHubAccountForAYear(year);
                List<ZenikaMember> zMembers = firestoreServices.getAllMembers();

                int syncedCount = 0;
                for (ZenikaMember zenikaMember : zMembers) {
                    if (zenikaMember.getGitHubAccount() != null) {
                        List<CustomStatsContributionsUserByMonth> stats = gitHubServices
                                .getContributionsForTheCurrentYear(zenikaMember.getGitHubAccount().getLogin(),
                                        year);
                        List<StatsContribution> statsMap = StatsMapper.mapGithubStatisticsToStatsContribution(
                                zenikaMember.getGitHubAccount().getLogin(), year, stats);

                        if (!statsMap.isEmpty()) {
                            for (StatsContribution stat : statsMap) {
                                firestoreServices.saveStatsForAGitHubAccountForAYear(stat);
                            }
                        }
                        syncedCount++;
                    }
                }
                Jt.success("Successfully synced contributions for " + syncedCount + " members in " + year + "!")
                        .use(contributionsTab);
            } catch (Exception e) {
                Jt.error("Error syncing contributions: " + e.getMessage()).use(contributionsTab);
            }
        }
    }
}
