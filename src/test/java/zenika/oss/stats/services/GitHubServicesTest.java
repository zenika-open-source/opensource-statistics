package zenika.oss.stats.services;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubOrganization;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.config.GitHubClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitHubServicesTest {

    private static final Logger log = LoggerFactory.getLogger(GitHubServicesTest.class);

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
    void test_getOrganizationInformation() {
        var organizationName = "testOrg";
        var organizationDescription = "description";

        GitHubOrganization expectedOrganization = new GitHubOrganization();
        expectedOrganization.setName(organizationName);
        expectedOrganization.setDescription(organizationDescription);

        when(gitHubClient.getOrgnizationByName(organizationName)).thenReturn(expectedOrganization);

        GitHubOrganization result = gitHubServices.getOrganizationInformation(organizationName);

        assertNotNull(result);
        assertEquals(organizationName, result.getName());
        assertEquals(organizationDescription, result.getDescription());

        verify(gitHubClient, times(1)).getOrgnizationByName(organizationName);
    }


    @Test
    void test_getOrganizationInformation_Null() {
        when(gitHubClient.getOrgnizationByName(null)).thenReturn(null);

        GitHubOrganization result = gitHubServices.getOrganizationInformation(null);

        assertNull(result);

        verify(gitHubClient, times(1)).getOrgnizationByName(null);
    }

    @Test
    void test_getUserInformation() {
        var idUser = "idUser";
        GitHubMember githubMember = new GitHubMember();
        githubMember.setId(idUser);
        githubMember.setLogin("login");
        githubMember.setType("type");

        when(gitHubClient.getUserInformation(idUser)).thenReturn(githubMember);

        final GitHubMember result = gitHubServices.getUserInformation(idUser);

        assertNotNull(result);

        verify(gitHubClient, times(1)).getUserInformation(idUser);
    }

    @Test
    void test_getPersonalProjectForAnUser() {
        String login = "testUser";
        List<GitHubProject> expectedProjects = Arrays.asList(
                new GitHubProject(),
                new GitHubProject()
        );

        when(gitHubClient.getReposForAnUser(login, 100, 1)).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getPersonalProjectForAnUser(login);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);

        verify(gitHubClient, times(1)).getReposForAnUser(login, 100, 1);
    }

    @Test
    void test_getForkedProjectForAnUser() {
        String login = "testUser";
        GitHubProject fork = new GitHubProject();
        fork.setFork(true);
        List<GitHubProject> expectedProjects = Arrays.asList(fork);

        when(gitHubClient.getReposForAnUser(login, 100, 1)).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getForkedProjectForAnUser(login);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gitHubClient, times(1)).getReposForAnUser(login, 100, 1);
    }

    @Test
    void test_getOrganizationMembers_MultiPage() {
        String org = "testOrg";
        List<GitHubMember> page1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            page1.add(new GitHubMember());
        }
        List<GitHubMember> page2 = Arrays.asList(new GitHubMember());

        when(gitHubClient.getOrganizationMembers(org, 100, 1)).thenReturn(page1);
        when(gitHubClient.getOrganizationMembers(org, 100, 2)).thenReturn(page2);

        List<GitHubMember> result = gitHubServices.getOrganizationMembers(org);

        assertNotNull(result);
        assertEquals(101, result.size());
        verify(gitHubClient, times(1)).getOrganizationMembers(org, 100, 1);
        verify(gitHubClient, times(1)).getOrganizationMembers(org, 100, 2);
    }

    @Test
    void test_getOrganizationProjects() {
        String org = "testOrg";
        GitHubProject project = new GitHubProject();
        project.setFork(false);
        project.setArchived(false);
        List<GitHubProject> expectedProjects = Arrays.asList(project);

        when(gitHubClient.getOrganizationProjects(org, 100, 1)).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getOrganizationProjects(org);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gitHubClient, times(1)).getOrganizationProjects(org, 100, 1);
    }
    
    
    /*
    @Test
    void testGetContributionsForTheCurrentYearAndAllTheOrganizationMembers() throws ExecutionException, InterruptedException {
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
        
        Response response = Mockito.mock(Response.class);

        UserStatsNumberContributions stats = new UserStatsNumberContributions();
        stats.setContributionsCollection(new ContributionsCollectionNumber());
        stats.getContributionsCollection().setPullRequestContributions(new PullRequestContributions());
        stats.getContributionsCollection().getPullRequestContributions().setTotalCount(10);
        
        when(response.getObject(UserStatsNumberContributions.class, "user")).thenReturn(stats);
        when(dynamicGraphQLClient.executeSync(anyString(), anyMap())).thenReturn(response);
        
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
    
    */
}
