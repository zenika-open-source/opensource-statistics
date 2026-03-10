package zenika.oss.stats.ressources.workflow;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

@QuarkusTest
public class WorkflowRessourcesTest {

    @InjectMock
    GitHubServices gitHubServices;

    @InjectMock
    FirestoreServices firestoreServices;

    @BeforeEach
    public void setup() throws Exception {
        GitHubMember member = new GitHubMember();
        member.id = "test";
        member.login = "login-test";

        List<GitHubMember> members = List.of(member);
        Mockito.when(gitHubServices.getZenikaOpenSourceMembers()).thenReturn(members);

        ZenikaMember zMember = new ZenikaMember();
        zMember.setGitHubAccount(member);
        Mockito.when(firestoreServices.getAllMembers()).thenReturn(List.of(zMember));
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
                .post("/v1/workflow/stats/save/login-test/2024")
                .then()
                .statusCode(200);
    }
}
