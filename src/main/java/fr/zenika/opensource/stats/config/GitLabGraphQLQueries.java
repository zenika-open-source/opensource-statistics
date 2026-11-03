package fr.zenika.opensource.stats.config;

public class GitLabGraphQLQueries {

    public static final String qGetMergedMergeRequestsForAPeriod = """
        query ($username: String!, $updatedAfter: Time, $updatedBefore: Time) {
          user(username: $username) {
            authoredMergeRequests(
              state: merged
              updatedAfter: $updatedAfter
              updatedBefore: $updatedBefore
              first: 100
            ) {
              count
              nodes {
                title
                webUrl
                state
                createdAt
                project {
                  name
                  fullPath
                  webUrl
                  visibility
                }
              }
            }
          }
        }
        """;
}

