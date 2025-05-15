package zenika.oss.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zenika.oss.stats.beans.github.GitHubProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ZenikaProjectMapperTest {

    @Test
    void mapFirestoreZenikaProjectToGitHubProject_ShouldMapAllFields() {
        // Arrange
        QueryDocumentSnapshot mockDocument = Mockito.mock(QueryDocumentSnapshot.class);

        // Setup mock document to return expected values
        when(mockDocument.getBoolean("archived")).thenReturn(true);
        when(mockDocument.getBoolean("fork")).thenReturn(false);
        when(mockDocument.getString("id")).thenReturn("12345");
        when(mockDocument.getString("name")).thenReturn("test-project");
        when(mockDocument.getString("full_name")).thenReturn("zenika/test-project");
        when(mockDocument.getString("html_url")).thenReturn("https://github.com/zenika/test-project");
        when(mockDocument.getString("visibility")).thenReturn("public");
        when(mockDocument.getLong("watchers_count")).thenReturn(42L);

        // Act
        GitHubProject result = ZenikaProjectMapper.mapFirestoreZenikaProjectToGitHubProject(mockDocument);

        // Assert
        assertNotNull(result);
        assertTrue(result.isArchived());
        assertFalse(result.isFork());
        assertEquals("12345", result.getId());
        assertEquals("test-project", result.getName());
        assertEquals("zenika/test-project", result.getFull_name());
        assertEquals("https://github.com/zenika/test-project", result.getHtml_url());
        assertEquals("public", result.getVisibility());
        assertEquals(42L, result.getWatchers_count());
    }

    @Test
    void mapFirestoreZenikaProjectToGitHubProject_WithNullValues_ShouldHandleGracefully() {
        // Arrange
        QueryDocumentSnapshot mockDocument = Mockito.mock(QueryDocumentSnapshot.class);

        // Setup mock document to return null for string values
        when(mockDocument.getBoolean("archived")).thenReturn(false);
        when(mockDocument.getBoolean("fork")).thenReturn(false);
        when(mockDocument.getString("id")).thenReturn(null);
        when(mockDocument.getString("name")).thenReturn(null);
        when(mockDocument.getString("full_name")).thenReturn(null);
        when(mockDocument.getString("html_url")).thenReturn(null);
        when(mockDocument.getString("visibility")).thenReturn(null);
        when(mockDocument.getLong("watchers_count")).thenReturn(0L);

        // Act
        GitHubProject result = ZenikaProjectMapper.mapFirestoreZenikaProjectToGitHubProject(mockDocument);

        // Assert
        assertNotNull(result);
        assertFalse(result.isArchived());
        assertFalse(result.isFork());
        assertNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getFull_name());
        assertNull(result.getHtml_url());
        assertNull(result.getVisibility());
        assertEquals(0L, result.getWatchers_count());
    }

    @Test
    void mapFirestoreZenikaProjectToGitHubProject_WithMissingFields_ShouldThrowException() {
        // Arrange
        QueryDocumentSnapshot mockDocument = Mockito.mock(QueryDocumentSnapshot.class);

        // Setup mock to throw exception when field is missing
        when(mockDocument.getBoolean("archived")).thenThrow(new IllegalStateException("No field 'archived'"));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ZenikaProjectMapper.mapFirestoreZenikaProjectToGitHubProject(mockDocument);
        });

        assertTrue(exception.getMessage().contains("No field 'archived'"));
    }
}