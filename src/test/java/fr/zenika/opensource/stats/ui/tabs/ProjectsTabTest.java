package fr.zenika.opensource.stats.ui.tabs;

import fr.zenika.opensource.stats.beans.Project;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectsTabTest {

    private final ProjectsTab projectsTab = new ProjectsTab();

    @Test
    void formatActivity_WithRecentDate_ShouldPrefixWithFlame() {
        String recent = Instant.now().minus(5, ChronoUnit.DAYS).toString();

        String result = projectsTab.formatActivity(recent);

        assertTrue(result.startsWith("🔥 "));
    }

    @Test
    void formatActivity_WithOldDate_ShouldNotPrefixWithFlame() {
        String old = Instant.now().minus(200, ChronoUnit.DAYS).toString();

        String result = projectsTab.formatActivity(old);

        assertFalse(result.startsWith("🔥"));
    }

    @Test
    void formatActivity_WithNullDate_ShouldReturnUnknown() {
        assertEquals("Unknown", projectsTab.formatActivity(null));
    }

    @Test
    void formatActivity_WithUnparseableDate_ShouldReturnUnknown() {
        assertEquals("Unknown", projectsTab.formatActivity("not-a-date"));
    }

    @Test
    void parseInstant_WithValidIsoString_ShouldReturnInstant() {
        Instant instant = projectsTab.parseInstant("2024-05-01T12:00:00Z");

        assertEquals(Instant.parse("2024-05-01T12:00:00Z"), instant);
    }

    @Test
    void parseInstant_WithBlankOrInvalid_ShouldReturnNull() {
        assertNull(projectsTab.parseInstant(null));
        assertNull(projectsTab.parseInstant(""));
        assertNull(projectsTab.parseInstant("not-a-date"));
    }

    @Test
    void sortProjects_ByActivity_ShouldOrderOldestFirstWithNullsFirst() {
        Project noActivity = projectWithActivity(null);
        Project older = projectWithActivity(Instant.now().minus(300, ChronoUnit.DAYS).toString());
        Project recent = projectWithActivity(Instant.now().minus(1, ChronoUnit.DAYS).toString());

        List<Project> projects = new ArrayList<>(List.of(recent, noActivity, older));
        projectsTab.projectSortColumn = "Activity";
        projectsTab.projectSortAscending = true;

        projectsTab.sortProjects(projects);

        assertEquals(List.of(noActivity, older, recent), projects);
    }

    @Test
    void sortProjects_ByActivityDescending_ShouldOrderMostRecentFirst() {
        Project older = projectWithActivity(Instant.now().minus(300, ChronoUnit.DAYS).toString());
        Project recent = projectWithActivity(Instant.now().minus(1, ChronoUnit.DAYS).toString());

        List<Project> projects = new ArrayList<>(List.of(older, recent));
        projectsTab.projectSortColumn = "Activity";
        projectsTab.projectSortAscending = false;

        projectsTab.sortProjects(projects);

        assertEquals(List.of(recent, older), projects);
    }

    private Project projectWithActivity(String lastActivityAt) {
        Project project = new Project();
        project.setLastActivityAt(lastActivityAt);
        return project;
    }
}
