package zenika.oss.stats.mapper;

import org.junit.jupiter.api.Test;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.gcp.StatsContribution;

import static org.junit.jupiter.api.Assertions.*;

class StatsMapperTest {

    @Test
    void shouldMapGithubStatisticToStatsContribution() {
        // Given
        String githubMember = "test-dev";
        int year = 2023;
        CustomStatsContributionsUserByMonth monthlyStats = new CustomStatsContributionsUserByMonth(10, "October", 150);

        // When
        StatsContribution result = StatsMapper.mapGithubStatisticToStatsContribution(githubMember, year, monthlyStats);

        // Then
        assertNotNull(result);
        assertEquals(result.getYear(),"2023");
        assertEquals(result.getMonth(), "October");
        assertEquals(result.getNumberOfContributionsOnGitHub(),150);
        assertEquals(result.getGithubHandle(), "test-dev");
        assertNull(result.getGitlabHandle());
        assertNull(result.getIdZenikaMember());
        assertEquals(result.getNumberOfContributionsOnGitLab(), 0);
    }
}
