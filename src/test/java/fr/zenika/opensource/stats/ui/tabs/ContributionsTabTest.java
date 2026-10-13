package fr.zenika.opensource.stats.ui.tabs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContributionsTabTest {

    @Test
    void parseMonthValue_ValidNamesAndNumbers() {
        assertEquals(1, ContributionsTab.parseMonthValue("January"));
        assertEquals(3, ContributionsTab.parseMonthValue("march"));
        assertEquals(10, ContributionsTab.parseMonthValue("OCTOBER"));
        assertEquals(12, ContributionsTab.parseMonthValue("12"));
        assertEquals(5, ContributionsTab.parseMonthValue("05"));
        assertEquals(0, ContributionsTab.parseMonthValue(""));
        assertEquals(0, ContributionsTab.parseMonthValue(null));
        assertEquals(0, ContributionsTab.parseMonthValue("not-a-month"));
    }

    @Test
    void toSortKey_WithCreatedAtAndFallback() {
        assertEquals("2026-04-10T08:30:00Z",
                ContributionsTab.toSortKey("2026-04-10T08:30:00Z", "2026", "April"));

        assertEquals("2026-10-00T00:00:00Z",
                ContributionsTab.toSortKey(null, "2026", "October"));

        assertEquals("2026-03-00T00:00:00Z",
                ContributionsTab.toSortKey("", "2026", "3"));
    }

    @Test
    void formatContributionDate_IsoAndFallback() {
        assertEquals("2026-04-10",
                ContributionsTab.formatContributionDate("2026-04-10T08:30:00Z", "2026", "April"));
        assertEquals("2026-04-10",
                ContributionsTab.formatContributionDate("2026-04-10", "2026", "April"));
        assertEquals("2026 (October)",
                ContributionsTab.formatContributionDate(null, "2026", "October"));
        assertEquals("October",
                ContributionsTab.formatContributionDate("", null, "October"));
        assertEquals("2026",
                ContributionsTab.formatContributionDate(null, "2026", null));
        assertEquals("",
                ContributionsTab.formatContributionDate(null, null, null));
    }

    @Test
    void compareContributionDetailsByDateDesc_NewerFirst() {
        // Newer timestamp should sort before older timestamp (cmp < 0)
        int cmp = ContributionsTab.compareContributionDetailsByDateDesc(
                "2026-05-01T00:00:00Z", "2026", "May", "Title B",
                "2026-03-01T00:00:00Z", "2026", "March", "Title A"
        );
        assertTrue(cmp < 0, "Newer date (May) must sort before older date (March)");

        // Older timestamp should sort after newer timestamp (cmp > 0)
        int cmpReverse = ContributionsTab.compareContributionDetailsByDateDesc(
                "2026-03-01T00:00:00Z", "2026", "March", "Title A",
                "2026-05-01T00:00:00Z", "2026", "May", "Title B"
        );
        assertTrue(cmpReverse > 0, "Older date (March) must sort after newer date (May)");

        // Same date tie-breaker by title
        int cmpSameDate = ContributionsTab.compareContributionDetailsByDateDesc(
                "2026-05-01T00:00:00Z", "2026", "May", "Alpha PR",
                "2026-05-01T00:00:00Z", "2026", "May", "Beta PR"
        );
        assertTrue(cmpSameDate < 0, "Same date should sort alphabetically by title");

        // Fallback when createdAt is empty: October should come before March
        int cmpFallback = ContributionsTab.compareContributionDetailsByDateDesc(
                "", "2026", "October", "Fall PR",
                "", "2026", "March", "Spring PR"
        );
        assertTrue(cmpFallback < 0, "October should sort before March");
    }
}
