package zenika.oss.stats.services;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.CustomStatsUser;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.github.GitHubOrganization;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.beans.github.graphql.PullRequestContributions;
import zenika.oss.stats.beans.github.graphql.User;
import zenika.oss.stats.beans.github.graphql.UserStatsNumberContributions;
import zenika.oss.stats.config.GitHubClient;
import zenika.oss.stats.config.GitHubGraphQLClient;
import zenika.oss.stats.config.GitHubGraphQLQueries;

import java.io.IOException;
import java.time.*;

import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitHubServices {

    private static final int NB_MEMBERS_PAR_PAGE = 100;

    @ConfigProperty(name = "organization.name")
    String organizationName;

    @Inject
    @RestClient
    GitHubClient gitHubClient;

    @Inject
    GitHubGraphQLClient gitHubGraphQLClient;

    @ConfigProperty(name = "quarkus.smallrye-graphql-client.github-api-dynamic.url")
    String graphQLUrl;

    @ConfigProperty(name = "github.token")
    String githubToken;

    DynamicGraphQLClient dynamicGraphQLClient;

    @PostConstruct
    public void init() {
        dynamicGraphQLClient = DynamicGraphQLClientBuilder.newBuilder()
                .url(graphQLUrl)
                .header("Authorization", "Bearer " + githubToken)
                .build();
    }

    /**
     * Get information for the current organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members
     *                         from.
     * @return Organization information.
     */
    public GitHubOrganization getOrganizationInformation(String organizationName) {

        return gitHubClient.getOrgnizationByName(organizationName);
    }

    /**
     * Retrieves members from a specified GitHub organization. This method fetches
     * the list of members belonging to the given organization
     * using the GitHub API. It can be used to obtain information about the members
     * of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members
     *                         from.
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
                .filter(repo -> !repo.isFork() && !repo.isArchived())
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
                .filter(GitHubProject::isFork)
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
                    "                        contributionsCollection {\n"
                    + "                          totalIssueContributions,\n" +
                    "                            totalCommitContributions,\n" +
                    "                            totalPullRequestContributions,\n" +
                    "                            totalPullRequestReviewContributions,\n" +
                    "                            totalRepositoryContributions\n" + "                        }\n" +
                    "                    }\n" + "                }";

            response = dynamicGraphQLClient.executeSync(query, variables);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.getObject(User.class, "user");

    }

    /**
     * Get contributions for the current year.
     *
     * @param login
     * @param year  : year to search number of contributions
     * @return a map of String (Month), Integer (number of contributions)
     */
    public List<CustomStatsContributionsUserByMonth> getContributionsForTheCurrentYear(final String login,
            final int year) {

        Response response = null;
        PullRequestContributions prContributions = null;
        var contributionsTab = new ArrayList<CustomStatsContributionsUserByMonth>();

        try {

            for (Month month : Month.values()) {
                if (year == Year.now().getValue() && month.getValue() > LocalDate.now().getMonthValue()) {
                    break;
                }
                var variables = new HashMap<String, Object>();
                variables.put("login", login);

                ZonedDateTime firstDayOfMonth = YearMonth.of(year, month)
                        .atDay(1)
                        .atTime(0, 0, 0)
                        .atZone(ZoneOffset.UTC);
                ZonedDateTime lastDayOfMonth = YearMonth.of(year, month)
                        .atEndOfMonth()
                        .atTime(23, 59, 59)
                        .atZone(ZoneOffset.UTC);

                variables.put("from", firstDayOfMonth.toInstant().toString());
                variables.put("to", lastDayOfMonth.toInstant().toString());

                response = dynamicGraphQLClient.executeSync(GitHubGraphQLQueries.qGetNumberOfContributionsForAPeriod,
                        variables);
                var userStats = response.getObject(UserStatsNumberContributions.class, "user");

                if (userStats != null) {
                    prContributions = userStats.getContributionsCollection().getPullRequestContributions();

                    contributionsTab.add(
                            new CustomStatsContributionsUserByMonth(month.getValue(),
                                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                                    prContributions.getTotalCount()));
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return contributionsTab;
    }

    /**
     * Get contributions for all the organization members for the current year .
     *
     * @param organizationName : organization name
     * @return a map of String (Month), Integer (number of contributions)
     */
    public List<CustomStatsUser> getContributionsForTheCurrentYearAndAllTheOrganizationMembers(
            final String organizationName) {

        var statsMembers = new ArrayList<CustomStatsUser>();

        var members = this.getOrganizationMembers(organizationName);
        var currentYear = Year.now().getValue();

        members.stream()
                .map(member -> {
                    return statsMembers.add(new CustomStatsUser(member.getLogin(), currentYear,
                            this.getContributionsForTheCurrentYear(member.login, currentYear)));
                })
                .collect(Collectors.toList());

        return statsMembers;
    }

    public List<GitHubMember> getZenikaOpenSourceMembers() {
        return gitHubClient.getOrganizationMembers(organizationName, NB_MEMBERS_PAR_PAGE);
    }
}
