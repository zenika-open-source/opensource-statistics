package fr.zenika.opensource.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.mapper.StatsMapper;
import fr.zenika.opensource.stats.services.FirestoreServices;
import fr.zenika.opensource.stats.services.GitHubServices;
import fr.zenika.opensource.stats.services.GitLabServices;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.icepear.echarts.Bar;
import org.icepear.echarts.charts.bar.BarSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.components.coord.cartesian.ValueAxis;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContributionsTab {

        @Inject
        GitHubServices gitHubServices;

        @Inject
        GitLabServices gitLabServices;

        @Inject
        FirestoreServices firestoreServices;

        @ConfigProperty(name = "oss.stats.sync.buttons.enabled", defaultValue = "false")
        boolean syncButtonsEnabled;

        public void render(JtContainer contributionsTab) {

                Jt.subheader("User Contributions History").use(contributionsTab);

                var controlRow = Jt.columns(2).key("contributions_controls_row").use(contributionsTab);

                Integer yearValue = Jt.numberInput("Year", Integer.class)
                                .key("year_selection")
                                .minValue(Year.now().getValue() - 5)
                                .maxValue(Year.now().getValue() + 1)
                                .value(Year.now().getValue())
                                .use(controlRow.col(0));

                if (syncButtonsEnabled) {
                        if (Jt.button("🔄 Sync Contributions").use(controlRow.col(1))) {
                                try {
                                        int year = yearValue;
                                        int currentYear = Year.now().getValue();

                                        List<Member> zMembers = firestoreServices.getAllMembers();
                                        int ghSyncedCount = 0;
                                        int glSyncedCount = 0;

                                        for (Member member : zMembers) {
                                                // GitHub Sync
                                                if (member.getGitHubAccount() != null
                                                                && member.getGitHubAccount().getLogin() != null) {
                                                        // For past years (older than previous year), skip if stats already exist
                                                        boolean shouldSkip = (year < currentYear - 1)
                                                                        && firestoreServices.hasStatsForMemberAndYear(
                                                                                        member.getId(), year,
                                                                                        "GitHub");
                                                        if (shouldSkip) {
                                                                // Already exists, skip
                                                        } else {
                                                                List<CustomStatsContributionsUserByMonth> ghStats = gitHubServices
                                                                                .getContributionsForTheCurrentYear(
                                                                                                member.getGitHubAccount()
                                                                                                                .getLogin(),
                                                                                                year);

                                                                if (!ghStats.isEmpty()) {
                                                                        List<StatsContribution> statsList = StatsMapper
                                                                                        .mapGitHubStatisticsToStatsContributions(
                                                                                                        member,
                                                                                                        year, ghStats);
                                                                        for (StatsContribution stat : statsList) {
                                                                                firestoreServices.saveStats(stat);
                                                                        }
                                                                        ghSyncedCount++;
                                                                }
                                                        }
                                                }

                                                // GitLab Sync
                                                if (member.getGitlabAccount() != null
                                                                && member.getGitlabAccount()
                                                                                .getUsername() != null) {
                                                        // For past years (older than previous year), skip if stats already exist
                                                        boolean shouldSkip = (year < currentYear - 1)
                                                                        && firestoreServices.hasStatsForMemberAndYear(
                                                                                        member.getId(), year,
                                                                                        "GitLab");
                                                        if (shouldSkip) {
                                                                // Already exists, skip
                                                        } else {
                                                                String glHandle = member.getGitlabAccount()
                                                                                .getUsername();
                                                                String glId = member.getGitlabAccount().getId();

                                                                List<CustomStatsContributionsUserByMonth> glStats;
                                                                if (glId != null && !glId.isEmpty()) {
                                                                        glStats = gitLabServices
                                                                                        .getContributionsByUserId(glId,
                                                                                                        glHandle, year);
                                                                } else {
                                                                        glStats = gitLabServices
                                                                                        .getContributionsForTheCurrentYear(
                                                                                                        glHandle, year);
                                                                }

                                                                if (!glStats.isEmpty()) {
                                                                        List<StatsContribution> statsList = StatsMapper
                                                                                        .mapGitLabStatisticsToStatsContributions(
                                                                                                        member,
                                                                                                        year, glStats);
                                                                        for (StatsContribution stat : statsList) {
                                                                                firestoreServices.saveStats(stat);
                                                                        }
                                                                        glSyncedCount++;
                                                                }
                                                        }
                                                }
                                        }

                                        Jt.success("✅ Successfully synced contributions in " + year + " (GitHub: "
                                                        + ghSyncedCount
                                                        + ", GitLab: " + glSyncedCount + ")")
                                                        .use(contributionsTab);

                                } catch (Exception e) {
                                        Jt.error("❌ Error syncing contributions: " + e.getMessage())
                                                        .use(contributionsTab);
                                        Log.error("Error syncing contributions", e);
                                }
                        }
                }

                Jt.subheader("Monthly Contributions in " + yearValue).use(contributionsTab);

                try {
                        List<Member> activeMembers = firestoreServices.getAllMembers();
                        Set<String> activeMemberIds = activeMembers.stream()
                                        .map(Member::getId)
                                        .collect(Collectors.toSet());

                        List<StatsContribution> allStats = firestoreServices.getStatsForYear(yearValue);

                        Map<Month, Integer> ghContributionsByMonth = allStats.stream()
                                        .filter(s -> "GitHub".equals(s.getSource()))
                                        .filter(s -> activeMemberIds.contains(s.getIdMember()))
                                        .collect(Collectors.groupingBy(
                                                        s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                        Collectors.summingInt(
                                                                        StatsContribution::getNumberOfContributionsOnGitHub)));

                        Map<Month, Integer> glContributionsByMonth = allStats.stream()
                                        .filter(s -> "GitLab".equals(s.getSource()))
                                        .filter(s -> activeMemberIds.contains(s.getIdMember()))
                                        .collect(Collectors.groupingBy(
                                                        s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                        Collectors.summingInt(
                                                                        StatsContribution::getNumberOfContributionsOnGitLab)));

                        List<String> months = new ArrayList<>();
                        List<Integer> ghCounts = new ArrayList<>();
                        List<Integer> glCounts = new ArrayList<>();

                        for (Month m : Month.values()) {
                                months.add(m.toString());
                                ghCounts.add(ghContributionsByMonth.getOrDefault(m, 0));
                                glCounts.add(glContributionsByMonth.getOrDefault(m, 0));
                        }

                        Bar bar = new Bar()
                                        .setTooltip("axis")
                                        .setLegend()
                                        .addXAxis(new CategoryAxis().setData(months.toArray(new String[0])))
                                        .addYAxis(new ValueAxis())
                                        .addSeries(new BarSeries()
                                                        .setName("GitHub")
                                                        .setStack("total")
                                                        .setData(ghCounts.toArray(new Integer[0])))
                                        .addSeries(new BarSeries()
                                                        .setName("GitLab")
                                                        .setStack("total")
                                                        .setData(glCounts.toArray(new Integer[0])));

                        Jt.echarts(bar).use(contributionsTab);

                } catch (Exception e) {
                        Jt.error("Could not load contributions chart: " + e.getMessage()).use(contributionsTab);
                }

                Jt.subheader("Individual Member Stats for " + yearValue).use(contributionsTab);

                String selectedMemberLabel = null;
                Map<String, String> memberOptions = new HashMap<>();

                try {
                        var individualRow = Jt.columns(2).key("individual_controls_row").use(contributionsTab);
                        List<Member> members = firestoreServices.getAllMembers();
                        memberOptions = members.stream()
                                        .collect(Collectors.toMap(
                                                        m -> m.getFirstname() + " " + m.getName() + " ("
                                                                        + (m.getGitHubAccount() != null
                                                                                        ? m.getGitHubAccount()
                                                                                                        .getLogin()
                                                                                        : (m.getGitlabAccount() != null
                                                                                                        ? m.getGitlabAccount()
                                                                                                                        .getUsername()
                                                                                                        : "no account"))
                                                                        + ")",
                                                        m -> m.getId(),
                                                        (existing, replacement) -> existing));

                        selectedMemberLabel = Jt
                                        .selectbox("Select a member",
                                                        new ArrayList<>(memberOptions.keySet().stream().sorted()
                                                                        .collect(Collectors.toList())))
                                        .key("member_selection")
                                        .use(individualRow.col(0));
                } catch (Exception e) {
                        Jt.error("Could not load members: " + e.getMessage()).use(contributionsTab);
                        Log.error("Error loading members", e);
                }

                try {
                        if (selectedMemberLabel != null) {
                                String selectedMemberId = memberOptions.get(selectedMemberLabel);

                                List<StatsContribution> memberStats = firestoreServices
                                                .getContributionsForMember(selectedMemberId);

                                Map<Month, Integer> ghMemberStats = memberStats.stream()
                                                .filter(s -> String.valueOf(yearValue).equals(s.getYear()))
                                                .filter(s -> "GitHub".equals(s.getSource()))
                                                .collect(Collectors.groupingBy(
                                                                s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                                Collectors.summingInt(
                                                                                StatsContribution::getNumberOfContributionsOnGitHub)));

                                Map<Month, Integer> glMemberStats = memberStats.stream()
                                                .filter(s -> String.valueOf(yearValue).equals(s.getYear()))
                                                .filter(s -> "GitLab".equals(s.getSource()))
                                                .collect(Collectors.groupingBy(
                                                                s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                                Collectors.summingInt(
                                                                                StatsContribution::getNumberOfContributionsOnGitLab)));

                                List<String> memberMonths = new ArrayList<>();
                                List<Integer> ghMemberCounts = new ArrayList<>();
                                List<Integer> glMemberCounts = new ArrayList<>();

                                for (Month m : Month.values()) {
                                        memberMonths.add(m.name());
                                        ghMemberCounts.add(ghMemberStats.getOrDefault(m, 0));
                                        glMemberCounts.add(glMemberStats.getOrDefault(m, 0));
                                }

                                Bar memberBar = new Bar()
                                                .setTooltip("axis")
                                                .setLegend()
                                                .addXAxis(new CategoryAxis()
                                                                .setData(memberMonths.toArray(new String[0])))
                                                .addYAxis(new ValueAxis())
                                                .addSeries(new BarSeries()
                                                                .setName("GitHub")
                                                                .setStack("memberTotal")
                                                                .setData(ghMemberCounts.toArray(new Integer[0])))
                                                .addSeries(new BarSeries()
                                                                .setName("GitLab")
                                                                .setStack("memberTotal")
                                                                .setData(glMemberCounts.toArray(new Integer[0])));

                                Jt.echarts(memberBar).use(contributionsTab);
                        }

                } catch (Exception e) {
                        Jt.error("Error loading member stats: " + e.getMessage()).use(contributionsTab);
                        Log.error("Error loading member stats", e);
                }

        }
}