package zenika.oss.stats.config;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import zenika.oss.stats.beans.User;

import org.eclipse.microprofile.graphql.NonNull;

@GraphQLClientApi(configKey = "github-api")
@AuthorizationHeader(type = AuthorizationHeader.Type.BEARER)
public interface GitHubGraphQLClient {
    
    
    User user(@NonNull String login);
    
}
