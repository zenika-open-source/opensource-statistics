package fr.zenika.opensource.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.Project;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.services.FirestoreServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.icepear.echarts.Pie;
import org.icepear.echarts.charts.pie.PieSeries;
import org.icepear.echarts.Bar;
import org.icepear.echarts.charts.bar.BarSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.components.coord.cartesian.ValueAxis;
import io.quarkus.logging.Log;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class StatsTab {

    @Inject
    FirestoreServices firestoreServices;

    @Inject
    fr.zenika.opensource.stats.services.ExternalContributionService externalContributionService;

    @ConfigProperty(name = "organization.name", defaultValue = "zenika-open-source")
    String organizationName;

    @ConfigProperty(name = "organization.display-name")
    String organizationDisplayName;

    public void render(JtContainer statsTab) {
        try {
            List<Member> members = firestoreServices.getAllMembers();

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

            Jt.subheader("🗾 Members by City").use(statsTab);
            if (!members.isEmpty()) {
                Jt.echarts(pie).use(statsTab);
            } else {
                Jt.text("no data available").use(statsTab);
            }

            // Yearly Contributions & Top Contributors
            renderYearlyContributions(statsTab);

            // Top 10 Latest Created Projects (Diagnostic Mode)
            try {
                List<Project> allProjects = firestoreServices.getAllProjects();

                List<Project> orgProjects = allProjects.stream()
                        .filter(p -> "GitHub Organization".equals(p.getSource()))
                        .collect(Collectors.toList());

                if (!allProjects.isEmpty()) {
                    record LatestProjectDisplay(String Name, String URL, String Created_At) {
                    }
                    List<LatestProjectDisplay> latestOrgProjects = orgProjects.stream()
                            .sorted((p1, p2) -> {
                                String d1 = p1.getCreated_at() != null ? p1.getCreated_at() : "";
                                String d2 = p2.getCreated_at() != null ? p2.getCreated_at() : "";
                                return d2.compareTo(d1);
                            })
                            .limit(10)
                            .map(p -> new LatestProjectDisplay(
                                    p.getName() != null ? p.getName() : "Unknown",
                                    p.getHtml_url() != null ? p.getHtml_url() : "",
                                    (p.getCreated_at() != null && p.getCreated_at().contains("T"))
                                            ? p.getCreated_at().split("T")[0]
                                            : "N/A"))
                            .collect(Collectors.toList());

                    if (!latestOrgProjects.isEmpty()) {
                        Jt.subheader("🆕 Top 10 Newest Organization Projects").use(statsTab);
                        Jt.table(latestOrgProjects).key("latest_org_projects_table").use(statsTab);
                    }
                }
            } catch (Exception e) {
                Log.error("Could not load latest projects", e);
            }

            // Top 5 Projects Section
            try {
                List<Project> allProjects = firestoreServices.getAllProjects();

                if (!allProjects.isEmpty()) {
                    Jt.subheader("\uD83C\uDFC6 Top 5 Community Projects by Stars").use(statsTab);

                    record ProjectDisplay(String Name, String Full_Name, String URL, Long Stars, Long Forks) {
                    }

                    List<ProjectDisplay> topCommunityProjects = allProjects.stream()
                            .filter(p -> !"GitHub Organization".equals(p.getSource()))
                            .sorted((p1, p2) -> Long.compare(
                                    p2.getWatchers_count() != null ? p2.getWatchers_count() : 0L,
                                    p1.getWatchers_count() != null ? p1.getWatchers_count() : 0L))
                            .limit(5)
                            .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                    p.getWatchers_count(), p.getForks()))
                            .collect(Collectors.toList());

                    Jt.table(topCommunityProjects).key("top_community_projects_table").use(statsTab);

                    List<ProjectDisplay> topOrgProjects = allProjects.stream()
                            .filter(p -> "GitHub Organization".equals(p.getSource()))
                            .sorted((p1, p2) -> Long.compare(
                                    p2.getWatchers_count() != null ? p2.getWatchers_count() : 0L,
                                    p1.getWatchers_count() != null ? p1.getWatchers_count() : 0L))
                            .limit(5)
                            .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                    p.getWatchers_count(), p.getForks()))
                            .collect(Collectors.toList());

                    if (!topOrgProjects.isEmpty()) {
                        Jt.subheader("🏆 Top 5 " + organizationDisplayName + " Open Source Projects by Stars")
                                .use(statsTab);
                        Jt.table(topOrgProjects).key("top_org_projects_table").use(statsTab);
                    }
                }
            } catch (Exception e) {
                Jt.warning("Could not load top projects: " + e.getMessage()).use(statsTab);
            }

            // Top 10 Contributed Projects (by PR/MR created)
            renderTopContributedProjects(statsTab);

            renderTopContributors(statsTab);

        } catch (Exception e) {
            Jt.warning("Could not load stats: " + e.getMessage()).use(statsTab);
        }
    }

    private void renderTopContributedProjects(JtContainer statsTab) {
        Jt.subheader("🌐 Top 10 Most Contributed External Projects (by PR/MR created)").use(statsTab);
        try {
            List<StatsContribution> allStats = firestoreServices.getAllStats();
            List<Member> activeMembers = firestoreServices.getAllMembers();
            List<Project> allProjects = firestoreServices.getAllProjects();

            Set<String> activeMemberIds = activeMembers.stream()
                    .map(Member::getId)
                    .collect(Collectors.toSet());

            Set<String> memberLogins = activeMembers.stream()
                    .flatMap(m -> {
                        List<String> logins = new ArrayList<>();
                        if (m.getGitHubAccount() != null && m.getGitHubAccount().getLogin() != null) {
                            logins.add(m.getGitHubAccount().getLogin().toLowerCase().trim());
                        }
                        if (m.getGitlabAccount() != null && m.getGitlabAccount().getUsername() != null) {
                            logins.add(m.getGitlabAccount().getUsername().toLowerCase().trim());
                        }
                        return logins.stream();
                    })
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

            Set<String> orgPrefixes = new HashSet<>();
            if (organizationName != null && !organizationName.isBlank()) {
                orgPrefixes.add(organizationName.toLowerCase().trim());
            }
            if (organizationDisplayName != null && !organizationDisplayName.isBlank()) {
                orgPrefixes.add(organizationDisplayName.toLowerCase().trim());
            }
            orgPrefixes.add("zenika");
            orgPrefixes.add("zenika-open-source");

            Set<String> knownInternalFullNames = allProjects.stream()
                    .map(Project::getFull_name)
                    .filter(Objects::nonNull)
                    .map(s -> s.toLowerCase().trim())
                    .collect(Collectors.toSet());

            Set<String> knownInternalUrls = allProjects.stream()
                    .map(Project::getHtml_url)
                    .filter(Objects::nonNull)
                    .map(s -> s.toLowerCase().trim().replaceAll("/+$", ""))
                    .collect(Collectors.toSet());

            List<StatsContribution> activeStats = allStats.stream()
                    .filter(s -> s.getIdMember() != null && activeMemberIds.contains(s.getIdMember()))
                    .collect(Collectors.toList());

            List<String> availableYears = activeStats.stream()
                    .map(StatsContribution::getYear)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            List<String> filterOptions = new ArrayList<>();
            filterOptions.add("All Time");
            filterOptions.addAll(availableYears);

            var filterRow = Jt.columns(3).key("top_contributed_projects_filter_row").use(statsTab);
            Object prevPeriod = Jt.componentsState().get("top_projects_period_select");
            int prevIdx = prevPeriod != null ? filterOptions.indexOf(prevPeriod.toString()) : 0;
            String selectedPeriod = Jt.selectbox("Filter by Year", new ArrayList<>(filterOptions))
                    .index(prevIdx >= 0 ? prevIdx : 0)
                    .key("top_projects_period_select")
                    .use(filterRow.col(0));

            List<StatsContribution> filteredStats = activeStats;
            if (selectedPeriod != null && !"All Time".equals(selectedPeriod)) {
                filteredStats = activeStats.stream()
                        .filter(s -> selectedPeriod.equals(s.getYear()))
                        .collect(Collectors.toList());
            }

            record DetailItem(String projectName, String projectUrl, String source, String memberId) {
            }

            List<DetailItem> details = filteredStats.stream()
                    .flatMap(stat -> Optional.ofNullable(stat.getContributionsDetails())
                            .orElse(List.of())
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(d -> isExternalProject(
                                    d.getProjectFullName(),
                                    d.getProjectUrl(),
                                    orgPrefixes,
                                    memberLogins,
                                    knownInternalFullNames,
                                    knownInternalUrls))
                            .map(d -> {
                                String name = Optional.ofNullable(d.getProjectFullName())
                                        .filter(s -> !s.isBlank())
                                        .orElse(Optional.ofNullable(d.getProjectName()).orElse("Unknown"));
                                String url = Optional.ofNullable(d.getProjectUrl()).orElse("");
                                return new DetailItem(name, url, stat.getSource(), stat.getIdMember());
                            }))
                    .filter(d -> !"Unknown".equals(d.projectName()))
                    .collect(Collectors.toList());

            if (details.isEmpty()) {
                String emptyMsg = (selectedPeriod != null && !"All Time".equals(selectedPeriod))
                        ? "No external PR/MR contribution details available for " + selectedPeriod + "."
                        : "No external PR/MR contribution details available yet. Run a contribution sync to populate project details.";
                Jt.text(emptyMsg).use(statsTab);
                return;
            }

            Map<String, List<DetailItem>> byProject = details.stream()
                    .collect(Collectors.groupingBy(DetailItem::projectName));

            record TopProjectDisplay(
                    int Rank,
                    String Project,
                    String URL,
                    String Source,
                    Long PR_MR_Count,
                    Long Contributors) {
            }

            List<Map.Entry<String, List<DetailItem>>> sortedProjects = byProject.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .limit(10)
                    .collect(Collectors.toList());

            List<TopProjectDisplay> top10 = new ArrayList<>();
            int rank = 1;
            for (Map.Entry<String, List<DetailItem>> entry : sortedProjects) {
                String proj = entry.getKey();
                List<DetailItem> items = entry.getValue();
                String url = items.stream()
                        .map(DetailItem::projectUrl)
                        .filter(u -> !u.isBlank())
                        .findFirst()
                        .orElse("");
                String source = items.stream()
                        .map(DetailItem::source)
                        .filter(s -> s != null && !s.isBlank())
                        .findFirst()
                        .orElse("");
                long distinctContributors = items.stream()
                        .map(DetailItem::memberId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                top10.add(new TopProjectDisplay(rank++, proj, url, source, (long) items.size(), distinctContributors));
            }

            // ECharts horizontal bar chart for the top projects
            List<String> chartProjects = new ArrayList<>();
            List<Long> chartCounts = new ArrayList<>();
            // Reverse order so rank 1 appears at top of horizontal chart
            for (int i = top10.size() - 1; i >= 0; i--) {
                chartProjects.add(top10.get(i).Project());
                chartCounts.add(top10.get(i).PR_MR_Count());
            }

            Bar bar = new Bar()
                    .setTooltip("axis")
                    .addYAxis(new CategoryAxis().setData(chartProjects.toArray(new String[0])))
                    .addXAxis(new ValueAxis())
                    .addSeries(new BarSeries()
                            .setName("PR / MR Count")
                            .setData(chartCounts.toArray(new Number[0])));

            Jt.echarts(bar).use(statsTab);
            Jt.table(top10).key("top_contributed_projects_table").use(statsTab);

        } catch (Exception e) {
            Jt.warning("Could not load top contributed projects: " + e.getMessage()).use(statsTab);
            Log.error("Error loading top contributed projects", e);
        }
    }

    private boolean isExternalProject(String projectFullName, String projectUrl,
            Set<String> orgPrefixes, Set<String> memberLogins,
            Set<String> knownInternalFullNames, Set<String> knownInternalUrls) {
        return externalContributionService.isExternalProject(projectFullName, projectUrl, "",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls);
    }

    private void renderYearlyContributions(JtContainer statsTab) {
        Jt.subheader("\uD83D\uDCCA Yearly Contributions").use(statsTab);
        try {
            List<StatsContribution> allStats = firestoreServices.getAllStats();

            List<Member> activeMembers = firestoreServices.getAllMembers();
            java.util.Set<String> activeMemberIds = activeMembers.stream()
                    .map(Member::getId)
                    .collect(Collectors.toSet());

            Map<String, Long> ghByYear = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdMember()))
                    .filter(s -> s.getYear() != null && "GitHub".equals(s.getSource()))
                    .collect(Collectors.groupingBy(
                            StatsContribution::getYear,
                            Collectors.summingLong(StatsContribution::getNumberOfContributionsOnGitHub)));

            Map<String, Long> glByYear = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdMember()))
                    .filter(s -> s.getYear() != null && "GitLab".equals(s.getSource()))
                    .collect(Collectors.groupingBy(
                            StatsContribution::getYear,
                            Collectors.summingLong(StatsContribution::getNumberOfContributionsOnGitLab)));

            List<String> years = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdMember()))
                    .map(StatsContribution::getYear)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            List<Long> ghCounts = years.stream().map(y -> ghByYear.getOrDefault(y, 0L)).collect(Collectors.toList());
            List<Long> glCounts = years.stream().map(y -> glByYear.getOrDefault(y, 0L)).collect(Collectors.toList());

            if (!years.isEmpty()) {
                Bar bar = new Bar()
                        .setTooltip("axis")
                        .setLegend()
                        .addXAxis(new CategoryAxis().setData(years.toArray(new String[0])))
                        .addYAxis(new ValueAxis())
                        .addSeries(new BarSeries()
                                .setName("GitHub")
                                .setStack("total")
                                .setData(ghCounts.toArray(new Number[0])))
                        .addSeries(new BarSeries()
                                .setName("GitLab")
                                .setStack("total")
                                .setData(glCounts.toArray(new Number[0])));

                Jt.echarts(bar).use(statsTab);
            } else {
                Jt.text("No yearly data available").use(statsTab);
            }
        } catch (Exception e) {
            Jt.warning("Could not load yearly contributions: " + e.getMessage()).use(statsTab);
        }
    }

    private void renderTopContributors(JtContainer statsTab) {
        int currentYear = Year.now().getValue();
        int previousYear = currentYear - 1;

        Jt.subheader("\uD83E\uDD47 Top 5 Contributors by number of contributions").use(statsTab);

        var columns = Jt.columns(2).key("top_contributors_columns").use(statsTab);

        // Current Year
        try {
            List<ContributorDisplay> topCurrent = getTopContributors(currentYear);
            Jt.markdown("## " + currentYear).use(columns.col(0));
            if (!topCurrent.isEmpty()) {
                Jt.table(topCurrent).use(columns.col(0));
            } else {
                Jt.text("No data available").use(columns.col(0));
            }
        } catch (Exception e) {
            Jt.error("Error loading " + currentYear + " stats: " + e.getMessage()).use(columns.col(0));
        }

        // Previous Year
        try {
            List<ContributorDisplay> topPrevious = getTopContributors(previousYear);
            Jt.markdown("## " + previousYear).use(columns.col(1));
            if (!topPrevious.isEmpty()) {
                Jt.table(topPrevious).use(columns.col(1));
            } else {
                Jt.text("No data available").use(columns.col(1));
            }
        } catch (Exception e) {
            Jt.error("Error loading " + previousYear + " stats: " + e.getMessage()).use(columns.col(1));
        }
    }

    private List<ContributorDisplay> getTopContributors(int year) throws Exception {
        List<StatsContribution> stats = firestoreServices.getStatsForYear(year);
        List<Member> members = firestoreServices.getAllMembers();

        Map<String, Member> membersById = members.stream()
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(
                        Member::getId,
                        m -> m,
                        (existing, replacement) -> existing));

        java.util.Set<String> activeMemberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toSet());

        Map<String, Integer> contributionsByMemberId = stats.stream()
                .filter(s -> s.getIdMember() != null && activeMemberIds.contains(s.getIdMember()))
                .collect(Collectors.groupingBy(
                        StatsContribution::getIdMember,
                        Collectors.summingInt(s -> {
                            if ("GitHub".equals(s.getSource())) {
                                return s.getNumberOfContributionsOnGitHub();
                            } else if ("GitLab".equals(s.getSource())) {
                                return s.getNumberOfContributionsOnGitLab();
                            }
                            return 0;
                        })));

        return contributionsByMemberId.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String memberId = e.getKey();
                    Member m = membersById.get(memberId);

                    String name = "Unknown (" + memberId + ")";
                    String github = "";
                    String gitlab = "";

                    if (m != null) {
                        String firstName = m.getFirstname() != null ? m.getFirstname() : "";
                        String lastName = m.getName() != null ? m.getName() : "";
                        String fullName = (firstName + " " + lastName).trim();
                        name = fullName.isEmpty() ? m.getId() : fullName;

                        if (m.getGitHubAccount() != null && m.getGitHubAccount().getLogin() != null) {
                            github = m.getGitHubAccount().getLogin();
                        }

                        if (m.getGitlabAccount() != null && m.getGitlabAccount().getUsername() != null) {
                            gitlab = m.getGitlabAccount().getUsername();
                        }
                    }

                    return new ContributorDisplay(name, github, gitlab, e.getValue());
                })
                .collect(Collectors.toList());
    }

    record ContributorDisplay(String Name, String GitHub, String GitLab, Integer Contributions) {
    }
}
