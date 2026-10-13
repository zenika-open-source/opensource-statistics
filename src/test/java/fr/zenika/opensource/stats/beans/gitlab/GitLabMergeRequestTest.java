package fr.zenika.opensource.stats.beans.gitlab;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLabMergeRequestTest {

    @Test
    void shouldExtractProjectAndMrDetailsFromWebUrlAndReferences() {
        GitLabMergeRequest mr = new GitLabMergeRequest();
        mr.setTitle("feat: add gitlab mr tracking");
        mr.setState("merged");
        mr.setWebUrl("https://gitlab.com/zenika/open-source/my-project/-/merge_requests/42");
        mr.setCreatedAt("2026-04-10T08:30:00Z");

        GitLabMergeRequest.GitLabReferences refs = new GitLabMergeRequest.GitLabReferences();
        refs.setFull("zenika/open-source/my-project!42");
        mr.setReferences(refs);

        assertTrue(mr.extractProjectUrl().isPresent());
        assertEquals("https://gitlab.com/zenika/open-source/my-project", mr.extractProjectUrl().orElse(""));
        assertEquals("zenika/open-source/my-project", mr.extractProjectFullName().orElse(""));
        assertEquals("my-project", mr.extractProjectName().orElse(""));

        ContributionDetail detail = mr.toContributionDetail();
        assertEquals("my-project", detail.getProjectName());
        assertEquals("zenika/open-source/my-project", detail.getProjectFullName());
        assertEquals("https://gitlab.com/zenika/open-source/my-project", detail.getProjectUrl());
        assertEquals("feat: add gitlab mr tracking", detail.getPrTitle());
        assertEquals("https://gitlab.com/zenika/open-source/my-project/-/merge_requests/42", detail.getPrUrl());
        assertEquals("merged", detail.getPrState());
        assertEquals("2026-04-10T08:30:00Z", detail.getCreatedAt());
    }

    @Test
    void shouldFallbackToWebUrlPathWhenReferencesAreMissing() {
        GitLabMergeRequest mr = new GitLabMergeRequest();
        mr.setTitle("fix: fallback path");
        mr.setState("merged");
        mr.setWebUrl("https://gitlab.com/group/sub/repo/-/merge_requests/7");

        ContributionDetail detail = mr.toContributionDetail();
        assertEquals("repo", detail.getProjectName());
        assertEquals("group/sub/repo", detail.getProjectFullName());
        assertEquals("https://gitlab.com/group/sub/repo", detail.getProjectUrl());
        assertEquals("https://gitlab.com/group/sub/repo/-/merge_requests/7", detail.getPrUrl());
    }
}
