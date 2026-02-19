package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.services.FirestoreServices;
import org.icepear.echarts.Pie;
import org.icepear.echarts.charts.pie.PieSeries;
import org.icepear.echarts.Bar;
import org.icepear.echarts.charts.bar.BarSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.components.coord.cartesian.ValueAxis;

import java.time.Year;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class StatsTab {

    @Inject
    FirestoreServices firestoreServices;

    public void render(JtContainer statsTab) {
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

            Jt.subheader("🗾 Members by City").use(statsTab);
            if (!members.isEmpty()) {
                Jt.echarts(pie).use(statsTab);
            } else {
                Jt.text("no data available").use(statsTab);
            }

            // Top 3 Projects Section
            try {
                List<zenika.oss.stats.beans.Project> allProjects = firestoreServices.getAllProjects();

                if (!allProjects.isEmpty()) {
                    Jt.subheader("\uD83C\uDFC6 Top 3 Projects by Stars").use(statsTab);

                    record ProjectDisplay(String name, String fullName, String url, Long stars, Long forks) {
                    }

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

            // Top 3 Contributors Section
            renderYearlyContributions(statsTab);
            renderTopContributors(statsTab);

        } catch (Exception e) {
            Jt.warning("Could not load stats: " + e.getMessage()).use(statsTab);
        }
    }

    private void renderYearlyContributions(JtContainer statsTab) {
        Jt.subheader("\uD83D\uDCCA Yearly Contributions").use(statsTab);
        try {
            List<StatsContribution> allStats = firestoreServices.getAllStats();

            List<ZenikaMember> activeMembers = firestoreServices.getAllMembers();
            java.util.Set<String> activeMemberIds = activeMembers.stream()
                    .map(ZenikaMember::getId)
                    .collect(Collectors.toSet());

            Map<String, Long> ghByYear = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdZenikaMember()))
                    .filter(s -> s.getYear() != null && "GitHub".equals(s.getSource()))
                    .collect(Collectors.groupingBy(
                            StatsContribution::getYear,
                            Collectors.summingLong(StatsContribution::getNumberOfContributionsOnGitHub)));

            Map<String, Long> glByYear = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdZenikaMember()))
                    .filter(s -> s.getYear() != null && "GitLab".equals(s.getSource()))
                    .collect(Collectors.groupingBy(
                            StatsContribution::getYear,
                            Collectors.summingLong(StatsContribution::getNumberOfContributionsOnGitLab)));

            List<String> years = allStats.stream()
                    .filter(s -> activeMemberIds.contains(s.getIdZenikaMember()))
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

        Jt.subheader("\uD83E\uDD47 Top 3 Contributors by number of contributions").use(statsTab);

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
        List<ZenikaMember> members = firestoreServices.getAllMembers();

        Map<String, ZenikaMember> membersById = members.stream()
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(
                        ZenikaMember::getId,
                        m -> m,
                        (existing, replacement) -> existing));

        java.util.Set<String> activeMemberIds = members.stream()
                .map(ZenikaMember::getId)
                .collect(Collectors.toSet());

        Map<String, Integer> contributionsByMemberId = stats.stream()
                .filter(s -> s.getIdZenikaMember() != null && activeMemberIds.contains(s.getIdZenikaMember()))
                .collect(Collectors.groupingBy(
                        StatsContribution::getIdZenikaMember,
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
                .limit(3)
                .map(e -> {
                    String memberId = e.getKey();
                    ZenikaMember m = membersById.get(memberId);

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

    record ContributorDisplay(String name, String github, String gitlab, Integer contributions) {
    }
}
