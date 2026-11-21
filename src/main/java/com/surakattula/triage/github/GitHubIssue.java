package com.surakattula.triage.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * One GitHub issue, with only the fields we need.
 * GitHub sends many more fields; the ones not listed here are ignored.
 */
public record GitHubIssue(
        int number,
        String title,
        String body,
        String state,
        List<Label> labels,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("pull_request") Object pullRequest) {

    /**
     * GitHub's issues endpoint also returns pull requests.
     * Only pull requests carry a "pull_request" field.
     */
    public boolean isPullRequest() {
        return pullRequest != null;
    }

    public record Label(String name) {
    }
}
