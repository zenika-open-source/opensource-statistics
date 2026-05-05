package zenika.oss.stats.ressources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zenika.oss.stats.services.FirestoreServices;

import java.util.Collections;

import static io.restassured.RestAssured.given;
    
@QuarkusTest
class MembersRessourcesTest {

    @InjectMock
    FirestoreServices firestoreServices;

    @Test
    void test_getAllMembers() throws Exception {
        Mockito.when(firestoreServices.getAllMembers()).thenReturn(Collections.emptyList());

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

}
