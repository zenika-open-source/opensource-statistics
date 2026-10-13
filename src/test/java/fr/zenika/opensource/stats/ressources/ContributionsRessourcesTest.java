package fr.zenika.opensource.stats.ressources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.services.FirestoreServices;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ContributionsRessourcesTest {

    @InjectMock
    FirestoreServices firestoreServices;

    @Test
    void test_getContributionsByMember() throws DatabaseException {
        // GIVEN: We define the behavior of our mocked service
        String memberId = "test-member";
        ContributionDetail detail = new ContributionDetail(
                "opensource-statistics",
                "zenika-open-source/opensource-statistics",
                "https://github.com/zenika-open-source/opensource-statistics",
                "feat: add PR details",
                "https://github.com/zenika-open-source/opensource-statistics/pull/42",
                "MERGED",
                "2024-10-10T10:00:00Z");

        StatsContribution fakeContribution = new StatsContribution();
        fakeContribution.setGithubHandle(memberId);
        fakeContribution.setYear("2024");
        fakeContribution.setMonth("October");
        fakeContribution.setNumberOfContributionsOnGitHub(100);
        fakeContribution.setContributionsDetails(List.of(detail));

        // When firestoreServices.getContributionsForAMemberOrderByYear is called with
        // "test-member", return our fake data
        Mockito.when(firestoreServices.getContributionsForAMemberOrderByYear(memberId))
                .thenReturn(List.of(fakeContribution));

        // WHEN: We call the REST endpoint
        given()
                .when().get("/v1/contributions/member/" + memberId) // Assuming this is the correct endpoint path
                .then()
                .statusCode(200)
                .body("size()", is(1)) // Assert the list contains one item
                .body("[0].githubHandle", is(memberId))
                .body("[0].numberOfContributionsOnGitHub", is(100))
                .body("[0].contributionsDetails.size()", is(1))
                .body("[0].contributionsDetails[0].projectFullName", is("zenika-open-source/opensource-statistics"))
                .body("[0].contributionsDetails[0].prUrl",
                        is("https://github.com/zenika-open-source/opensource-statistics/pull/42"));

        // Verify that the service method was called exactly once
        Mockito.verify(firestoreServices, Mockito.times(1)).getContributionsForAMemberOrderByYear(memberId);
    }

    @Test
    void test_getContributionsDetailsByYear() throws DatabaseException {
        ContributionDetail detail = new ContributionDetail(
                "quarkus",
                "quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus",
                "fix: issue",
                "https://github.com/quarkusio/quarkus/pull/99",
                "MERGED",
                "2026-05-01T12:00:00Z");

        StatsContribution fakeContribution = new StatsContribution();
        fakeContribution.setYear("2026");
        fakeContribution.setMonth("May");
        fakeContribution.setContributionsDetails(List.of(detail));

        Mockito.when(firestoreServices.getStatsForYear(2026))
                .thenReturn(List.of(fakeContribution));

        given()
                .queryParam("year", 2026)
                .when().get("/v1/contributions/details")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].projectFullName", is("quarkusio/quarkus"))
                .body("[0].prTitle", is("fix: issue"))
                .body("[0].prUrl", is("https://github.com/quarkusio/quarkus/pull/99"));

        Mockito.verify(firestoreServices, Mockito.times(1)).getStatsForYear(2026);
    }

    @Test
    void test_getExternalContributions() throws DatabaseException {
        ContributionDetail detail = new ContributionDetail(
                "quarkus",
                "quarkusio/quarkus",
                "https://github.com/quarkusio/quarkus",
                "feat: add feature",
                "https://github.com/quarkusio/quarkus/pull/101",
                "MERGED",
                "2026-06-01T12:00:00Z");

        StatsContribution fakeContribution = new StatsContribution();
        fakeContribution.setIdMember("m-external");
        fakeContribution.setGithubHandle("johndoe");
        fakeContribution.setYear("2026");
        fakeContribution.setMonth("June");
        fakeContribution.setSource("GitHub");
        fakeContribution.setContributionsDetails(List.of(detail));

        Mockito.when(firestoreServices.getAllMembers()).thenReturn(List.of());
        Mockito.when(firestoreServices.getAllProjects()).thenReturn(List.of());
        Mockito.when(firestoreServices.getStatsForYear(2026)).thenReturn(List.of(fakeContribution));

        given()
                .queryParam("year", 2026)
                .queryParam("state", "MERGED")
                .when().get("/v1/contributions/external")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].projectFullName", is("quarkusio/quarkus"))
                .body("[0].prTitle", is("feat: add feature"))
                .body("[0].prState", is("MERGED"))
                .body("[0].prUrl", is("https://github.com/quarkusio/quarkus/pull/101"));
    }
}