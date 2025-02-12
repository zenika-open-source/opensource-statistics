package zenika.oss.stats.mapper;

import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.gcp.StatsContribution;

import java.util.ArrayList;
import java.util.List;

public class StatsMapper {
    public static List<StatsContribution> mapGithubStatisticsToStatsContribution(String githubMember, int year, List<CustomStatsContributionsUserByMonth> stats) {
        List<StatsContribution> statsContributions = new ArrayList<>();

        stats.forEach(stat -> statsContributions.add(StatsMapper.mapGithubStatisticToStatsContribution(githubMember, year, stat)));

        return statsContributions;
    }

    private static StatsContribution mapGithubStatisticToStatsContribution(String githubMember, int year, CustomStatsContributionsUserByMonth customStatsContributionsUserByMonth) {
        StatsContribution statsContribution = new StatsContribution();

        statsContribution.setYear(String.valueOf(year));
        statsContribution.setMonth(customStatsContributionsUserByMonth.getMonthLabel());
        statsContribution.setNumberOfContributionsOnGitHub(customStatsContributionsUserByMonth.getContributions());
        statsContribution.setGithubHandle(githubMember);
        statsContribution.setGitlabHandle(null);
        statsContribution.setIdZenikaMember(null);

        return statsContribution;
    }
}
