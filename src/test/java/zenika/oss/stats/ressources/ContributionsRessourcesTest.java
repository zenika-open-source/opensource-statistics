package zenika.oss.stats.ressources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.services.FirestoreServices;

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
        StatsContribution fakeContribution = new StatsContribution();
        fakeContribution.setGithubHandle(memberId);
        fakeContribution.setYear("2024");
        fakeContribution.setMonth("October");
        fakeContribution.setNumberOfContributionsOnGitHub(100);

        // When firestoreServices.getContributionsForAMemberOrderByYear is called with "test-member", return our fake data
        Mockito.when(firestoreServices.getContributionsForAMemberOrderByYear(memberId))
                .thenReturn(List.of(fakeContribution));

        // WHEN: We call the REST endpoint
        given()
                .when().get("/v1/contributions/member/" + memberId) // Assuming this is the correct endpoint path
                .then()
                .statusCode(200)
                .body("size()", is(1)) // Assert the list contains one item
                .body("[0].githubHandle", is(memberId))
                .body("[0].numberOfContributionsOnGitHub", is(100));

        // Verify that the service method was called exactly once
        Mockito.verify(firestoreServices, Mockito.times(1)).getContributionsForAMemberOrderByYear(memberId);
    }
}