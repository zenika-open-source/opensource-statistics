package zenika.oss.stats.config;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import io.smallrye.graphql.client.typesafe.api.NestedParameter;
import zenika.oss.stats.beans.gitlab.graphql.GitLabGraphQLUser;
import zenika.oss.stats.beans.gitlab.graphql.GitLabUsersNodes;
import java.util.List;

@GraphQLClientApi(configKey = "gitlab-api")
@AuthorizationHeader(type = AuthorizationHeader.Type.BEARER)
public interface GitLabGraphQLClient {

    @Query("user")
    GitLabGraphQLUser user(@NonNull String username);

    @Query("user")
    GitLabGraphQLUser userWithMRs(@Name("username") @NonNull String username,
            @NestedParameter("authoredMergeRequests") String createdAfter,
            @NestedParameter("authoredMergeRequests") String createdBefore,
            @NestedParameter("authoredMergeRequests") String state);

    @Query("users")
    GitLabUsersNodes users(@NonNull List<@NonNull String> usernames,
            @NestedParameter("authoredMergeRequests") String createdAfter,
            @NestedParameter("authoredMergeRequests") String createdBefore,
            @NestedParameter("authoredMergeRequests") String state);
}
