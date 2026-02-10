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
import zenika.oss.stats.beans.gitlab.GitLabMember;
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
            // Inject CSS for red buttons and vertical alignment
            Jt.markdown("""
                <style>
                button[id^="btn_save_"], button[id^="btn_cancel_"] {
                    background-color: #d32f2f !important;
                    color: white !important;
                    border: none;
                    padding: 8px 16px;
                    border-radius: 4px;
                    cursor: pointer;
                    transition: background-color 0.3s;
                }
                button[id^="btn_save_"]:hover, button[id^="btn_cancel_"]:hover {
                    background-color: #b71c1c !important;
                }
                button[id^="btn_save_"]:active, button[id^="btn_cancel_"]:active {
                    background-color: #a93226 !important;
                }
                div[id^="edit_row_"] {
                    align-items: center !important;
                }
                </style>
                """).use(membersTab);

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

                if (m.getId().equals(selectedMemberId)) {
                    var editRow = Jt.columns(6).key("edit_row_" + m.getId()).use(membersTab);

                    String newFirstname = Jt.textInput("Firstname").value(m.getFirstname())
                            .use(editRow.col(0));
                    String newName = Jt.textInput("Name").value(m.getName()).use(editRow.col(1));
                    String newGitLabHandle = Jt.textInput("GitLab").value(m.getGitlabAccount() != null ? m.getGitlabAccount().getUsername() : "").use(editRow.col(2));
                    String newCity = Jt.textInput("City").value(m.getCity()).use(editRow.col(3));

                    if (Jt.button("Save").key("btn_save_" + m.getId()).use(editRow.col(4))) {
                        m.setFirstname(newFirstname);
                        m.setName(newName);
                        if (m.getGitlabAccount() == null) {
                            m.setGitlabAccount(new GitLabMember());
                        }
                        m.getGitlabAccount().setUsername(newGitLabHandle);
                        m.setCity(newCity);
                        firestoreServices.createMember(m);
                        selectedMemberId = null;
                        Jt.success("Successfully updated ✅").use(membersTab);
                        Jt.markdown("<style>#edit_row_" + m.getId() + " { display: none !important; }</style>").use(membersTab);
                    }
                    if (Jt.button("Cancel").key("btn_cancel_" + m.getId()).use(editRow.col(5))) {
                        selectedMemberId = null;
                        Jt.markdown("<style>#edit_row_" + m.getId() + " { display: none !important; }</style>").use(membersTab);
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

            if (!allProjects.isEmpty()) {
                record ProjectDisplay(String name, String fullName, String url, Long stars, Long forks) {
                }
                List<ProjectDisplay> rows = allProjects.stream()
                        .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                p.getWatchers_count(), p.getForks()))
                        .collect(Collectors.toList());

                Jt.subheader("Projects List").use(projectsTab);
                Jt.table(rows).use(projectsTab);
            } else {
                Jt.text("no data available").use(projectsTab);
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
