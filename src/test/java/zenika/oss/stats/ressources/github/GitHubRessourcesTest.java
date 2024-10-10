package zenika.oss.stats.ressources.github;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import zenika.oss.stats.beans.GitHubMember;
import zenika.oss.stats.services.GitHubServices;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class GitHubRessourcesTest {

    private GitHubMember member;

    @InjectMock
    GitHubServices gitHubServices;

    @BeforeEach
    public void setup() {

        member = new GitHubMember();
        member.id = "test";
        member.login = "login-test";

        List<GitHubMember> members = Arrays.asList(member);

        Mockito.when(gitHubServices.getOrganizationInformation("test"))
                .thenReturn(null);
        Mockito.when(gitHubServices.getUserInformation("test"))
                .thenReturn(null);
        Mockito.when(gitHubServices.getPersonalProjectForAnUser("test"))
                .thenReturn(null);
        Mockito.when(gitHubServices.getForkedProjectForAnUser("test"))
                .thenReturn(null);

        Mockito.when(gitHubServices.getOrganizationMembers("test"))
                .thenReturn(members);
    }

    @Test
    public void test_getOrganizationInformation() {

        given().when()
                .get("/v1/github/organization/infos/")
                .then()
                .statusCode(200);
    }

    @Test
    public void test_getOrganizationMembers() {

        given().when()
                .get("/v1/github/organization/members/")
                .then()
                .statusCode(200);
    }

    @Test
    public void test_getUserInformation() {

        given().when()
                .get("/v1/github/user/" + member.id)
                .then()
                .statusCode(200);

    }

    @Test
    public void test_getPersonalProjectForAnUser() {

        given().when()
                .get("/v1/github/user/" + member.login + "/personal-projects")
                .then()
                .statusCode(200);

    }

    @Test
    public void test_getForkedProjectsForAnUser() {

        given().when()
                .get("/v1/github/user/" + member.login + "/forked-projects")
                .then()
                .statusCode(200);

    }

}
