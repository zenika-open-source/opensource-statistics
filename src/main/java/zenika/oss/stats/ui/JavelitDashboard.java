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
import java.util.Comparator;
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
    private boolean showMembersTable = false;
    private String memberSearchTerm = "";
    private String projectSearchTerm = "";

    // Sorting state for projects
    private String projectSortColumn = "Name";
    private boolean projectSortAscending = true;

    // Sorting state for members
    private String memberSortColumn = "Firstname";
    private boolean memberSortAscending = true;

    void onStart(@Observes StartupEvent ev) {
        Server.builder(() -> {
            Jt.header("📊 Opensource Statistics Dashbord").use();
            Jt.subheader("Welcome to the Zenika Open Source contributions dashboard").use();
            Jt.markdown(
                    "This dashboard get publics datas from GitHub (and GitLab as soon)")
                    .use();

            var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Projects", "📊 Contributions", "📈 Stats")).use();

            renderMembersTab(tabs.tab("🙋 Members"));
            renderProjectsTab(tabs.tab("🚀 Projects"));
            renderContributionsTab(tabs.tab("📊 Contributions"));
            renderStatsTab(tabs.tab("📈 Stats"));

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

            Jt.markdown("<br/>").use(membersTab);
            String toggleLabel = showMembersTable ? "▼ Hide Members List" : "▶ Show Members List";
            if (Jt.button(toggleLabel).use(membersTab)) {
                showMembersTable = !showMembersTable;
            }

            if (showMembersTable) {
                // Search Bar
                var searchRow = Jt.columns(2).use(membersTab);
                memberSearchTerm = Jt.textInput("Search Members").value(memberSearchTerm).use(searchRow.col(0));
                // Removed explicit search button

                // Filter
                List<ZenikaMember> filteredMembers = members.stream()
                    .filter(m -> matchesMember(m, memberSearchTerm))
                    .collect(Collectors.toList());

                // Sort
                sortMembers(filteredMembers);

                Jt.subheader("Zenika Members List").use(membersTab);
                var header = Jt.columns(6).key("member_header").use(membersTab);
                
                if (Jt.button(getMemberSortLabel("Firstname")).use(header.col(0))) {
                    toggleMemberSort("Firstname");
                }
                if (Jt.button(getMemberSortLabel("Lastname")).use(header.col(1))) {
                    toggleMemberSort("Lastname");
                }
                Jt.text("GitHub").use(header.col(2));
                Jt.text("GitLab").use(header.col(3));
                if (Jt.button(getMemberSortLabel("City")).use(header.col(4))) {
                    toggleMemberSort("City");
                }
                Jt.text("Actions").use(header.col(5));

                for (ZenikaMember m : filteredMembers) {
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
                // Search Bar
                var searchRow = Jt.columns(2).use(projectsTab);
                projectSearchTerm = Jt.textInput("Search Projects").value(projectSearchTerm).use(searchRow.col(0));
                // Removed explicit search button

                List<GitHubProject> filteredProjects = allProjects.stream()
                    .filter(p -> matchesProject(p, projectSearchTerm))
                    .collect(Collectors.toList());

                // Sort
                sortProjects(filteredProjects);

                record ProjectDisplay(String name, String fullName, String url, Long stars, Long forks) {
                }
                List<ProjectDisplay> rows = filteredProjects.stream()
                        .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                p.getWatchers_count(), p.getForks()))
                        .collect(Collectors.toList());

                Jt.subheader("Projects List").use(projectsTab);
                
                // Custom Header with Sort Buttons
                var header = Jt.columns(5).key("projects_header").use(projectsTab);
                
                if (Jt.button(getSortLabel("Name")).use(header.col(0))) {
                    toggleSort("Name");
                }
                Jt.text("Full Name").use(header.col(1));
                Jt.text("URL").use(header.col(2));
                if (Jt.button(getSortLabel("Stars")).use(header.col(3))) {
                    toggleSort("Stars");
                }
                if (Jt.button(getSortLabel("Forks")).use(header.col(4))) {
                    toggleSort("Forks");
                }

                // Custom Table Rows (replacing Jt.table to match header)
                for (ProjectDisplay p : rows) {
                    var row = Jt.columns(5).use(projectsTab);
                    Jt.text(p.name()).use(row.col(0));
                    Jt.text(p.fullName()).use(row.col(1));
                    Jt.text(p.url()).use(row.col(2));
                    Jt.text(String.valueOf(p.stars())).use(row.col(3));
                    Jt.text(String.valueOf(p.forks())).use(row.col(4));
                }

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

    private boolean matchesMember(ZenikaMember m, String term) {
        if (term == null || term.isBlank()) return true;
        String lowerTerm = term.toLowerCase();
        return (m.getFirstname() != null && m.getFirstname().toLowerCase().contains(lowerTerm)) ||
               (m.getName() != null && m.getName().toLowerCase().contains(lowerTerm)) ||
               (m.getCity() != null && m.getCity().toLowerCase().contains(lowerTerm)) ||
               (m.getGitHubAccount() != null && m.getGitHubAccount().getLogin().toLowerCase().contains(lowerTerm)) ||
               (m.getGitlabAccount() != null && m.getGitlabAccount().getUsername().toLowerCase().contains(lowerTerm));
    }

    private boolean matchesProject(GitHubProject p, String term) {
        if (term == null || term.isBlank()) return true;
        String lowerTerm = term.toLowerCase();
        return (p.getName() != null && p.getName().toLowerCase().contains(lowerTerm)) ||
               (p.getFull_name() != null && p.getFull_name().toLowerCase().contains(lowerTerm)) ||
               (p.getHtml_url() != null && p.getHtml_url().toLowerCase().contains(lowerTerm));
    }

    private void toggleSort(String column) {
        if (projectSortColumn.equals(column)) {
            projectSortAscending = !projectSortAscending;
        } else {
            projectSortColumn = column;
            projectSortAscending = true;
        }
    }

    private String getSortLabel(String column) {
        if (projectSortColumn.equals(column)) {
            return column + (projectSortAscending ? " ▲" : " ▼");
        }
        return column;
    }

    private void sortProjects(List<GitHubProject> projects) {
        Comparator<GitHubProject> comparator = switch (projectSortColumn) {
            case "Name" -> Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : "");
            case "Stars" -> Comparator.comparing(GitHubProject::getWatchers_count);
            case "Forks" -> Comparator.comparing(GitHubProject::getForks);
            default -> Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : "");
        };

        if (!projectSortAscending) {
            comparator = comparator.reversed();
        }
        projects.sort(comparator);
    }

    private void toggleMemberSort(String column) {
        if (memberSortColumn.equals(column)) {
            memberSortAscending = !memberSortAscending;
        } else {
            memberSortColumn = column;
            memberSortAscending = true;
        }
    }

    private String getMemberSortLabel(String column) {
        if (memberSortColumn.equals(column)) {
            return column + (memberSortAscending ? " ▲" : " ▼");
        }
        return column;
    }

    private void sortMembers(List<ZenikaMember> members) {
        Comparator<ZenikaMember> comparator = switch (memberSortColumn) {
            case "Firstname" -> Comparator.comparing(m -> m.getFirstname() != null ? m.getFirstname().toLowerCase() : "");
            case "Lastname" -> Comparator.comparing(m -> m.getName() != null ? m.getName().toLowerCase() : "");
            case "City" -> Comparator.comparing(m -> m.getCity() != null ? m.getCity().toLowerCase() : "");
            default -> Comparator.comparing(m -> m.getFirstname() != null ? m.getFirstname().toLowerCase() : "");
        };

        if (!memberSortAscending) {
            comparator = comparator.reversed();
        }
        members.sort(comparator);
    }

    private void renderStatsTab(JtContainer statsTab) {
        try {
            List<ZenikaMember> members = firestoreServices.getAllMembers();

            Map<String, Long> cityStats = members.stream()
                    .collect(Collectors.groupingBy(m -> m.getCity() == null ? "Unknown" : m.getCity(),
                            Collectors.counting()));

            Object[] data = cityStats.entrySet().stream()
                    .map(e -> Map.of("name", e.getKey(), "value", e.getValue()))
                    .toArray();

            Pie pie = new Pie()
                    .setTooltip("item")
                    .setLegend();

            pie.addSeries(new PieSeries()
                    .setName("Members by City")
                    .setRadius("50%")
                    .setData(data));

            Jt.subheader("Members by City").use(statsTab);
            if (!members.isEmpty()) {
                Jt.echarts(pie).use(statsTab);
            } else {
                Jt.text("no data available").use(statsTab);
            }

            // Top 3 Projects Section
            try {
                List<GitHubProject> allProjects = firestoreServices.getAllProjects();
                
                if (!allProjects.isEmpty()) {
                    Jt.subheader("\uD83C\uDFC6 Top 3 Projects by Stars").use(statsTab);
                    
                    record ProjectDisplay(String name, String fullName, String url, Long stars, Long forks) {}
                    
                    List<ProjectDisplay> topProjects = allProjects.stream()
                            .sorted((p1, p2) -> Long.compare(p2.getWatchers_count(), p1.getWatchers_count()))
                            .limit(3)
                            .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                    p.getWatchers_count(), p.getForks()))
                            .collect(Collectors.toList());
                            
                    Jt.table(topProjects).use(statsTab);
                }
            } catch (Exception e) {
                Jt.warning("Could not load top projects: " + e.getMessage()).use(statsTab);
            }

        } catch (Exception e) {
            Jt.warning("Could not load stats: " + e.getMessage()).use(statsTab);
        }
    }
}
