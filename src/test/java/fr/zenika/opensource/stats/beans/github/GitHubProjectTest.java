package fr.zenika.opensource.stats.beans.github;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}
