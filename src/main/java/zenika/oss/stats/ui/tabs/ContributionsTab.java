package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.mapper.StatsMapper;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

import org.icepear.echarts.Bar;
import org.icepear.echarts.charts.bar.BarSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.components.coord.cartesian.ValueAxis;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContributionsTab {

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    public void render(JtContainer contributionsTab) {

        var columns = Jt.columns(2).key("contributions_columns").use(contributionsTab);

        Jt.subheader("User Contributions History").use(columns.col(0));

        Integer yearValue = Jt.numberInput("Year", Integer.class)
                .minValue(Year.now().getValue() - 5)
                .maxValue(Year.now().getValue() + 1)
                .value(Year.now().getValue())
                .use(columns.col(1));

        if (Jt.button("📈 Sync Contributions for the year selected").use(columns.col(1))) {
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
                Jt.success("✅ Successfully synced contributions for " + syncedCount + " members in " + year + "!")
                        .use(contributionsTab);
            } catch (Exception e) {
                Jt.error("❌ Error syncing contributions: " + e.getMessage()).use(contributionsTab);
            }
        }

        Jt.subheader("Monthly Contributions in " + yearValue).use(contributionsTab);

        try {
            List<StatsContribution> allStats = firestoreServices.getStatsForYear(yearValue);

            Map<Month, Integer> contributionsByMonth = allStats.stream()
                    .collect(Collectors.groupingBy(
                            s -> Month.valueOf(s.getMonth().toUpperCase()),
                            Collectors.summingInt(
                                    s -> s.getNumberOfContributionsOnGitHub() + s.getNumberOfContributionsOnGitLab())));

            List<String> months = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            for (Month m : Month.values()) {
                months.add(m.name());
                counts.add(contributionsByMonth.getOrDefault(m, 0));
            }

            Bar bar = new Bar()
                    .setTooltip("item")
                    .setLegend()
                    .addXAxis(new CategoryAxis().setData(months.toArray(new String[0])))
                    .addYAxis(new ValueAxis())
                    .addSeries(new BarSeries()
                            .setName("Contributions")
                            .setData(counts.toArray(new Integer[0])));

            Jt.echarts(bar).use(contributionsTab);

        } catch (Exception e) {
            Jt.error("Could not load contributions chart: " + e.getMessage()).use(contributionsTab);
        }

        Jt.subheader("Individual Member Stats for " + yearValue).use(contributionsTab);

        try {
            List<ZenikaMember> members = firestoreServices.getAllMembers();
            Map<String, String> memberOptions = members.stream()
                    .filter(m -> m.getGitHubAccount() != null && m.getGitHubAccount().getLogin() != null)
                    .collect(Collectors.toMap(
                            m -> m.getName() + " (" + m.getGitHubAccount().getLogin() + ")",
                            m -> m.getGitHubAccount().getLogin(),
                            (existing, replacement) -> existing));

            String selectedMemberLabel = Jt.selectbox("", new ArrayList<>(memberOptions.keySet()))
                    .use(contributionsTab);

            if (selectedMemberLabel != null) {
                String selectedMemberHandle = memberOptions.get(selectedMemberLabel);

                List<StatsContribution> memberStats = firestoreServices
                        .getContributionsForAMemberOrderByYear(selectedMemberHandle);

                Map<Month, Integer> memberContributionsByMonth = memberStats.stream()
                        .filter(s -> String.valueOf(yearValue).equals(s.getYear()))
                        .collect(Collectors.groupingBy(
                                s -> Month.valueOf(s.getMonth().toUpperCase()),
                                Collectors.summingInt(s -> s.getNumberOfContributionsOnGitHub()
                                        + s.getNumberOfContributionsOnGitLab())));

                List<String> memberMonths = new ArrayList<>();
                List<Integer> memberCounts = new ArrayList<>();

                for (Month m : Month.values()) {
                    memberMonths.add(m.name());
                    memberCounts.add(memberContributionsByMonth.getOrDefault(m, 0));
                }

                Bar memberBar = new Bar()
                        .setTooltip("item")
                        .setLegend()
                        .addXAxis(new CategoryAxis().setData(memberMonths.toArray(new String[0])))
                        .addYAxis(new ValueAxis())
                        .addSeries(new BarSeries()
                                .setName(selectedMemberHandle)
                                .setData(memberCounts.toArray(new Integer[0])));

                Jt.echarts(memberBar).use(contributionsTab);
            }

        } catch (Exception e) {
            Jt.error("Error loading member stats: " + e.getMessage()).use(contributionsTab);
        }

    }
}
