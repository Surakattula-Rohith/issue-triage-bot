package com.surakattula.triage.github;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Reads public issues from the GitHub REST API.
 */
@Component
public class GitHubClient {

    private final RestClient restClient;

    /**
     * @param token a GitHub personal access token, or empty to call GitHub without one
     *              (60 requests per hour without a token, 5,000 with one)
     */
    public GitHubClient(@Value("${GITHUB_TOKEN:}") String token) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "issue-triage-bot");
        if (!token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.restClient = builder.build();
    }

    public GitHubIssue getIssue(String owner, String repo, int number) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/issues/{number}", owner, repo, number)
                .retrieve()
                .body(GitHubIssue.class);
    }

    /**
     * One page of a repo's issues (and pull requests), newest first.
     * GitHub returns at most 100 items per page.
     */
    public List<GitHubIssue> listIssues(String owner, String repo, int page) {
        GitHubIssue[] items = restClient.get()
                .uri("/repos/{owner}/{repo}/issues?state=all&per_page=100&page={page}", owner, repo, page)
                .retrieve()
                .body(GitHubIssue[].class);
        return List.of(items);
    }

    /**
     * How many GitHub requests are left this hour, e.g. "4997 of 5000".
     * Asking costs nothing: calls to /rate_limit don't count against the limit.
     */
    public String requestsLeft() {
        HttpHeaders headers = restClient.get()
                .uri("/rate_limit")
                .retrieve()
                .toBodilessEntity()
                .getHeaders();
        return headers.getFirst("X-RateLimit-Remaining") + " of " + headers.getFirst("X-RateLimit-Limit");
    }
}
