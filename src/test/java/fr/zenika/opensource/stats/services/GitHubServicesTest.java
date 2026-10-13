package fr.zenika.opensource.stats.services;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.beans.github.GitHubOrganization;
import fr.zenika.opensource.stats.beans.github.GitHubProject;
import fr.zenika.opensource.stats.config.GitHubClient;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        gitHubServices.githubToken = java.util.Optional.empty();
    }

    @Test
    void test_getOrganizationInformation() {
        var organizationName = "testOrg";
        var organizationDescription = "description";

        GitHubOrganization expectedOrganization = new GitHubOrganization();
        expectedOrganization.setName(organizationName);
        expectedOrganization.setDescription(organizationDescription);

        when(gitHubClient.getOrgnizationByName(any(), eq(organizationName))).thenReturn(expectedOrganization);

        GitHubOrganization result = gitHubServices.getOrganizationInformation(organizationName);

        assertNotNull(result);
        assertEquals(organizationName, result.getName());
        assertEquals(organizationDescription, result.getDescription());

        verify(gitHubClient, times(1)).getOrgnizationByName(any(), eq(organizationName));
    }

    @Test
    void test_getOrganizationInformation_Null() {
        when(gitHubClient.getOrgnizationByName(any(), eq(null))).thenReturn(null);

        GitHubOrganization result = gitHubServices.getOrganizationInformation(null);

        assertNull(result);

        verify(gitHubClient, times(1)).getOrgnizationByName(any(), eq(null));
    }

    @Test
    void test_getUserInformation() {
        var idUser = "idUser";
        GitHubMember githubMember = new GitHubMember();
        githubMember.setId(idUser);
        githubMember.setLogin("login");
        githubMember.setType("type");

        when(gitHubClient.getUserInformation(any(), eq(idUser))).thenReturn(githubMember);

        final GitHubMember result = gitHubServices.getUserInformation(idUser);

        assertNotNull(result);

        verify(gitHubClient, times(1)).getUserInformation(any(), eq(idUser));
    }

    @Test
    void test_getPersonalProjectForAnUser() {
        String login = "testUser";
        List<GitHubProject> expectedProjects = Arrays.asList(
                new GitHubProject(),
                new GitHubProject());

        when(gitHubClient.getReposForAnUser(any(), eq(login), eq(100), eq(1))).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getPersonalProjectForAnUser(login);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);

        verify(gitHubClient, times(1)).getReposForAnUser(any(), eq(login), eq(100), eq(1));
    }

    @Test
    void test_getForkedProjectForAnUser() {
        String login = "testUser";
        GitHubProject fork = new GitHubProject();
        fork.setFork(true);
        List<GitHubProject> expectedProjects = Arrays.asList(fork);

        when(gitHubClient.getReposForAnUser(any(), eq(login), eq(100), eq(1))).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getForkedProjectForAnUser(login);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gitHubClient, times(1)).getReposForAnUser(any(), eq(login), eq(100), eq(1));
    }

    @Test
    void test_getOrganizationMembers_MultiPage() {
        String org = "testOrg";
        List<GitHubMember> page1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            page1.add(new GitHubMember());
        }
        List<GitHubMember> page2 = Arrays.asList(new GitHubMember());

        when(gitHubClient.getOrganizationMembers(any(), eq(org), eq(100), eq(1))).thenReturn(page1);
        when(gitHubClient.getOrganizationMembers(any(), eq(org), eq(100), eq(2))).thenReturn(page2);

        List<GitHubMember> result = gitHubServices.getOrganizationMembers(org);

        assertNotNull(result);
        assertEquals(101, result.size());
        verify(gitHubClient, times(1)).getOrganizationMembers(any(), eq(org), eq(100), eq(1));
        verify(gitHubClient, times(1)).getOrganizationMembers(any(), eq(org), eq(100), eq(2));
    }

    @Test
    void test_getOrganizationProjects() {
        String org = "testOrg";
        GitHubProject project = new GitHubProject();
        project.setFork(false);
        project.setArchived(false);
        List<GitHubProject> expectedProjects = Arrays.asList(project);

        when(gitHubClient.getOrganizationProjects(any(), eq(org), eq(100), eq(1))).thenReturn(expectedProjects);

        List<GitHubProject> result = gitHubServices.getOrganizationProjects(org);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gitHubClient, times(1)).getOrganizationProjects(any(), eq(org), eq(100), eq(1));
    }

    @Test
    void test_extractGitHubContributionDetails_FiltersPrivateAndMapsPublic() {
        fr.zenika.opensource.stats.beans.github.graphql.PullRequestRepository publicRepo = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestRepository();
        publicRepo.setName("opensource-statistics");
        publicRepo.setNameWithOwner("zenika-open-source/opensource-statistics");
        publicRepo.setUrl("https://github.com/zenika-open-source/opensource-statistics");
        publicRepo.setPrivate(false);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestNode publicPr = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestNode();
        publicPr.setTitle("feat: track PRs");
        publicPr.setUrl("https://github.com/zenika-open-source/opensource-statistics/pull/10");
        publicPr.setState("MERGED");
        publicPr.setCreatedAt("2026-01-15T10:00:00Z");
        publicPr.setRepository(publicRepo);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributionNode node1 = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributionNode();
        node1.setPullRequest(publicPr);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestRepository privateRepo = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestRepository();
        privateRepo.setName("secret");
        privateRepo.setNameWithOwner("org/secret");
        privateRepo.setUrl("https://github.com/org/secret");
        privateRepo.setPrivate(true);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestNode privatePr = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestNode();
        privatePr.setTitle("chore: secret");
        privatePr.setUrl("https://github.com/org/secret/pull/1");
        privatePr.setState("MERGED");
        privatePr.setRepository(privateRepo);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributionNode node2 = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributionNode();
        node2.setPullRequest(privatePr);

        fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributions prContributions = new fr.zenika.opensource.stats.beans.github.graphql.PullRequestContributions();
        prContributions.setTotalCount(2);
        prContributions.setNodes(Arrays.asList(node1, node2));

        var details = gitHubServices.extractGitHubContributionDetails(prContributions);

        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals("opensource-statistics", details.get(0).getProjectName());
        assertEquals("zenika-open-source/opensource-statistics", details.get(0).getProjectFullName());
        assertEquals("https://github.com/zenika-open-source/opensource-statistics", details.get(0).getProjectUrl());
        assertEquals("feat: track PRs", details.get(0).getPrTitle());
        assertEquals("https://github.com/zenika-open-source/opensource-statistics/pull/10", details.get(0).getPrUrl());
        assertEquals("MERGED", details.get(0).getPrState());
    }
}

