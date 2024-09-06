package zenika.oss.stats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.GitHubMember;
import zenika.oss.stats.beans.GitHubOrganization;
import zenika.oss.stats.beans.GitHubProject;
import zenika.oss.stats.config.GitHubClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GitHubServices {

    private static final int NB_MEMBERS_PAR_PAGE= 100;
    
    @Inject
    @RestClient
    GitHubClient gitHubClient;

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
}
