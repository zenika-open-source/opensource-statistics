package fr.zenika.opensource.stats.beans.github;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubProjectTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_ShouldMapPushedAtToLastActivityAt() throws IOException {
        String json = "{\"name\":\"test-project\",\"pushed_at\":\"2024-05-01T12:00:00Z\"}";

        GitHubProject result = objectMapper.readValue(json, GitHubProject.class);

        assertEquals("test-project", result.getName());
        assertEquals("2024-05-01T12:00:00Z", result.getLastActivityAt());
    }

    @Test
    void deserialize_WithoutPushedAt_ShouldLeaveLastActivityAtNull() throws IOException {
        String json = "{\"name\":\"test-project\"}";

        GitHubProject result = objectMapper.readValue(json, GitHubProject.class);

        assertNull(result.getLastActivityAt());
    }

    @Test
    void deserialize_PrivateProject_ShouldNotBePublic() throws IOException {
        String json = "{\"name\":\"private-repo\",\"private\":true,\"visibility\":\"private\"}";

        GitHubProject result = objectMapper.readValue(json, GitHubProject.class);

        assertEquals("private-repo", result.getName());
        assertFalse(result.isPublic());
    }

    @Test
    void deserialize_PublicProject_ShouldBePublic() throws IOException {
        String json = "{\"name\":\"public-repo\",\"private\":false,\"visibility\":\"public\"}";

        GitHubProject result = objectMapper.readValue(json, GitHubProject.class);

        assertEquals("public-repo", result.getName());
        assertTrue(result.isPublic());
    }
}
