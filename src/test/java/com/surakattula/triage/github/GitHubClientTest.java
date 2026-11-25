package com.surakattula.triage.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GitHubClientTest {

    @Test
    void getIssueReadsTheFieldsWeNeed() {
        // Arrange: a fake GitHub that answers one expected request with a fixed reply
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer fakeGitHub = MockRestServiceServer.bindTo(builder).build();
        GitHubClient client = new GitHubClient(builder, "");

        fakeGitHub.expect(requestTo("https://api.github.com/repos/spring-projects/spring-boot/issues/1"))
                .andRespond(withSuccess("""
                        {
                          "number": 1,
                          "title": "App fails to start",
                          "state": "open",
                          "labels": [{"name": "type: bug", "color": "d73a4a"}],
                          "created_at": "2026-11-11T15:23:34Z",
                          "html_url": "https://github.com/spring-projects/spring-boot/issues/1",
                          "comments": 3
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        GitHubIssue issue = client.getIssue("spring-projects", "spring-boot", 1);

        // Assert
        assertThat(issue.title()).isEqualTo("App fails to start");
        assertThat(issue.labels()).extracting(GitHubIssue.Label::name).containsExactly("type: bug");
        assertThat(issue.createdAt()).isEqualTo(Instant.parse("2026-11-11T15:23:34Z"));
        assertThat(issue.isPullRequest()).isFalse();
        fakeGitHub.verify();
    }

    @Test
    void nextLinkFindsTheNextPageAddress() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK,
                "<https://api.github.com/repositories/6296790/issues?per_page=100&page=2&after=Y3Vy%3D>; rel=\"next\", "
                        + "<https://api.github.com/repositories/6296790/issues?per_page=100&before=Y3Vy%3D>; rel=\"prev\"");

        assertThat(GitHubClient.nextLink(headers)).hasToString(
                "https://api.github.com/repositories/6296790/issues?per_page=100&page=2&after=Y3Vy%3D");
    }

    @Test
    void nextLinkIsNullOnTheLastPage() {
        HttpHeaders lastPage = new HttpHeaders();
        lastPage.add(HttpHeaders.LINK,
                "<https://api.github.com/repositories/6296790/issues?per_page=100&before=Y3Vy%3D>; rel=\"prev\"");

        assertThat(GitHubClient.nextLink(lastPage)).isNull();
        assertThat(GitHubClient.nextLink(new HttpHeaders())).isNull(); // no Link header at all
    }
}
