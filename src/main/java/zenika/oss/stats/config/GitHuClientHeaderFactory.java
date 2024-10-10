package zenika.oss.stats.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@ApplicationScoped
public class GitHuClientHeaderFactory implements ClientHeadersFactory {

    @ConfigProperty(name = "github.token")
    String token;
    
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("Accept", "application/vnd.github+json");
        result.add("Authorization", "Bearer" + token);
        result.add("X-GitHub-Api-Version", "2022-11-28");
        return result;
    }
}
