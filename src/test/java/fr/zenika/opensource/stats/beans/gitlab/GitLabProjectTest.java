package fr.zenika.opensource.stats.beans.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLabProjectTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_ShouldMapLastActivityAt() throws IOException {
        String json = "{\"name\":\"test-project\",\"last_activity_at\":\"2024-05-01T12:00:00.000Z\"}";

        GitLabProject result = objectMapper.readValue(json, GitLabProject.class);

        assertEquals("test-project", result.getName());
        assertEquals("2024-05-01T12:00:00.000Z", result.getLastActivityAt());
    }

    @Test
    void deserialize_WithoutLastActivityAt_ShouldLeaveLastActivityAtNull() throws IOException {
        String json = "{\"name\":\"test-project\"}";

        GitLabProject result = objectMapper.readValue(json, GitLabProject.class);

        assertNull(result.getLastActivityAt());
    }

    @Test
    void deserialize_PrivateProject_ShouldNotBePublic() throws IOException {
        String json = "{\"name\":\"arbre-gen\",\"visibility\":\"private\"}";

        GitLabProject result = objectMapper.readValue(json, GitLabProject.class);

        assertEquals("arbre-gen", result.getName());
        assertEquals("private", result.getVisibility());
        assertFalse(result.isPublic());
    }

    @Test
    void deserialize_PublicProject_ShouldBePublic() throws IOException {
        String json = "{\"name\":\"open-source-project\",\"visibility\":\"public\"}";

        GitLabProject result = objectMapper.readValue(json, GitLabProject.class);

        assertEquals("open-source-project", result.getName());
        assertEquals("public", result.getVisibility());
        assertTrue(result.isPublic());
    }
}
