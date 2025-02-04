package zenika.oss.stats.mapper;

import org.junit.jupiter.api.Test;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;

import static org.junit.jupiter.api.Assertions.*;

public class ZenikaMemberMapperTest {
    @Test
    void mapGitHubMemberToZenikaMember_ShouldCreateZenikaMemberWithGitHubAccount() {
        // Arrange
        GitHubMember gitHubMember = new GitHubMember();
        gitHubMember.setLogin("testUser");

        // Act
        ZenikaMember result = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);

        // Assert
        assertNotNull(result, "Mapped ZenikaMember should not be null");
        assertNotNull(result.getId(), "ZenikaMember ID should not be null");
        assertEquals(gitHubMember, result.getGitHubAccount(), "GitHub account should match input");
    }

    @Test
    void mapGitHubMemberToZenikaMember_WithNullInput_ShouldCreateZenikaMemberWithNullGitHubAccount() {
        // Act
        ZenikaMember result = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(null);

        // Assert
        assertNotNull(result, "Mapped ZenikaMember should not be null");
        assertNotNull(result.getId(), "ZenikaMember ID should not be null");
        assertNull(result.getGitHubAccount(), "GitHub account should be null");
    }

    @Test
    void mapGitHubMemberToZenikaMember_ShouldGenerateUniqueIds() {
        // Arrange
        GitHubMember gitHubMember = new GitHubMember();

        // Act
        ZenikaMember result1 = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);
        ZenikaMember result2 = ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember);

        // Assert
        assertNotEquals(result1.getId(), result2.getId(), "Generated IDs should be unique");
    }
}
