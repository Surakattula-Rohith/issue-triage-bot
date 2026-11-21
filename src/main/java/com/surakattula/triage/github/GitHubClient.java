package com.surakattula.triage.github;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Reads public issues from the GitHub REST API.
 */
@Component
public class GitHubClient {

    private final RestClient restClient;

    public GitHubClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "issue-triage-bot")
                .build();
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
}
