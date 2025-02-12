package zenika.oss.stats.ressources.workflow;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class WorkflowRessourcesTest {

    @InjectMock
    GitHubServices gitHubServices;

    @BeforeEach
    public void setup() {
        GitHubMember member = new GitHubMember();
        member.id = "test";
        member.login = "login-test";

        List<GitHubMember> members = List.of(member);

        Mockito.when(gitHubServices.getZenikaOpenSourceMembers()).thenReturn(members);
    }

    @Test
    void test_saveMembers() {

        given().when()
                .post("/v1/workflow/members/save")
                .then()
                .statusCode(200);
    }

    @Test
    void test_saveForkedProject() {

        given().when()
                .post("/v1/workflow/forked-projects/save")
                .then()
                .statusCode(200);
    }

    @Test
    void test_savePersonalProjects() {

        given().when()
                .post("/v1/workflow/personal-projects/save")
                .then()
                .statusCode(200);
    }

    @Test
    void test_saveStatsForYear() {

        given().when()
                .post("/v1/workflow/stats/save/2024")
                .then()
                .statusCode(200);
    }

    @Test
    void test_saveStatsForAGitHubAccountForAYear() {

        given().when()
                .post("/v1/workflow/stats/save/my-user/2024")
                .then()
                .statusCode(200);
    }
}
