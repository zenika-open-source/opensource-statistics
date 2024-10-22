package zenika.oss.stats.services;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.CustomStatsUser;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.config.GitHubClient;

import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitHubServicesTest {

    @InjectMocks
    private GitHubServices gitHubServices;

    @Mock
    private GitHubClient gitHubClient;
    
    @Mock
    DynamicGraphQLClient dynamicGraphQLClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetContributionsForTheCurrentYearAndAllTheOrganizationMembers() {
        // Arrange
        String organizationName = "testOrg";
        GitHubMember user1 = new GitHubMember();
        user1.login = "user1";
        GitHubMember user2 = new GitHubMember();
        user2.login = "user2";

        List<GitHubMember> members = Arrays.asList(user1, user2);
                
        List<CustomStatsContributionsUserByMonth> user1Contributions = Arrays.asList(
                new CustomStatsContributionsUserByMonth(1, "January", 10),
                new CustomStatsContributionsUserByMonth(2, "February", 15)
        );

        List<CustomStatsContributionsUserByMonth> user2Contributions = Arrays.asList(
                new CustomStatsContributionsUserByMonth(1, "January", 5),
                new CustomStatsContributionsUserByMonth(2, "February", 8)
        );

        when(gitHubServices.getOrganizationMembers(organizationName)).thenReturn(members);
        when(gitHubServices.getContributionsForTheCurrentYear("user1", Year.now().getValue())).thenReturn(user1Contributions);
        when(gitHubServices.getContributionsForTheCurrentYear("user2", Year.now().getValue())).thenReturn(user2Contributions);

        // Act
        List<CustomStatsUser> result = gitHubServices.getContributionsForTheCurrentYearAndAllTheOrganizationMembers(organizationName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        CustomStatsUser user1Stats = result.get(0);
        assertEquals("user1", user1Stats.getLogin());
        assertEquals(user1Contributions, user1Stats.getContributionsUserByMonths());

        CustomStatsUser user2Stats = result.get(1);
        assertEquals("user2", user2Stats.getLogin());
        assertEquals(user2Contributions, user2Stats.getContributionsUserByMonths());

        verify(gitHubServices, times(1)).getOrganizationMembers(organizationName);
        verify(gitHubServices, times(1)).getContributionsForTheCurrentYear("user1", Year.now().getValue());
        verify(gitHubServices, times(1)).getContributionsForTheCurrentYear("user2", Year.now().getValue());
    }

    @Test
    void testGetContributionsForTheCurrentYearAndAllTheOrganizationMembersWithEmptyOrganization() {
        // Arrange
        String organizationName = "emptyOrg";
        when(gitHubServices.getOrganizationMembers(organizationName)).thenReturn(Collections.emptyList());

        // Act
        List<CustomStatsUser> result = gitHubServices.getContributionsForTheCurrentYearAndAllTheOrganizationMembers(organizationName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gitHubServices, times(1)).getOrganizationMembers(organizationName);
        verify(gitHubServices, never()).getContributionsForTheCurrentYear(anyString(), anyInt());
    }
}
