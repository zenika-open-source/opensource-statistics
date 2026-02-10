package zenika.oss.stats.ressources.github;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.services.GitHubServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
class GitHubRessourcesTest {

        private static final Logger log = LoggerFactory.getLogger(GitHubRessourcesTest.class);

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
        void test_getOrganizationInformation() {

                given().when()
                                .get("/v1/github/organization/infos/")
                                .then()
                                .statusCode(200);
        }

        @Test
        void test_getOrganizationMembers() {

                given().when()
                                .get("/v1/github/organization/members/")
                                .then()
                                .statusCode(200);
        }

        @Test
        void test_getUserInformation() {

                given().when()
                                .get("/v1/github/user/" + member.id)
                                .then()
                                .statusCode(200);

        }

        @Test
        void test_getPersonalProjectForAnUser() {

                given().when()
                                .get("/v1/github/user/" + member.login + "/personal-projects")
                                .then()
                                .statusCode(200);

        }

        @Test
        void test_getForkedProjectsForAnUser() {

                given().when()
                                .get("/v1/github/user/" + member.login + "/forked-projects")
                                .then()
                                .statusCode(200);

        }

        @Test
        void test_getContributionsDataWithLoginAndCurrentyear() {

                Mockito.when(gitHubServices.getContributionsForTheCurrentYear("test", 2024))
                                .thenReturn(List.of(new CustomStatsContributionsUserByMonth(1, "JANUARY", 10)));

                given().when()
                                .get("/v1/github/user/" + member.login + "/contributions/year/current")
                                .then()
                                .statusCode(200);

        }

        @Test
        void test_getContributionsDataWithLoginAndYear() {

                Mockito.when(gitHubServices.getContributionsForTheCurrentYear("test", 2024))
                                .thenReturn(List.of(new CustomStatsContributionsUserByMonth(1, "JANUARY", 10)));

                given().when()
                                .get("/v1/github/user/" + member.login + "/contributions/year/" + 2024)
                                .then()
                                .statusCode(200);

        }

        @Test
        void test_getContributionsForAnOrganizationAndForAllMembersAndTheCurrentYear() {

                Mockito.when(gitHubServices.getContributionsForTheCurrentYearAndAllTheOrganizationMembers(any()))
                                .thenReturn(new ArrayList());

                given().when()
                                .get("/v1/github/organization/members/contributions/year/current")
                                .then()
                                .statusCode(200);
        }
}
