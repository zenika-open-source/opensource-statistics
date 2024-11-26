package zenika.oss.stats.ressources;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
    
@QuarkusTest
class MembersRessourcesTest {

    @Test
    void test_getAllMembers() {

        given().when()
                .get("/v1/members/all")
                .then()
                .statusCode(200);
    }

    @Test
    void test_getMember() {

        given().when()
                .get("/v1/members/id-test")
                .then()
                .statusCode(200);
    }

    @Test
    void test_getInactivesMembers() {

        given().when()
                .get("/v1/members/inactives")
                .then()
                .statusCode(200);
    }

}
