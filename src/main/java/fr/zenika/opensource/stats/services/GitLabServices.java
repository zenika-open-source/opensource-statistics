package fr.zenika.opensource.stats.services;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMergeRequest;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabGraphQLUser;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestNode;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestProject;
import fr.zenika.opensource.stats.beans.gitlab.graphql.GitLabMergeRequestsCount;
import fr.zenika.opensource.stats.config.GitLabClient;
import fr.zenika.opensource.stats.config.GitLabGraphQLQueries;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitLabServices {

    @Inject
    @RestClient
    GitLabClient gitLabClient;

    @ConfigProperty(name = "quarkus.smallrye-graphql-client.gitlab-api.url", defaultValue = "https://gitlab.com/api/graphql")
    String graphQLUrl;

    @Inject
    @ConfigProperty(name = "gitlab.token")
    Optional<String> gitlabToken;

    DynamicGraphQLClient dynamicGraphQLClient;

    @PostConstruct
    public void init() {
        dynamicGraphQLClient = DynamicGraphQLClientBuilder.newBuilder()
                .url(graphQLUrl)
                .header("Authorization", getAuthHeader())
                .build();
    }

    private String getAuthHeader() {
        return "Bearer " + gitlabToken.orElse("");
    }

    /**
     * Get basic information for a user.
     *
     * @param username The GitLab username.
     * @return An Optional containing the user information if found.
     */
    public Optional<GitLabMember> getUserInformation(String username) {
        List<GitLabMember> users = gitLabClient.getUserInformations(getAuthHeader(), username);
        if (users != null && !users.isEmpty()) {
            return Optional.of(users.get(0));
        }
        return Optional.empty();
    }

    /**
     * Get personal projects for a user.
     *
     * @param username The GitLab username.
     * @return A list of public personal projects (not forks).
     */
    public List<GitLabProject> getPersonalProjectsForAnUser(String username) {
        return getUserInformation(username)
                .map(user -> gitLabClient.getProjectsForAnUser(getAuthHeader(), user.getId(), "public"))
                .map(projects -> projects.stream()
                        .filter(project -> !project.isFork() && project.isPublic())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Get contributions for the current year.
     *
     * @param username The GitLab username.
     * @param year     The year to fetch contributions for.
     * @return A list of monthly contribution stats.
     */
    public List<CustomStatsContributionsUserByMonth> getContributionsForTheCurrentYear(String username, int year) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return getContributionsByUserId(null, username, year);
    }

    /**
     * Get contributions for a specific GitLab user.
     *
     * @param userId   The numeric GitLab user ID (optional when using GraphQL by username).
     * @param username The GitLab username.
     * @param year     The year.
     * @return A list of monthly contribution stats.
     */
    public List<CustomStatsContributionsUserByMonth> getContributionsByUserId(String userId, String username,
            int year) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        List<CustomStatsContributionsUserByMonth> result = new ArrayList<>();

        for (Month month : Month.values()) {
            if (year == LocalDate.now().getYear() && month.getValue() > LocalDate.now().getMonthValue()) {
                break;
            }

            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            String updatedAfter = startOfMonth.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            String updatedBefore = endOfMonth.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

            try {
                var variables = new HashMap<String, Object>();
                variables.put("username", username);
                variables.put("updatedAfter", updatedAfter);
                variables.put("updatedBefore", updatedBefore);

                io.smallrye.graphql.client.Response gqlResponse = dynamicGraphQLClient.executeSync(
                        GitLabGraphQLQueries.qGetMergedMergeRequestsForAPeriod,
                        variables);

                GitLabGraphQLUser userStats = Optional.ofNullable(gqlResponse)
                        .map(r -> r.getObject(GitLabGraphQLUser.class, "user"))
                        .orElse(null);

                GitLabMergeRequestsCount mrContributions = Optional.ofNullable(userStats)
                        .map(GitLabGraphQLUser::getAuthoredMergeRequests)
                        .orElse(null);

                List<ContributionDetail> details = extractGitLabGraphQLContributionDetails(mrContributions);
                int total = details.size();

                result.add(new CustomStatsContributionsUserByMonth(
                        month.getValue(),
                        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        total,
                        details));
                Log.info("Fetching GitLab merged MRs for " + username + " in " + month + " "
                        + year + " | " + total);

            } catch (Exception e) {
                Log.error("Error fetching GitLab MRs for " + username + " in " + month, e);
                result.add(new CustomStatsContributionsUserByMonth(
                        month.getValue(),
                        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        0));
            }
        }
        return result;
    }

    List<ContributionDetail> extractGitLabGraphQLContributionDetails(GitLabMergeRequestsCount mrContributions) {
        return Optional.ofNullable(mrContributions)
                .map(GitLabMergeRequestsCount::getNodes)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .filter(mr -> Optional.ofNullable(mr.getProject())
                        .map(GitLabMergeRequestProject::isPublic)
                        .orElse(true))
                .map(this::mapMergeRequestNodeToDetail)
                .collect(Collectors.toList());
    }

    private ContributionDetail mapMergeRequestNodeToDetail(GitLabMergeRequestNode mr) {
        Optional<GitLabMergeRequestProject> projOpt = Optional.ofNullable(mr.getProject());
        String projectName = projOpt.map(GitLabMergeRequestProject::getName).orElse("");
        String projectFullName = projOpt.map(GitLabMergeRequestProject::getFullPath).orElse(projectName);
        String projectUrl = projOpt.map(GitLabMergeRequestProject::getWebUrl).orElse("");
        return new ContributionDetail(
                projectName,
                projectFullName,
                projectUrl,
                Optional.ofNullable(mr.getTitle()).orElse(""),
                Optional.ofNullable(mr.getWebUrl()).orElse(""),
                Optional.ofNullable(mr.getState()).orElse(""),
                Optional.ofNullable(mr.getCreatedAt()).orElse(""));
    }

    List<ContributionDetail> extractGitLabContributionDetails(Response response) {
        if (response == null || !response.hasEntity()) {
            return List.of();
        }
        try {
            List<GitLabMergeRequest> mrs = response.readEntity(new GenericType<List<GitLabMergeRequest>>() {
            });
            return Optional.ofNullable(mrs)
                    .orElse(List.of())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(GitLabMergeRequest::toContributionDetail)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            Log.warn("Could not deserialize GitLab merge requests details", e);
            return List.of();
        }
    }
}

