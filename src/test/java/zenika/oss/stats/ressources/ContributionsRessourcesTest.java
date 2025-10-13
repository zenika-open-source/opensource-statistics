package zenika.oss.stats.ressources;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;


@QuarkusTest
class ContributionsRessourcesTest {

    @Test
    void test_getContributionsByMember() {

        given().when()
                .get("/v1/contributions/contributions/member/id-test")
                .then()
                .statusCode(200);

    }
    
    @Test
    void test_getContributionsForAMonth() {

        given().when()
                .get("/v1/contributions/contributions/month/1")
                .then()
                .statusCode(200);

    }
}
