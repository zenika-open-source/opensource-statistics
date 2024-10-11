package zenika.oss.stats.config;

public class GitHubGraphQLQueries {

    public static final String qGetNumberOfContributionsForAPeriod = """
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

}
