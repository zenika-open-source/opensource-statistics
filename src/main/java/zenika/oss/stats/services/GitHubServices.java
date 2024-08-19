package zenika.oss.stats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.config.GitHubClient;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;

@ApplicationScoped
public class GitHubServices {

    @Inject
    GitHubClient gitHubClient;
    
    /**
     * Retrieves members from a specified GitHub organization.
     * This method fetches the list of members belonging to the given organization using the GitHub API. It can be used to obtain
     * information about the members of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return a List of GHUsers
     * @throws IOException If there's an error communicating with the GitHub API.
     */
    public List<GHUser> getMembersFromAnOrganization(String organizationName) {

        try {
            return gitHubClient.getBuilder().getOrganization(organizationName)
                    .getMembers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get information for the current organization.
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return Organization information.
     */
    public GHOrganization getOrganizationInformation(String organizationName) {

        try {
            return gitHubClient.getBuilder().getOrganization(organizationName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
