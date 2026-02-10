package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.mapper.StatsMapper;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

import java.time.Year;
import java.util.List;

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
