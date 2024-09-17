package zenika.oss.stats.services;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ContributionsBean;
import zenika.oss.stats.beans.GitHubMember;
import zenika.oss.stats.beans.GitHubOrganization;
import zenika.oss.stats.beans.GitHubProject;
import zenika.oss.stats.config.GitHubClient;
import zenika.oss.stats.config.GitHubGraphQLClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;

@ApplicationScoped
public class GitHubServices {

    private static final int NB_MEMBERS_PAR_PAGE= 100;
    
    @Inject
    @RestClient
    GitHubClient gitHubClient;
    
    @Inject
    GitHubGraphQLClient gitHubGraphQLClient;

    @Inject
    @GraphQLClient("github-api-dynamic")
    DynamicGraphQLClient dynamicGraphQLClient;
    /**
     * Get information for the current organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return Organization information.
     */
    public GitHubOrganization getOrganizationInformation(String organizationName) {

        return gitHubClient.getOrgnizationByName(organizationName);
    }

    /**
     * Retrieves members from a specified GitHub organization. This method fetches the list of members belonging to the given organization
     * using the GitHub API. It can be used to obtain information about the members of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
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
        return repos.stream().filter(repo -> !repo.isFork()).collect(Collectors.toList());
    }
    
    /**
     * Get forked project (ie no forked) for a user.
     *
     * @param login : id of the user
     * @return a list of public projects created by the user.
     */
    public List<GitHubProject> getForkedProjectForAnUser(final String login) {
        var repos = gitHubClient.getReposForAnUser(login);
        return repos.stream().filter(repo -> repo.isFork()).collect(Collectors.toList());
    }

    public ContributionsBean getContributionsData(final String login) {

        return gitHubGraphQLClient.getUserContributions(login);
    }


    public ContributionsBean getContributionsDataDynamic(final String login) {
        Response response = null;
        try {
                
        /*
        Document query = document(
                operation(
                        field("getUserContributions", 
                                field("user",
                                        field("contributionsCollection",
                                            field("totalIssueContributions"),
                                            field("totalCommitContributions"),
                                            field("totalPullRequestContributions"), 
                                                field("totalPullRequestReviewContributions"),
                                                field("totalRepositoryContributions")
                                        )
                                )
                        )
                )
        );
       
            response = dynamicGraphQLClient.executeSync(query);
         */
            response = dynamicGraphQLClient.executeSync("""
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
              """, login);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.getObject(ContributionsBean.class, "getUserContributions");
        
    }
}
