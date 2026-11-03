package fr.zenika.opensource.stats.services;

import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;
import fr.zenika.opensource.stats.config.GitLabClient;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitLabServicesTest {

    @Mock
    GitLabClient gitLabClient;

    @InjectMocks
    GitLabServices gitLabServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitLabServices.gitlabToken = Optional.of("test-token");
    }

    @Test
    void getPersonalProjectsForAnUser_ShouldOnlyReturnPublicNonForkProjects() {
        String username = "jeanphi-baconnais";
        GitLabMember user = new GitLabMember();
        user.setId("3589694");
        when(gitLabClient.getUserInformations(eq("Bearer test-token"), eq(username)))
                .thenReturn(List.of(user));

        GitLabProject publicProj = new GitLabProject();
        publicProj.setName("open-project");
        publicProj.setVisibility("public");

        GitLabProject privateProj = new GitLabProject();
        privateProj.setName("arbre-gen");
        privateProj.setVisibility("private");

        GitLabProject forkProj = new GitLabProject();
        forkProj.setName("forked-project");
        forkProj.setVisibility("public");
        forkProj.setForkedFromProject(new Object());

        when(gitLabClient.getProjectsForAnUser(eq("Bearer test-token"), eq("3589694"), eq("public")))
                .thenReturn(List.of(publicProj, privateProj, forkProj));

        List<GitLabProject> result = gitLabServices.getPersonalProjectsForAnUser(username);

        assertEquals(1, result.size());
        assertEquals("open-project", result.get(0).getName());
        verify(gitLabClient).getProjectsForAnUser("Bearer test-token", "3589694", "public");
    }

    @Test
    void getContributionsByUserId_ShouldQueryWithoutAuthHeaderForPublicMRs() {
        Response response = mock(Response.class);
        when(response.getHeaderString("X-Total")).thenReturn("5");

        when(gitLabClient.getMergeRequests(eq("3589694"), eq("merged"), anyString(), anyString(), eq("all"), eq(100)))
                .thenReturn(response);

        List<CustomStatsContributionsUserByMonth> result = gitLabServices.getContributionsByUserId("3589694", "jeanphi", 2026);

        assertTrue(result.size() > 0);
        assertEquals(5, result.get(0).getContributions());
        verify(gitLabClient, org.mockito.Mockito.atLeastOnce()).getMergeRequests(eq("3589694"), eq("merged"), anyString(), anyString(), eq("all"), eq(100));
    }
}
