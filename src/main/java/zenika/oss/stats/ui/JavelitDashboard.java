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
import java.util.Map;
import java.util.stream.Collectors;

import org.icepear.echarts.Pie;
import org.icepear.echarts.charts.pie.PieSeries;

@ApplicationScoped
public class JavelitDashboard {

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    private String selectedMemberId;

    void onStart(@Observes StartupEvent ev) {
        Server.builder(() -> {
            Jt.header("📊 Opensource Statistics Dashbord").use();
            Jt.subheader("Welcome to the Zenika Open Source contributions dashboard").use();
            Jt.markdown(
                    "This dashboard get publics datas from GitHub (and GitLab as soon)")
                    .use();

            var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Projects", "📊 Contributions")).use();

            renderMembersTab(tabs.tab("🙋 Members"));
            renderProjectsTab(tabs.tab("🚀 Projects"));
            renderContributionsTab(tabs.tab("📊 Contributions"));

        }, 8888).build().start();
    }

    private void renderMembersTab(JtContainer membersTab) {

        try {
            List<ZenikaMember> members = firestoreServices.getAllMembers();

            var columns = Jt.columns(2).key("members_columns").use(membersTab);

            Jt.subheader("Zenika Members (" + members.size() + ")").use(columns.col(0));

            if (Jt.button("🔄 Sync Members from GitHub").use(columns.col(1))) {
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

            Map<String, Long> cityStats = members.stream()
                    .collect(Collectors.groupingBy(m -> m.getCity() == null ? "Unknown" : m.getCity(),
                            Collectors.counting()));

            Object[] data = cityStats.entrySet().stream()
                    .map(e -> new Object[] { e.getKey(), e.getValue() })
                    .toArray();

            Jt.subheader("Zenika Members List").use(membersTab);
            var header = Jt.columns(6).key("member_header").use(membersTab);
            Jt.text("Firstname").use(header.col(0));
            Jt.text("Lastname").use(header.col(1));
            Jt.text("GitHub").use(header.col(2));
            Jt.text("GitLab").use(header.col(3));
            Jt.text("City").use(header.col(4));
            Jt.text("Actions").use(header.col(5));

            for (ZenikaMember m : members) {
                var row = Jt.columns(6).key("member_row_" + m.getId()).use(membersTab);
                Jt.text(m.getFirstname() != null ? m.getFirstname() : "").use(row.col(0));
                Jt.text(m.getName() != null ? m.getName() : "").use(row.col(1));
                Jt.text(m.getGitHubAccount() != null ? m.getGitHubAccount().getLogin() : "").use(row.col(2));
                Jt.text(m.getGitlabAccount() != null ? m.getGitlabAccount().getUsername() : "").use(row.col(3));
                Jt.text(m.getCity() != null ? m.getCity() : "").use(row.col(4));
                if (Jt.button("📝").key("btn_edit_" + m.getId()).use(row.col(5))) {
                    selectedMemberId = m.getId();
                }
            }

            if (selectedMemberId != null) {
                ZenikaMember memberToEdit = members.stream()
                        .filter(m -> m.getId().equals(selectedMemberId))
                        .findFirst().orElse(null);
                if (memberToEdit != null) {
                    Jt.subheader("Edit Member: " + memberToEdit.getId()).use(membersTab);

                    String newFirstname = Jt.textInput("Firstname").value(memberToEdit.getFirstname())
                            .use(membersTab);
                    String newName = Jt.textInput("Name").value(memberToEdit.getName()).use(membersTab);
                    String newCity = Jt.textInput("City").value(memberToEdit.getCity()).use(membersTab);

                    var actions = Jt.columns(2).key("edit_actions").use(membersTab);
                    if (Jt.button("Save").use(actions.col(0))) {
                        memberToEdit.setFirstname(newFirstname);
                        memberToEdit.setName(newName);
                        memberToEdit.setCity(newCity);
                        firestoreServices.createMember(memberToEdit);
                        selectedMemberId = null;
                        Jt.success("Successfully updated " + memberToEdit.getId()).use(membersTab);
                    }
                    if (Jt.button("Cancel").use(actions.col(1))) {
                        selectedMemberId = null;
                    }
                }
            }

            Pie pie = new Pie()
                    .setTooltip("item")
                    .setLegend();

            pie.addSeries(new PieSeries()
                    .setName("Members by City")
                    .setRadius("50%")
                    .setData(data));

            Jt.subheader("Members by City").use(membersTab);
            if (members.size() > 0) {
                Jt.echarts(pie).use(membersTab);
            } else {
                Jt.text("no data available").use(membersTab);
            }

        } catch (Exception e) {
            Jt.warning("Could not load current members: " + e.getMessage()).use(membersTab);
        }
    }

    private void renderProjectsTab(JtContainer projectsTab) {
        try {
            List<GitHubProject> allProjects = firestoreServices.getAllProjects();

            var columns = Jt.columns(2).key("projects_columns").use(projectsTab);
            Jt.subheader("User Projects (" + allProjects.size() + ")").use(columns.col(0));

            if (Jt.button("🚀 Sync Personal Projects").use(columns.col(1))) {
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

            Jt.text("Fetch and save personal projects (not forked) for all saved members.").use(projectsTab);

        } catch (Exception e) {
            Jt.warning("Could not load current projects").use(projectsTab);
        }
    }

    private void renderContributionsTab(JtContainer contributionsTab) {
        var columns = Jt.columns(2).key("contributions_columns").use(contributionsTab);

        Jt.subheader("User Contributions History").use(columns.col(0));

        Integer yearValue = Jt.numberInput("Year", Integer.class)
                .minValue(Year.now().getValue() - 5)
                .maxValue(Year.now().getValue() + 1)
                .value(Year.now().getValue())
                .use(columns.col(1));

        if (Jt.button("📈 Sync Contributions for " + yearValue).use(columns.col(1))) {
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

        Jt.text("Fetch and save contribution statistics for the specified year.").use(contributionsTab);
    }
}
