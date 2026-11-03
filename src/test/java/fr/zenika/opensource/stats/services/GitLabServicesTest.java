package fr.zenika.opensource.stats.services;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabGraphQLUser;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestNode;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestProject;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestsCount;
import fr.zenika.opensource.stats.config.GitLabClient;
import fr.zenika.opensource.stats.config.GitLabGraphQLQueries;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitLabServicesTest {

    @Mock
    GitLabClient gitLabClient;

    @Mock
    DynamicGraphQLClient dynamicGraphQLClient;

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
    void getContributionsByUserId_ShouldQueryGraphQLAndFilterOnlyPublicMRs() throws Exception {
        GitLabMergeRequestProject publicProject = new GitLabMergeRequestProject();
        publicProject.setName("communities-api");
        publicProject.setFullPath("communities-api/communities-api");
        publicProject.setWebUrl("https://gitlab.com/communities-api/communities-api");
        publicProject.setVisibility("public");

        GitLabMergeRequestNode publicMr = new GitLabMergeRequestNode();
        publicMr.setTitle("feat: public mr");
        publicMr.setWebUrl("https://gitlab.com/communities-api/communities-api/-/merge_requests/1");
        publicMr.setState("merged");
        publicMr.setCreatedAt("2026-01-10T10:00:00Z");
        publicMr.setProject(publicProject);

        GitLabMergeRequestProject privateProject = new GitLabMergeRequestProject();
        privateProject.setName("arbregen");
        privateProject.setFullPath("arbre-gen/arbregen");
        privateProject.setWebUrl("https://gitlab.com/arbre-gen/arbregen");
        privateProject.setVisibility("private");

        GitLabMergeRequestNode privateMr = new GitLabMergeRequestNode();
        privateMr.setTitle("chore: private mr");
        privateMr.setWebUrl("https://gitlab.com/arbre-gen/arbregen/-/merge_requests/2");
        privateMr.setState("merged");
        privateMr.setCreatedAt("2026-01-11T10:00:00Z");
        privateMr.setProject(privateProject);

        GitLabMergeRequestsCount mrContributions = new GitLabMergeRequestsCount();
        mrContributions.setCount(2);
        mrContributions.setNodes(List.of(publicMr, privateMr));

        GitLabGraphQLUser gqlUser = new GitLabGraphQLUser();
        gqlUser.setAuthoredMergeRequests(mrContributions);

        io.smallrye.graphql.client.Response gqlResponse = mock(io.smallrye.graphql.client.Response.class);
        when(gqlResponse.getObject(GitLabGraphQLUser.class, "user")).thenReturn(gqlUser);

        when(dynamicGraphQLClient.executeSync(eq(GitLabGraphQLQueries.qGetMergedMergeRequestsForAPeriod), anyMap()))
                .thenReturn(gqlResponse);

        List<CustomStatsContributionsUserByMonth> result = gitLabServices.getContributionsByUserId("3589694", "jeanphi", 2026);

        assertTrue(result.size() > 0);
        assertEquals(1, result.get(0).getContributions());
        List<ContributionDetail> details = result.get(0).getContributionsDetails();
        assertEquals(1, details.size());
        assertEquals("communities-api/communities-api", details.get(0).getProjectFullName());
        verify(dynamicGraphQLClient, org.mockito.Mockito.atLeastOnce())
                .executeSync(eq(GitLabGraphQLQueries.qGetMergedMergeRequestsForAPeriod), anyMap());
    }

    @Test
    void graphqlResponseDeserialization_ShouldFilterPrivateProject() {
        String json = """
                {
                  "data": {
                    "user": {
                      "authoredMergeRequests": {
                        "count": 2,
                        "nodes": [
                          {
                            "title": "feat: private mr",
                            "webUrl": "https://gitlab.com/arbre-gen/arbregen/-/merge_requests/277",
                            "state": "merged",
                            "createdAt": "2025-12-30T07:44:02Z",
                            "project": {
                              "name": "arbregen",
                              "fullPath": "arbre-gen/arbregen",
                              "webUrl": "https://gitlab.com/arbre-gen/arbregen",
                              "visibility": "private"
                            }
                          },
                          {
                            "title": "feat: public mr",
                            "webUrl": "https://gitlab.com/communities-api/communities-api/-/merge_requests/298",
                            "state": "merged",
                            "createdAt": "2026-11-02T07:05:16Z",
                            "project": {
                              "name": "communities-api",
                              "fullPath": "communities-api/communities-api",
                              "webUrl": "https://gitlab.com/communities-api/communities-api",
                              "visibility": "public"
                            }
                          }
                        ]
                      }
                    }
                  }
                }
                """;
        io.smallrye.graphql.client.Response response = io.smallrye.graphql.client.impl.ResponseReader.readFrom(json, null);
        GitLabGraphQLUser userStats = response.getObject(GitLabGraphQLUser.class, "user");
        List<ContributionDetail> details = gitLabServices.extractGitLabGraphQLContributionDetails(userStats.getAuthoredMergeRequests());

        assertEquals(1, details.size());
        assertEquals("communities-api/communities-api", details.get(0).getProjectFullName());
        assertEquals("communities-api", details.get(0).getProjectName());
    }
}
