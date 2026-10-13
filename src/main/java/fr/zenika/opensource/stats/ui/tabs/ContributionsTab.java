package fr.zenika.opensource.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.ExternalContribution;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.mapper.StatsMapper;
import fr.zenika.opensource.stats.services.ExternalContributionService;
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
import java.util.Objects;
import java.util.Optional;
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

        @Inject
        ExternalContributionService externalContributionService;

        @ConfigProperty(name = "oss.stats.sync.buttons.enabled", defaultValue = "false")
        boolean syncButtonsEnabled;

        private String contributionSearchTerm = "";

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
                                                        // For past years (older than previous year), skip if stats
                                                        // already exist
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
                                                        // For past years (older than previous year), skip if stats
                                                        // already exist
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

                List<Member> activeMembers = List.of();
                List<StatsContribution> allStatsForYear = List.of();

                try {
                        activeMembers = firestoreServices.getAllMembers();
                        Set<String> activeMemberIds = activeMembers.stream()
                                        .map(Member::getId)
                                        .collect(Collectors.toSet());

                        allStatsForYear = firestoreServices.getStatsForYear(yearValue);

                        Map<Month, Integer> ghContributionsByMonth = allStatsForYear.stream()
                                        .filter(s -> "GitHub".equals(s.getSource()))
                                        .filter(s -> activeMemberIds.contains(s.getIdMember()))
                                        .collect(Collectors.groupingBy(
                                                        s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                        Collectors.summingInt(
                                                                        StatsContribution::getNumberOfContributionsOnGitHub)));

                        Map<Month, Integer> glContributionsByMonth = allStatsForYear.stream()
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

                renderExternalContributionsSection(contributionsTab, yearValue);

                Jt.subheader("Individual Member Stats for " + yearValue).use(contributionsTab);

                final String NO_SELECTION_LABEL = "-- Select a member --";
                String selectedMemberLabel = null;
                String selectedMemberId = null;
                Map<String, String> memberOptions = new HashMap<>();

                try {
                        var individualRow = Jt.columns(2).key("individual_controls_row").use(contributionsTab);
                        List<Member> members = activeMembers.isEmpty() ? firestoreServices.getAllMembers()
                                        : activeMembers;
                        memberOptions = members.stream()
                                        .filter(m -> m.getId() != null)
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

                        List<String> memberLabels = new ArrayList<>();
                        memberLabels.add(NO_SELECTION_LABEL);
                        memberLabels.addAll(memberOptions.keySet().stream().sorted().toList());

                        Object previousSelection = Jt.componentsState().get("member_selection");
                        int previousIndex = previousSelection != null
                                        ? memberLabels.indexOf(previousSelection.toString())
                                        : -1;

                        selectedMemberLabel = Jt
                                        .selectbox("Select a member", new ArrayList<>(memberLabels))
                                        .index(previousIndex >= 0 ? previousIndex : 0)
                                        .key("member_selection")
                                        .use(individualRow.col(0));

                        if (selectedMemberLabel != null && !NO_SELECTION_LABEL.equals(selectedMemberLabel)) {
                                selectedMemberId = memberOptions.get(selectedMemberLabel);
                        }
                } catch (Exception e) {
                        Jt.error("Could not load members: " + e.getMessage()).use(contributionsTab);
                        Log.error("Error loading members", e);
                }

                // If no member is selected, do not render member stats, project summary, or
                // PR/MR tables
                if (selectedMemberId == null) {
                        return;
                }

                List<StatsContribution> memberYearStats = List.of();
                try {
                        final String memberId = selectedMemberId;
                        memberYearStats = allStatsForYear.stream()
                                        .filter(s -> memberId.equals(s.getIdMember()))
                                        .collect(Collectors.toList());

                        if (memberYearStats.isEmpty()) {
                                List<StatsContribution> directMemberStats = firestoreServices
                                                .getContributionsForMember(memberId);
                                memberYearStats = directMemberStats.stream()
                                                .filter(s -> String.valueOf(yearValue).equals(s.getYear()))
                                                .collect(Collectors.toList());
                        }

                        Map<Month, Integer> ghMemberStats = memberYearStats.stream()
                                        .filter(s -> "GitHub".equals(s.getSource()))
                                        .collect(Collectors.groupingBy(
                                                        s -> Month.valueOf(s.getMonth().toUpperCase()),
                                                        Collectors.summingInt(
                                                                        StatsContribution::getNumberOfContributionsOnGitHub)));

                        Map<Month, Integer> glMemberStats = memberYearStats.stream()
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

                } catch (Exception e) {
                        Jt.error("Error loading member stats: " + e.getMessage()).use(contributionsTab);
                        Log.error("Error loading member stats", e);
                }

                renderMemberContributedProjectsAndPRs(contributionsTab, yearValue, selectedMemberLabel,
                                memberYearStats);
        }

        private void renderMemberContributedProjectsAndPRs(JtContainer contributionsTab, int yearValue,
                        String selectedMemberLabel, List<StatsContribution> memberYearStats) {
                try {
                        record MemberDetailItem(String projectName, String projectFullName, String projectUrl,
                                        String prTitle, String prUrl, String prState, String createdAt, String source,
                                        String year, String month) {
                        }

                        List<MemberDetailItem> allDetails = memberYearStats.stream()
                                        .flatMap(stat -> Optional.ofNullable(stat.getContributionsDetails())
                                                        .orElse(List.of())
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .map(detail -> new MemberDetailItem(
                                                                        Optional.ofNullable(detail.getProjectName())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getProjectFullName())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getProjectUrl())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getPrTitle())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getPrUrl())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getPrState())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(detail.getCreatedAt())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(stat.getSource())
                                                                                        .orElse(""),
                                                                        Optional.ofNullable(stat.getYear())
                                                                                        .orElse(String.valueOf(yearValue)),
                                                                        Optional.ofNullable(stat.getMonth())
                                                                                        .orElse(""))))
                                        .collect(Collectors.toList());

                        if (allDetails.isEmpty()) {
                                Jt.text("No detailed PR/MR data available for " + selectedMemberLabel + " in "
                                                + yearValue + ".")
                                                .use(contributionsTab);
                                return;
                        }

                        allDetails.sort((d1, d2) -> compareContributionDetailsByDateDesc(
                                        d1.createdAt(), d1.year(), d1.month(), d1.prTitle(),
                                        d2.createdAt(), d2.year(), d2.month(), d2.prTitle()));

                        // 1. Group by project to show the summary of contributed projects with PR/MR
                        // count
                        Map<String, List<MemberDetailItem>> byProject = allDetails.stream()
                                        .collect(Collectors.groupingBy(d -> {
                                                if (d.projectFullName() != null && !d.projectFullName().isBlank()) {
                                                        return d.projectFullName();
                                                }
                                                if (d.projectName() != null && !d.projectName().isBlank()) {
                                                        return d.projectName();
                                                }
                                                return "Unknown Project";
                                        }));

                        record MemberProjectSummary(String Project, String URL, String Source, Long PR_MR_Count) {
                        }

                        List<MemberProjectSummary> projectSummaries = byProject.entrySet().stream()
                                        .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                                        .map(entry -> {
                                                String proj = entry.getKey();
                                                List<MemberDetailItem> items = entry.getValue();
                                                String url = items.stream()
                                                                .map(MemberDetailItem::projectUrl)
                                                                .filter(u -> !u.isBlank())
                                                                .findFirst()
                                                                .orElse("");
                                                String source = items.stream()
                                                                .map(MemberDetailItem::source)
                                                                .filter(s -> !s.isBlank())
                                                                .findFirst()
                                                                .orElse("");
                                                return new MemberProjectSummary(proj, url, source, (long) items.size());
                                        })
                                        .collect(Collectors.toList());

                        Jt.subheader("📁 Contributed Projects for " + selectedMemberLabel + " ("
                                        + projectSummaries.size()
                                        + " projects, " + allDetails.size() + " PR/MRs in " + yearValue + ")")
                                        .use(contributionsTab);

                        if (projectSummaries.size() >= 2) {
                                List<String> projNames = projectSummaries.stream().map(MemberProjectSummary::Project)
                                                .toList();
                                List<Long> projCounts = projectSummaries.stream().map(MemberProjectSummary::PR_MR_Count)
                                                .toList();

                                Bar projBar = new Bar()
                                                .setTooltip("axis")
                                                .addXAxis(new CategoryAxis().setData(projNames.toArray(new String[0])))
                                                .addYAxis(new ValueAxis())
                                                .addSeries(new BarSeries()
                                                                .setName("PR / MR Count")
                                                                .setData(projCounts.toArray(new Number[0])));
                                Jt.echarts(projBar).use(contributionsTab);
                        }

                        Jt.table(projectSummaries).key("member_contributed_projects_summary_table")
                                        .use(contributionsTab);

                        // 2. Detailed list of Pull Requests & Merge Requests
                        Jt.subheader("🔍 Pull Requests & Merge Requests Details for " + selectedMemberLabel)
                                        .use(contributionsTab);

                        var searchRow = Jt.columns(2).key("member_prs_search_row").use(contributionsTab);
                        contributionSearchTerm = Jt.textInput("Search by Project, PR Title, State, or Date")
                                        .key("member_prs_search_input")
                                        .value(contributionSearchTerm)
                                        .use(searchRow.col(0));

                        String lowerTerm = Optional.ofNullable(contributionSearchTerm)
                                        .map(String::trim)
                                        .map(String::toLowerCase)
                                        .orElse("");

                        record MemberPRDisplay(String Date, String Project, String Project_URL, String PR_Title,
                                        String PR_URL, String State, String Source) {
                        }

                        List<MemberPRDisplay> filteredPRs = allDetails.stream()
                                        .filter(d -> lowerTerm.isEmpty()
                                                        || d.projectName().toLowerCase().contains(lowerTerm)
                                                        || d.projectFullName().toLowerCase().contains(lowerTerm)
                                                        || d.prTitle().toLowerCase().contains(lowerTerm)
                                                        || d.prState().toLowerCase().contains(lowerTerm)
                                                        || d.source().toLowerCase().contains(lowerTerm)
                                                        || d.createdAt().toLowerCase().contains(lowerTerm)
                                                        || d.month().toLowerCase().contains(lowerTerm)
                                                        || formatContributionDate(d.createdAt(), d.year(), d.month()).toLowerCase().contains(lowerTerm))
                                        .map(d -> new MemberPRDisplay(
                                                        formatContributionDate(d.createdAt(), d.year(), d.month()),
                                                        !d.projectFullName().isBlank() ? d.projectFullName()
                                                                        : d.projectName(),
                                                        d.projectUrl(),
                                                        d.prTitle(),
                                                        d.prUrl(),
                                                        d.prState(),
                                                        d.source()))
                                        .collect(Collectors.toList());

                        Jt.table(filteredPRs).key("member_prs_details_table").use(contributionsTab);

                } catch (Exception e) {
                        Jt.warning("Could not load contributed projects and PRs: " + e.getMessage())
                                        .use(contributionsTab);
                        Log.error("Error loading contributed projects and PRs", e);
                }
        }

        private void renderExternalContributionsSection(JtContainer contributionsTab, int yearValue) {
                Jt.subheader("📢 Latest External Contributions (for Communication)").use(contributionsTab);

                try {
                        var extControls = Jt.columns(4).key("ext_contrib_controls_row").use(contributionsTab);

                        List<String> periodOptions = List.of("Year " + yearValue, "All Time");
                        Object prevPeriod = Jt.componentsState().get("ext_contrib_period");
                        int periodIdx = prevPeriod != null ? periodOptions.indexOf(prevPeriod.toString()) : 0;
                        String selectedPeriod = Jt.selectbox("Period", new ArrayList<>(periodOptions))
                                        .index(periodIdx >= 0 ? periodIdx : 0)
                                        .key("ext_contrib_period")
                                        .use(extControls.col(0));

                        List<String> stateOptions = List.of("ALL", "MERGED", "OPEN", "CLOSED");
                        Object prevState = Jt.componentsState().get("ext_contrib_state");
                        int stateIdx = prevState != null ? stateOptions.indexOf(prevState.toString()) : 0;
                        String selectedState = Jt.selectbox("State", new ArrayList<>(stateOptions))
                                        .index(stateIdx >= 0 ? stateIdx : 0)
                                        .key("ext_contrib_state")
                                        .use(extControls.col(1));

                        List<String> limitOptions = List.of("25 latest", "50 latest", "100 latest", "All");
                        Object prevLimit = Jt.componentsState().get("ext_contrib_limit");
                        int limitIdx = prevLimit != null ? limitOptions.indexOf(prevLimit.toString()) : 0;
                        String selectedLimit = Jt.selectbox("Limit", new ArrayList<>(limitOptions))
                                        .index(limitIdx >= 0 ? limitIdx : 0)
                                        .key("ext_contrib_limit")
                                        .use(extControls.col(2));

                        Object prevSearch = Jt.componentsState().get("ext_contrib_search");
                        String searchVal = prevSearch != null ? prevSearch.toString() : "";
                        String searchTerm = Jt.textInput("Search")
                                        .key("ext_contrib_search")
                                        .value(searchVal)
                                        .use(extControls.col(3));

                        Integer queryYear = "All Time".equals(selectedPeriod) ? null : yearValue;
                        String queryState = "ALL".equals(selectedState) ? null : selectedState;

                        List<ExternalContribution> externalList = externalContributionService.getExternalContributions(
                                        queryYear, queryState, null, null);

                        String term = Optional.ofNullable(searchTerm).map(String::trim).map(String::toLowerCase).orElse("");
                        List<ExternalContribution> filteredList = externalList.stream()
                                        .filter(c -> term.isEmpty()
                                                        || Optional.ofNullable(c.getMemberName()).orElse("").toLowerCase().contains(term)
                                                        || Optional.ofNullable(c.getMemberHandle()).orElse("").toLowerCase().contains(term)
                                                        || Optional.ofNullable(c.getProjectFullName()).orElse("").toLowerCase().contains(term)
                                                        || Optional.ofNullable(c.getPrTitle()).orElse("").toLowerCase().contains(term)
                                                        || Optional.ofNullable(c.getPrState()).orElse("").toLowerCase().contains(term)
                                                        || Optional.ofNullable(c.getSource()).orElse("").toLowerCase().contains(term))
                                        .collect(Collectors.toList());

                        Integer queryLimit = switch (Optional.ofNullable(selectedLimit).orElse("25 latest")) {
                                case "25 latest" -> 25;
                                case "50 latest" -> 50;
                                case "100 latest" -> 100;
                                default -> null;
                        };

                        int displayLimit = queryLimit != null ? Math.min(queryLimit, filteredList.size()) : filteredList.size();
                        List<ExternalContribution> displayed = filteredList.stream()
                                        .limit(displayLimit)
                                        .collect(Collectors.toList());

                        if (displayed.isEmpty()) {
                                Jt.text("No external contributions found matching criteria.").use(contributionsTab);
                        } else {
                                Jt.markdown("**" + filteredList.size() + " external contributions found** *(showing "
                                                + displayed.size() + ", sorted newest first)*").use(contributionsTab);

                                record ExternalPRDisplay(String Date, String Member, String Project, String Title,
                                                String State, String Source, String PR_URL) {
                                }

                                List<ExternalPRDisplay> tableData = displayed.stream()
                                                .map(c -> new ExternalPRDisplay(
                                                                c.getFormattedDate(),
                                                                c.getMemberName(),
                                                                c.getProjectFullName(),
                                                                c.getPrTitle(),
                                                                c.getPrState(),
                                                                c.getSource(),
                                                                c.getPrUrl()))
                                                .collect(Collectors.toList());

                                Jt.table(tableData).key("ext_contrib_table").use(contributionsTab);
                        }
                } catch (Exception e) {
                        Jt.warning("Could not load external contributions: " + e.getMessage()).use(contributionsTab);
                        Log.error("Error loading external contributions", e);
                }
        }

        static int parseMonthValue(String monthStr) {
                if (monthStr == null || monthStr.isBlank()) {
                        return 0;
                }
                try {
                        return Month.valueOf(monthStr.trim().toUpperCase()).getValue();
                } catch (IllegalArgumentException e) {
                        try {
                                return Integer.parseInt(monthStr.trim());
                        } catch (NumberFormatException nfe) {
                                return 0;
                        }
                }
        }

        static String toSortKey(String createdAt, String year, String month) {
                if (createdAt != null && !createdAt.isBlank()) {
                        return createdAt.trim();
                }
                int m = parseMonthValue(month);
                String y = Optional.ofNullable(year).filter(s -> !s.isBlank()).orElse("0000").trim();
                return String.format("%s-%02d-00T00:00:00Z", y, m);
        }

        static String formatContributionDate(String createdAt, String year, String month) {
                if (createdAt != null && !createdAt.isBlank()) {
                        if (createdAt.contains("T")) {
                                return createdAt.split("T")[0];
                        }
                        return createdAt.trim();
                }
                if (year != null && !year.isBlank() && month != null && !month.isBlank()) {
                        return year.trim() + " (" + month.trim() + ")";
                }
                if (month != null && !month.isBlank()) {
                        return month.trim();
                }
                return Optional.ofNullable(year).map(String::trim).orElse("");
        }

        static int compareContributionDetailsByDateDesc(String c1, String y1, String m1, String title1,
                        String c2, String y2, String m2, String title2) {
                String key1 = toSortKey(c1, y1, m1);
                String key2 = toSortKey(c2, y2, m2);
                int cmp = key2.compareTo(key1);
                if (cmp != 0) {
                        return cmp;
                }
                return Optional.ofNullable(title1).orElse("")
                                .compareTo(Optional.ofNullable(title2).orElse(""));
        }
}