package zenika.oss.stats.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

@ApplicationScoped
public class GitHubClient {

    private final GitHub builder;
    
    public GitHubClient (@ConfigProperty(name = "github.token") String token) {

        try {
            this.builder = new GitHubBuilder().withOAuthToken(token).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GitHub getBuilder() {

        return builder;
    }

}
