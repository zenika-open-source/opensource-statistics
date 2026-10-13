package fr.zenika.opensource.stats.mapper;

import org.junit.jupiter.api.Test;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsMapperTest {

    @Test
    void shouldMapGithubStatisticToStatsContribution() {
        // Given
        String githubMember = "test-dev";
        int year = 2023;
        ContributionDetail detail = new ContributionDetail(
                "quarkus",
                "quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus",
                "Fix GraphQL client config",
                "https://github.com/quarkusio/quarkus/pull/1234",
                "MERGED",
                "2023-10-12T10:00:00Z");
        CustomStatsContributionsUserByMonth monthlyStats = new CustomStatsContributionsUserByMonth(
                10, "October", 150, List.of(detail));

        // When
        StatsContribution result = StatsMapper.mapGithubStatisticToStatsContribution(githubMember, year, monthlyStats);

        // Then
        assertNotNull(result);
        assertEquals("2023", result.getYear());
        assertEquals("October", result.getMonth());
        assertEquals(150, result.getNumberOfContributionsOnGitHub());
        assertEquals("test-dev", result.getGithubHandle());
        assertNull(result.getGitlabHandle());
        assertNull(result.getIdMember());
        assertEquals(0, result.getNumberOfContributionsOnGitLab());
        assertNotNull(result.getContributionsDetails());
        assertEquals(1, result.getContributionsDetails().size());
        assertEquals("quarkusio/quarkus", result.getContributionsDetails().get(0).getProjectFullName());
        assertEquals("https://github.com/quarkusio/quarkus/pull/1234", result.getContributionsDetails().get(0).getPrUrl());
    }

    @Test
    void shouldMapGitHubAndGitLabStatisticsWithContributionDetails() {
        // Given
        Member member = new Member();
        member.setId("member-1");
        GitHubMember gh = new GitHubMember();
        gh.setLogin("gh-user");
        member.setGitHubAccount(gh);
        GitLabMember gl = new GitLabMember();
        gl.setUsername("gl-user");
        member.setGitlabAccount(gl);

        ContributionDetail ghDetail = new ContributionDetail(
                "repo-gh", "org/repo-gh", "https://github.com/org/repo-gh",
                "Add feature", "https://github.com/org/repo-gh/pull/1", "MERGED", "2026-03-01T00:00:00Z");
        ContributionDetail glDetail = new ContributionDetail(
                "repo-gl", "group/repo-gl", "https://gitlab.com/group/repo-gl",
                "Fix bug", "https://gitlab.com/group/repo-gl/-/merge_requests/2", "merged", "2026-03-02T00:00:00Z");

        List<CustomStatsContributionsUserByMonth> ghStats = List.of(
                new CustomStatsContributionsUserByMonth(3, "March", 1, List.of(ghDetail)));
        List<CustomStatsContributionsUserByMonth> glStats = List.of(
                new CustomStatsContributionsUserByMonth(3, "March", 1, List.of(glDetail)));

        // When
        List<StatsContribution> results = StatsMapper.mapMemberStatisticsToStatsContributions(
                member, 2026, ghStats, glStats);

        // Then
        assertEquals(2, results.size());
        assertEquals("org/repo-gh", results.get(0).getContributionsDetails().get(0).getProjectFullName());
        assertEquals("group/repo-gl", results.get(1).getContributionsDetails().get(0).getProjectFullName());
    }
}

