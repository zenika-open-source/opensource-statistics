package zenika.oss.stats.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.gitlab.GitLabEvent;
import zenika.oss.stats.beans.gitlab.GitLabMember;
import zenika.oss.stats.beans.gitlab.GitLabProject;
import zenika.oss.stats.beans.gitlab.graphql.GitLabGraphQLUser;
import zenika.oss.stats.beans.gitlab.graphql.GitLabUsersNodes;
import zenika.oss.stats.config.GitLabClient;
import zenika.oss.stats.config.GitLabGraphQLClient;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitLabServices {

    @Inject
    @RestClient
    GitLabClient gitLabClient;

    @Inject
    GitLabGraphQLClient gitLabGraphQLClient;

    /**
     * Get basic information for a user.
     *
     * @param username The GitLab username.
     * @return An Optional containing the user information if found.
     */
    public Optional<GitLabMember> getUserInformation(String username) {
        List<GitLabMember> users = gitLabClient.getUserInformations(username);
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
                .map(user -> gitLabClient.getProjectsForAnUser(user.getId()))
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
        try {
            List<CustomStatsContributionsUserByMonth> result = new ArrayList<>();
            for (Month month : Month.values()) {
                if (year == LocalDate.now().getYear() && month.getValue() > LocalDate.now().getMonthValue()) {
                    break;
                }

                String startOfMonth = String.format("%d-%02d-01T00:00:00+00:00", year, month.getValue());
                LocalDate lastDayOfMonthDate = LocalDate.of(year, month,
                        month.length(LocalDate.of(year, month, 1).isLeapYear()));
                String endOfMonth = lastDayOfMonthDate.toString() + "T23:59:59+00:00";

                GitLabGraphQLUser userResponse = gitLabGraphQLClient.userWithMRs(username, startOfMonth,
                        endOfMonth, "merged");

                int count = 0;
                if (userResponse != null && userResponse.getAuthoredMergeRequests() != null) {
                    count = userResponse.getAuthoredMergeRequests().getCount();
                }

                result.add(new CustomStatsContributionsUserByMonth(
                        month.getValue(),
                        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        count));
            }
            return result;
        } catch (Exception e) {
            Log.warn("GitLab GraphQL (MRs query) failed for user " + username + ". Falling back to REST API.", e);
        }

        return getContributionsFromRestApi(username, year);
    }

    private List<CustomStatsContributionsUserByMonth> getContributionsFromRestApi(String username, int year) {
        Optional<GitLabMember> user = getUserInformation(username);
        if (user.isEmpty()) {
            return List.of();
        }

        String userId = user.get().getId();
        String after = year + "-01-01";
        String before = year + "-12-31";

        List<GitLabEvent> allEvents = new ArrayList<>();
        int page = 1;
        int perPage = 100;

        try {
            while (true) {
                List<GitLabEvent> events = gitLabClient.getEventsForAnUser(userId, after, before, perPage, page);
                if (events == null || events.isEmpty()) {
                    break;
                }
                allEvents.addAll(events);
                if (events.size() < perPage) {
                    break;
                }
                page++;
                // Safety limit to avoid infinite loops if API behaves unexpectedly
                if (page > 50)
                    break;
            }
        } catch (Exception e) {
            Log.error("Error fetching GitLab events for " + username, e);
        }

        Map<Month, Long> countsByMonth = allEvents.stream()
                .filter(e -> e.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        e -> LocalDate.parse(e.getCreatedAt().substring(0, 10)).getMonth(),
                        Collectors.counting()));

        List<CustomStatsContributionsUserByMonth> result = new ArrayList<>();
        for (Month month : Month.values()) {
            if (year == LocalDate.now().getYear() && month.getValue() > LocalDate.now().getMonthValue()) {
                break;
            }
            int count = countsByMonth.getOrDefault(month, 0L).intValue();
            result.add(new CustomStatsContributionsUserByMonth(
                    month.getValue(),
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    count));
        }
        return result;
    }
}
