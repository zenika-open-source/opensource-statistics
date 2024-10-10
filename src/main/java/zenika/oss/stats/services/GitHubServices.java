package zenika.oss.stats.services;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.GitHubMember;
import zenika.oss.stats.beans.GitHubOrganization;
import zenika.oss.stats.beans.GitHubProject;
import zenika.oss.stats.beans.User;
import zenika.oss.stats.beans.UserStatsNumberContributions;
import zenika.oss.stats.config.GitHubClient;
import zenika.oss.stats.config.GitHubGraphQLClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GitHubServices {

    private static final int NB_MEMBERS_PAR_PAGE = 100;

    @Inject
    @RestClient
    GitHubClient gitHubClient;

    @Inject
    GitHubGraphQLClient gitHubGraphQLClient;

    @Inject
    @GraphQLClient("github-api-dynamic")
    DynamicGraphQLClient dynamicGraphQLClient;

    /**
     * Get information for the current organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return Organization information.
     */
    public GitHubOrganization getOrganizationInformation(String organizationName) {

        return gitHubClient.getOrgnizationByName(organizationName);
    }

    /**
     * Retrieves members from a specified GitHub organization. This method fetches the list of members belonging to the given organization
     * using the GitHub API. It can be used to obtain information about the members of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return a List of GHUsers
     * @throws IOException If there's an error communicating with the GitHub API.
     */
    public List<GitHubMember> getOrganizationMembers(String organizationName) {

        return gitHubClient.getOrganizationMembers(organizationName, NB_MEMBERS_PAR_PAGE);
    }

    /**
     * Get information for a user.
     *
     * @param id : id of the user
     * @return user
     */
    public GitHubMember getUserInformation(final String id) {

        return gitHubClient.getUserInformation(id);
    }

    /**
     * Get personal project (ie no forked) for a user.
     *
     * @param login : id of the user
     * @return a list of public projects created by the user.
     */
    public List<GitHubProject> getPersonalProjectForAnUser(final String login) {

        var repos = gitHubClient.getReposForAnUser(login);
        return repos.stream()
                .filter(repo -> !repo.isFork())
                .collect(Collectors.toList());
    }

    /**
     * Get forked project (ie no forked) for a user.
     *
     * @param login : id of the user
     * @return a list of public projects created by the user.
     */
    public List<GitHubProject> getForkedProjectForAnUser(final String login) {

        var repos = gitHubClient.getReposForAnUser(login);
        return repos.stream()
                .filter(repo -> repo.isFork())
                .collect(Collectors.toList());
    }

    public User getContributionsData(final String login) {

        return gitHubGraphQLClient.user(login);
    }

    public User getContributionsDataDynamic(final String login) {

        Response response = null;
        try {
            var variables = new HashMap<String, Object>(); // <3>
            variables.put("login", login);
            String query = "query($login: String!) {\n" + "                    user(login: $login) {\n" +
                    "                        contributionsCollection {\n" + "                            totalIssueContributions,\n" +
                    "                            totalCommitContributions,\n" +
                    "                            totalPullRequestContributions,\n" +
                    "                            totalPullRequestReviewContributions,\n" +
                    "                            totalRepositoryContributions\n" + "                        }\n" +
                    "                    }\n" + "                }";

            response = dynamicGraphQLClient.executeSync(query, variables);

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.getObject(User.class, "user");

    }

    /**
     * Get contributions for the current year.
     *
     * @param login
     * @param year : year to search number of contributions
     * @return a map of String (Month), Integer (number of contributions)
     */
    public UserStatsNumberContributions getContributionsForTheCurrentYear(final String login, final int year) {

        Response response = null;
        try {
            var variables = new HashMap<String, Object>();
            variables.put("login", login);
            variables.put("from", "2024-01-01T23:59:59Z");
            variables.put("to", "2024-10-31T23:59:59Z");

            String query = """
                    query ($login: String!, $from: DateTime!, $to: DateTime!) {
                      user(login: $login) {
                        contributionsCollection(
                          from: $from
                          to: $to
                        ) {
                          pullRequestContributions(first: 1) {
                            totalCount
                          }
                        }
                      }
                    }
                    """;
            response = dynamicGraphQLClient.executeSync(query, variables);

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } return response.getObject(UserStatsNumberContributions.class, "user");
    }
}
