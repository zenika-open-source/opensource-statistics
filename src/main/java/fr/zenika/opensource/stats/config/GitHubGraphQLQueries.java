package fr.zenika.opensource.stats.config;

public class GitHubGraphQLQueries {

  public static final String qGetNumberOfContributionsForAPeriod = """
      query ($login: String!, $from: DateTime!, $to: DateTime!) {
        user(login: $login) {
          contributionsCollection(
            from: $from
            to: $to
          ) {
            pullRequestContributions(first: 100) {
              totalCount
              nodes {
                pullRequest {
                  title
                  url
                  state
                  createdAt
                  repository {
                    name
                    nameWithOwner
                    url
                    isPrivate
                  }
                }
              }
            }
          }
        }
      }
      """;

}

