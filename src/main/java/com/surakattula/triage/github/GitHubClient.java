package com.surakattula.triage.github;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Reads public issues from the GitHub REST API.
 */
@Component
public class GitHubClient {

    private static final String BASE_URL = "https://api.github.com";

    private final RestClient restClient;

    /**
     * @param builder in the app, Spring's pre-configured builder; in tests, one wired to a fake GitHub
     * @param token   a GitHub personal access token, or empty to call GitHub without one
     *                (60 requests per hour without a token, 5,000 with one)
     */
    public GitHubClient(RestClient.Builder builder, @Value("${GITHUB_TOKEN:}") String token) {
        builder.baseUrl(BASE_URL)
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
     * Hands every page of a repo's issues and pull requests (open and closed, newest first)
     * to {@code handlePage}, 100 items at a time, until there are no more.
     * Paging is hidden in here: callers never see page numbers or cursors.
     */
    public void forEachPage(String owner, String repo, Consumer<List<GitHubIssue>> handlePage) {
        URI next = UriComponentsBuilder
                .fromUriString(BASE_URL + "/repos/{owner}/{repo}/issues?state=all&per_page=100")
                .build(owner, repo);
        while (next != null) { // null means GitHub sent no "next" link: that was the last page
            // A URI object is used as-is. A String would be encoded again and break the cursor.
            ResponseEntity<GitHubIssue[]> response = restClient.get()
                    .uri(next)
                    .retrieve()
                    .toEntity(GitHubIssue[].class);
            handlePage.accept(List.of(response.getBody()));
            next = nextLink(response.getHeaders());
        }
    }

    /**
     * Finds the "next" address in GitHub's Link header, for example:
     * {@code <https://api.github.com/...&after=Y3Vy...>; rel="next", <...>; rel="first"}.
     * Returns null when there is no "next" link, which means this was the last page.
     */
    static URI nextLink(HttpHeaders headers) {
        String link = headers.getFirst(HttpHeaders.LINK);
        if (link == null) {
            return null;
        }
        for (String part : link.split(",")) {
            if (part.contains("rel=\"next\"")) {
                String url = part.substring(part.indexOf('<') + 1, part.indexOf('>'));
                return URI.create(url);
            }
        }
        return null;
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
