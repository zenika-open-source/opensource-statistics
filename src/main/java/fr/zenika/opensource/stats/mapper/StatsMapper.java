package fr.zenika.opensource.stats.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;

public class StatsMapper {

    public static List<StatsContribution> mapGitHubStatisticsToStatsContributions(Member member, int year,
            List<CustomStatsContributionsUserByMonth> githubStats) {
        List<StatsContribution> statsContributions = new ArrayList<>();
        for (CustomStatsContributionsUserByMonth stat : githubStats) {
            StatsContribution sc = new StatsContribution();
            sc.setYear(String.valueOf(year));
            sc.setIdMember(member.getId());
            sc.setSource("GitHub");
            Optional.ofNullable(member.getGitHubAccount())
                    .ifPresent(account -> sc.setGithubHandle(account.getLogin()));
            sc.setMonth(stat.getMonthLabel());
            sc.setNumberOfContributionsOnGitHub(stat.getContributions());
            sc.setContributionsDetails(Optional.ofNullable(stat.getContributionsDetails()).orElse(List.of()));
            statsContributions.add(sc);
        }
        return statsContributions;
    }

    public static List<StatsContribution> mapGitLabStatisticsToStatsContributions(Member member, int year,
            List<CustomStatsContributionsUserByMonth> gitlabStats) {
        List<StatsContribution> statsContributions = new ArrayList<>();
        for (CustomStatsContributionsUserByMonth stat : gitlabStats) {
            StatsContribution sc = new StatsContribution();
            sc.setYear(String.valueOf(year));
            sc.setIdMember(member.getId());
            sc.setSource("GitLab");
            Optional.ofNullable(member.getGitlabAccount())
                    .ifPresent(account -> sc.setGitlabHandle(account.getUsername()));
            sc.setMonth(stat.getMonthLabel());
            sc.setNumberOfContributionsOnGitLab(stat.getContributions());
            sc.setContributionsDetails(Optional.ofNullable(stat.getContributionsDetails()).orElse(List.of()));
            statsContributions.add(sc);
        }
        return statsContributions;
    }

    public static List<StatsContribution> mapMemberStatisticsToStatsContributions(Member member, int year,
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
            sc.setContributionsDetails(Optional.ofNullable(stat.getContributionsDetails()).orElse(List.of()));
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
        statsContribution.setContributionsDetails(
                Optional.ofNullable(customStatsContributionsUserByMonth.getContributionsDetails()).orElse(List.of()));
        return statsContribution;
    }
}

