package fr.zenika.opensource.stats.services;

import io.quarkus.logging.Log;
import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.CustomStatsContributionsUserByMonth;
import fr.zenika.opensource.stats.beans.gitlab.GitLabEvent;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMergeRequest;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;
import fr.zenika.opensource.stats.config.GitLabClient;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitLabServices {

    private static final int NB_MRS_PER_PAGE = 100;

    @Inject
    @RestClient
    GitLabClient gitLabClient;

    @Inject
    @ConfigProperty(name = "gitlab.token")
    Optional<String> gitlabToken;

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
     * @return A list of personal projects (not forks).
     */
    public List<GitLabProject> getPersonalProjectsForAnUser(String username) {
        return getUserInformation(username)
                .map(user -> gitLabClient.getProjectsForAnUser(getAuthHeader(), user.getId()))
                .map(projects -> projects.stream()
                        .filter(project -> !project.isFork())
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
        return getUserInformation(username)
                .map(user -> getContributionsByUserId(user.getId(), username, year))
                .orElse(List.of());
    }

    /**
     * Get contributions for a specific GitLab user ID.
     *
     * @param userId   The numeric GitLab user ID.
     * @param username The username (for logging).
     * @param year     The year.
     * @return A list of monthly contribution stats.
     */
    public List<CustomStatsContributionsUserByMonth> getContributionsByUserId(String userId, String username,
            int year) {
        List<CustomStatsContributionsUserByMonth> result = new ArrayList<>();

        // If userId is not numeric, it might be a username from old data. Try to
        // resolve it.
        String actualId = userId;
        if (userId != null && !userId.matches("\\d+")) {
            Log.warn("GitLab ID '" + userId + "' for " + username
                    + " is not numeric. Attempting to resolve ID from username.");
            Optional<GitLabMember> user = getUserInformation(username);
            if (user.isPresent()) {
                actualId = user.get().getId();
                Log.info("Resolved GitLab username '" + username + "' to ID '" + actualId + "'");
            } else {
                Log.error("Failed to resolve GitLab ID for username '" + username + "'. Aborting contribution fetch.");
                return List.of();
            }
        }

        for (Month month : Month.values()) {
            if (year == LocalDate.now().getYear() && month.getValue() > LocalDate.now().getMonthValue()) {
                break;
            }

            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            String updatedAfter = startOfMonth.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            String updatedBefore = endOfMonth.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

            try {
                // Use scope=all to see MRs from all users, not just the token owner
                // Use updated_after/before for better compatibility with older GitLab versions
                Response response = gitLabClient.getMergeRequests(getAuthHeader(), actualId, "merged", updatedAfter, updatedBefore,
                        "all", NB_MRS_PER_PAGE);
                String totalStr = response.getHeaderString("X-Total");
                List<ContributionDetail> details = extractGitLabContributionDetails(response);

                int total = Optional.ofNullable(totalStr)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(details.size());

                result.add(new CustomStatsContributionsUserByMonth(
                        month.getValue(),
                        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        total,
                        details));
                Log.info("Fetching GitLab merged MRs for " + username + " (ID: " + actualId + ") in " + month + " "
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

