package fr.zenika.opensource.stats.services;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.ExternalContribution;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.Project;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.exception.DatabaseException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ExternalContributionServiceTest {

    @Inject
    ExternalContributionService externalContributionService;

    @InjectMock
    FirestoreServices firestoreServices;

    @Test
    void test_isExternalProject_detection() {
        Set<String> orgPrefixes = Set.of("zenika", "zenika-open-source");
        Set<String> memberLogins = Set.of("johndoe", "alice");
        Set<String> knownInternalFullNames = Set.of("zenika/internal-tool", "johndoe/my-side-project");
        Set<String> knownInternalUrls = Set.of("https://github.com/zenika/internal-tool");

        // Org project
        assertFalse(externalContributionService.isExternalProject(
                "zenika-open-source/opensource-statistics",
                "https://github.com/zenika-open-source/opensource-statistics",
                "https://github.com/zenika-open-source/opensource-statistics/pull/1",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls));

        // Member personal project
        assertFalse(externalContributionService.isExternalProject(
                "johndoe/my-side-project",
                "https://github.com/johndoe/my-side-project",
                "https://github.com/johndoe/my-side-project/pull/5",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls));

        // Internal project in known list
        assertFalse(externalContributionService.isExternalProject(
                "zenika/internal-tool",
                "https://github.com/zenika/internal-tool",
                "",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls));

        // External project (Quarkus)
        assertTrue(externalContributionService.isExternalProject(
                "quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus/pull/42",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls));

        // External project (Spring)
        assertTrue(externalContributionService.isExternalProject(
                "spring-projects/spring-boot",
                "https://github.com/spring-projects/spring-boot",
                "https://github.com/spring-projects/spring-boot/pull/123",
                orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls));
    }

    @Test
    void test_extractProjectFullNameFromPrUrl() {
        assertEquals("quarkusio/quarkus", externalContributionService.extractProjectFullNameFromPrUrl(
                "https://github.com/quarkusio/quarkus/pull/123").orElse(""));

        assertEquals("inkscape/inkscape", externalContributionService.extractProjectFullNameFromPrUrl(
                "https://gitlab.com/inkscape/inkscape/-/merge_requests/456").orElse(""));

        assertTrue(externalContributionService.extractProjectFullNameFromPrUrl("").isEmpty());
    }

    @Test
    void test_buildSocialPostSnippet() {
        ExternalContribution contrib = new ExternalContribution(
                "m1", "John Doe", "johndoe",
                "quarkus", "quarkusio/quarkus", "https://github.com/quarkusio/quarkus",
                "Fix NPE in OIDC provider", "https://github.com/quarkusio/quarkus/pull/123",
                "MERGED", "2026-03-15T10:00:00Z", "GitHub", "2026", "MARCH");

        String snippet = externalContributionService.buildSocialPostSnippet(contrib);

        assertNotNull(snippet);
        assertTrue(snippet.contains("John Doe"));
        assertTrue(snippet.contains("quarkusio/quarkus"));
        assertTrue(snippet.contains("Fix NPE in OIDC provider"));
        assertTrue(snippet.contains("https://github.com/quarkusio/quarkus/pull/123"));
        assertTrue(snippet.contains("#OpenSource"));
        assertTrue(snippet.contains("#quarkus"));
    }

    @Test
    void test_getExternalContributions_filtersAndSorts() throws DatabaseException {
        Member m = new Member();
        m.setId("m1");
        m.setName("Doe");
        m.setFirstname("John");
        GitHubMember ghm = new GitHubMember();
        ghm.setLogin("johndoe");
        m.setGitHubAccount(ghm);

        Mockito.when(firestoreServices.getAllMembers()).thenReturn(List.of(m));
        Mockito.when(firestoreServices.getAllProjects()).thenReturn(List.of());

        ContributionDetail internalDetail = new ContributionDetail(
                "opensource-statistics", "zenika-open-source/opensource-statistics",
                "https://github.com/zenika-open-source/opensource-statistics",
                "Internal PR", "https://github.com/zenika-open-source/opensource-statistics/pull/1",
                "MERGED", "2026-01-01T00:00:00Z");

        ContributionDetail externalMergedOlder = new ContributionDetail(
                "quarkus", "quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus",
                "Old PR", "https://github.com/quarkusio/quarkus/pull/10",
                "MERGED", "2026-02-01T00:00:00Z");

        ContributionDetail externalOpenNewer = new ContributionDetail(
                "vue", "vuejs/core",
                "https://github.com/vuejs/core",
                "New PR", "https://github.com/vuejs/core/pull/20",
                "OPEN", "2026-03-01T00:00:00Z");

        StatsContribution stat = new StatsContribution();
        stat.setIdMember("m1");
        stat.setGithubHandle("johndoe");
        stat.setYear("2026");
        stat.setMonth("MARCH");
        stat.setSource("GitHub");
        stat.setContributionsDetails(List.of(internalDetail, externalMergedOlder, externalOpenNewer));

        Mockito.when(firestoreServices.getStatsForYear(2026)).thenReturn(List.of(stat));

        // 1. Fetch all external contributions
        List<ExternalContribution> all = externalContributionService.getExternalContributions(2026, null, null, null);
        assertEquals(2, all.size());
        // Check sorting: newer first
        assertEquals("vuejs/core", all.get(0).getProjectFullName());
        assertEquals("quarkusio/quarkus", all.get(1).getProjectFullName());

        // 2. Filter by MERGED only
        List<ExternalContribution> mergedOnly = externalContributionService.getExternalContributions(2026, "MERGED", null, null);
        assertEquals(1, mergedOnly.size());
        assertEquals("quarkusio/quarkus", mergedOnly.get(0).getProjectFullName());

        // 3. Limit to 1
        List<ExternalContribution> limited = externalContributionService.getExternalContributions(2026, null, null, 1);
        assertEquals(1, limited.size());
        assertEquals("vuejs/core", limited.get(0).getProjectFullName());
    }
}
