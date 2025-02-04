package zenika.oss.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.junit.jupiter.api.Test;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ZenikaMemberMapperTest {

    @Test
    void test_mapGitHubMemberToZenikaMember_ShouldCreateZenikaMemberWithGitHubAccount() {
        GitHubMember gitHubMember = new GitHubMember();
        gitHubMember.setLogin("testUser");

        ZenikaMember result = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);

        assertNotNull(result, "Mapped ZenikaMember should not be null");
        assertNotNull(result.getId(), "ZenikaMember ID should not be null");
        assertEquals(gitHubMember, result.getGitHubAccount(), "GitHub account should match input");
    }

    @Test
    void test_mapGitHubMemberToZenikaMember_WithNullInput_ShouldCreateZenikaMemberWithNullGitHubAccount() {
        ZenikaMember result = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(null);

        assertNotNull(result, "Mapped ZenikaMember should not be null");
        assertNotNull(result.getId(), "ZenikaMember ID should not be null");
        assertNull(result.getGitHubAccount(), "GitHub account should be null");
    }

    @Test
    void test_mapGitHubMemberToZenikaMember_ShouldGenerateUniqueIds() {
        GitHubMember gitHubMember = new GitHubMember();

        ZenikaMember result1 = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);
        ZenikaMember result2 = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);
        assertNotEquals(result1.getId(), result2.getId(), "Generated IDs should be unique");
    }

    @Test
    void mapFirestoreZenikaMemberToZenikaMember_ShouldCallGettersOnce() {
        // Arrange
        QueryDocumentSnapshot queryDocumentSnapshot = mock(QueryDocumentSnapshot.class);
        when(queryDocumentSnapshot.getString("firstname")).thenReturn("my firstname");
        when(queryDocumentSnapshot.getString("name")).thenReturn("my name");
        when(queryDocumentSnapshot.getId()).thenReturn("1223123123123");
        when(queryDocumentSnapshot.get(anyString(), any())).thenReturn(null);

        // Act
        ZenikaMember zenikaMember = ZenikaMemberMapper.mapFirestoreZenikaMemberToZenikaMember(queryDocumentSnapshot);

        // Assert
        verify(queryDocumentSnapshot, times(1)).getString("firstname");
        verify(queryDocumentSnapshot, times(1)).getString("name");
        verify(queryDocumentSnapshot, times(1)).getId();
        verify(queryDocumentSnapshot, times(1)).get("gitHubAccount", GitHubMember.class);

        assertNotNull(zenikaMember);
        assertEquals("my firstname", zenikaMember.getFirstname());
        assertEquals("my name", zenikaMember.getName());
    }
}
