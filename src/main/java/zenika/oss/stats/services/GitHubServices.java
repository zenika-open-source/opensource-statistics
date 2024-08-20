package zenika.oss.stats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.GitHubMember;
import zenika.oss.stats.beans.GitHubOrganization;
import zenika.oss.stats.config.GitHubClient;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GitHubServices {

    @Inject
    @RestClient
    GitHubClient gitHubClient;

    /**
     * Retrieves members from a specified GitHub organization. This method fetches the list of members belonging to the given organization
     * using the GitHub API. It can be used to obtain information about the members of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return a List of GHUsers
     * @throws IOException If there's an error communicating with the GitHub API.
     */
    public List<GitHubMember> getOrganizationMembers(String organizationName) {

        return gitHubClient.getOrganizationMembers(organizationName);
    }

    /**
     * Get information for the current organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return Organization information.
     */
    public GitHubOrganization getOrganizationInformation(String organizationName) {
        return gitHubClient.getOrgnizationByName(organizationName);
    }

}
