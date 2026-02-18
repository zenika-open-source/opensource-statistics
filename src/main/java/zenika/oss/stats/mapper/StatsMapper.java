package zenika.oss.stats.mapper;

import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.gcp.StatsContribution;

import java.util.ArrayList;
import java.util.List;

public class StatsMapper {

    public static List<StatsContribution> mapGitHubStatisticsToStatsContributions(ZenikaMember member, int year,
            List<CustomStatsContributionsUserByMonth> githubStats) {
        List<StatsContribution> statsContributions = new ArrayList<>();
        for (CustomStatsContributionsUserByMonth stat : githubStats) {
            StatsContribution sc = new StatsContribution();
            sc.setYear(String.valueOf(year));
            sc.setIdZenikaMember(member.getId());
            sc.setSource("GitHub");
            if (member.getGitHubAccount() != null) {
                sc.setGithubHandle(member.getGitHubAccount().getLogin());
            }
            sc.setMonth(stat.getMonthLabel());
            sc.setNumberOfContributionsOnGitHub(stat.getContributions());
            statsContributions.add(sc);
        }
        return statsContributions;
    }

    public static List<StatsContribution> mapGitLabStatisticsToStatsContributions(ZenikaMember member, int year,
            List<CustomStatsContributionsUserByMonth> gitlabStats) {
        List<StatsContribution> statsContributions = new ArrayList<>();
        for (CustomStatsContributionsUserByMonth stat : gitlabStats) {
            StatsContribution sc = new StatsContribution();
            sc.setYear(String.valueOf(year));
            sc.setIdZenikaMember(member.getId());
            sc.setSource("GitLab");
            if (member.getGitlabAccount() != null) {
                sc.setGitlabHandle(member.getGitlabAccount().getUsername());
            }
            sc.setMonth(stat.getMonthLabel());
            sc.setNumberOfContributionsOnGitLab(stat.getContributions());
            statsContributions.add(sc);
        }
        return statsContributions;
    }

    public static List<StatsContribution> mapMemberStatisticsToStatsContributions(ZenikaMember member, int year,
            List<CustomStatsContributionsUserByMonth> githubStats,
            List<CustomStatsContributionsUserByMonth> gitlabStats) {
        List<StatsContribution> stats = new ArrayList<>();
        stats.addAll(mapGitHubStatisticsToStatsContributions(member, year, githubStats));
        stats.addAll(mapGitLabStatisticsToStatsContributions(member, year, gitlabStats));
        return stats;
    }

    public static List<StatsContribution> mapGithubStatisticsToStatsContribution(String githubMember, int year,
            List<CustomStatsContributionsUserByMonth> stats) {
        List<StatsContribution> statsContributions = new ArrayList<>();
        stats.forEach(stat -> {
            StatsContribution sc = new StatsContribution();
            sc.setYear(String.valueOf(year));
            sc.setMonth(stat.getMonthLabel());
            sc.setNumberOfContributionsOnGitHub(stat.getContributions());
            sc.setGithubHandle(githubMember);
            sc.setSource("GitHub");
            statsContributions.add(sc);
        });
        return statsContributions;
    }

    public static StatsContribution mapGithubStatisticToStatsContribution(String githubMember, int year,
            CustomStatsContributionsUserByMonth customStatsContributionsUserByMonth) {
        StatsContribution statsContribution = new StatsContribution();
        statsContribution.setYear(String.valueOf(year));
        statsContribution.setMonth(customStatsContributionsUserByMonth.getMonthLabel());
        statsContribution.setNumberOfContributionsOnGitHub(customStatsContributionsUserByMonth.getContributions());
        statsContribution.setGithubHandle(githubMember);
        statsContribution.setSource("GitHub");
        return statsContribution;
    }
}
