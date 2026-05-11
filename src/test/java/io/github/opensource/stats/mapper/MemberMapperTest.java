package io.github.opensource.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.junit.jupiter.api.Test;
import io.github.opensource.stats.beans.Member;
import io.github.opensource.stats.beans.github.GitHubMember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MemberMapperTest {

    @Test
    void test_mapGitHubMemberToMember_ShouldCreateMemberWithGitHubAccount() {
        GitHubMember gitHubMember = new GitHubMember();
        gitHubMember.setLogin("testUser");

        Member result = MemberMapper.mapGitHubMemberToMember(gitHubMember);

        assertNotNull(result, "Mapped Member should not be null");
        assertNotNull(result.getId(), "Member ID should not be null");
        assertEquals(gitHubMember, result.getGitHubAccount(), "GitHub account should match input");
    }

    @Test
    void test_mapGitHubMemberToMember_WithNullInput_ShouldCreateMemberWithNullGitHubAccount() {
        Member result = MemberMapper.mapGitHubMemberToMember(null);

        assertNotNull(result, "Mapped Member should not be null");
        assertNotNull(result.getId(), "Member ID should not be null");
        assertNull(result.getGitHubAccount(), "GitHub account should be null");
    }

    @Test
    void test_mapGitHubMemberToMember_ShouldGenerateUniqueIds() {
        GitHubMember gitHubMember = new GitHubMember();

        Member result1 = MemberMapper.mapGitHubMemberToMember(gitHubMember);
        Member result2 = MemberMapper.mapGitHubMemberToMember(gitHubMember);
        assertNotEquals(result1.getId(), result2.getId(), "Generated IDs should be unique");
    }

    @Test
    void mapFirestoreMemberToMember_ShouldCallGettersOnce() {
        // Arrange
        QueryDocumentSnapshot queryDocumentSnapshot = mock(QueryDocumentSnapshot.class);
        when(queryDocumentSnapshot.getString("firstname")).thenReturn("my firstname");
        when(queryDocumentSnapshot.getString("name")).thenReturn("my name");
        when(queryDocumentSnapshot.getId()).thenReturn("1223123123123");
        when(queryDocumentSnapshot.get(anyString(), any())).thenReturn(null);

        // Act
        Member member = MemberMapper.mapFirestoreMemberToMember(queryDocumentSnapshot);

        // Assert
        verify(queryDocumentSnapshot, times(1)).getString("firstname");
        verify(queryDocumentSnapshot, times(1)).getString("name");
        verify(queryDocumentSnapshot, times(1)).getId();
        verify(queryDocumentSnapshot, times(1)).get("gitHubAccount", GitHubMember.class);

        assertNotNull(member);
        assertEquals("my firstname", member.getFirstname());
        assertEquals("my name", member.getName());
    }
}
