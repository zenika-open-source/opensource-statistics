package zenika.oss.stats.config;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.NestedParameter;
import zenika.oss.stats.beans.ContributionsBean;

import org.eclipse.microprofile.graphql.Query;

@GraphQLClientApi(configKey = "github-api")
@AuthorizationHeader(type = AuthorizationHeader.Type.BEARER)
public interface GitHubGraphQLClient {
    
    @Query("""
                  query getUserContributions($login: String!) { user(login: $login) {
                  contributionsCollection {
                    totalIssueContributions,
                    totalCommitContributions,
                    totalPullRequestContributions,
                    totalPullRequestReviewContributions,
                    totalRepositoryContributions
                  }
                } 
              }
              """)
    ContributionsBean getUserContributions(@NestedParameter("login") String login);
    
}
