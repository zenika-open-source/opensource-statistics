package zenika.oss.stats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

@ApplicationScoped
public class GitHubServices {

    GitHub github;

    @ConfigProperty(name = "github.token")
    String gitHubToken;
    
    @Inject
    public GitHubServices() throws IOException {
        github = new GitHubBuilder().withOAuthToken("my_personal_token").build();
    }
    
    /**
     * Retrieves members from a specified GitHub organization.
     *
     * This method fetches the list of members belonging to the given organization using the GitHub API. It can be used to obtain
     * information about the members of a particular GitHub organization.
     *
     * @param organizationName The name of the GitHub organization to fetch members from.
     * @return
     * @throws IOException If there's an error communicating with the GitHub API.
     */
    public List<GHUser> getMembersFromAnOrganization(String organizationName) {

        try {
            return github.getOrganization(organizationName)
                    .getMembers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
