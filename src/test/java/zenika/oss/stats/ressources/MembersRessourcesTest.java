package zenika.oss.stats.ressources;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
    
@QuarkusTest
public class MembersRessourcesTest {

    @Test
    public void test_getAllMembers() {

        given().when()
                .get("/v1/members/")
                .then()
                .statusCode(404);

    }

}
