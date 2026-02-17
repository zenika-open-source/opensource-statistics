package zenika.oss.stats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import zenika.oss.stats.beans.gitlab.GitLabMember;
import zenika.oss.stats.beans.gitlab.GitLabProject;
import zenika.oss.stats.config.GitLabClient;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitLabServices {

    @Inject
    @RestClient
    GitLabClient gitLabClient;

    /**
     * Get basic information for a user.
     *
     * @param username The GitLab username.
     * @return The user information.
     */
    public GitLabMember getUserInformation(String username) {
        List<GitLabMember> users = gitLabClient.getUserInformations(username);
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * Get personal projects for a user.
     *
     * @param username The GitLab username.
     * @return A list of personal projects (not forks).
     */
    public List<GitLabProject> getPersonalProjectsForAnUser(String username) {
        GitLabMember user = getUserInformation(username);
        if (user == null) {
            return List.of();
        }

        List<GitLabProject> projects = gitLabClient.getProjectsForAnUser(user.getId());
        return projects.stream()
                .filter(project -> !project.isFork())
                .collect(Collectors.toList());
    }
}
