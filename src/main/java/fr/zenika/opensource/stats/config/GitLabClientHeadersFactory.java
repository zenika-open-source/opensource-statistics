package fr.zenika.opensource.stats.config;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Optional;

@ApplicationScoped
public class GitLabClientHeadersFactory implements ClientHeadersFactory {

    @Inject
    @ConfigProperty(name = "gitlab.token")
    Optional<String> gitlabToken;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.putSingle("Authorization", "Bearer " + gitlabToken.orElse(""));
        return result;
    }
}
