package com.surakattula.triage.storage;

import com.surakattula.triage.github.GitHubIssue;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Saves issues into the "issues" table and reads them back.
 */
@Repository
public class IssueRepository {

    private final JdbcClient jdbc;

    public IssueRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Insert the issue, or update it if we already have that number.
     */
    public void save(GitHubIssue issue) {
        jdbc.sql("""
                        INSERT INTO issues (number, title, body, state, labels, created_at, html_url)
                        VALUES (:number, :title, :body, :state, :labels, :createdAt, :htmlUrl)
                        ON CONFLICT (number) DO UPDATE SET
                            title    = EXCLUDED.title,
                            body     = EXCLUDED.body,
                            state    = EXCLUDED.state,
                            labels   = EXCLUDED.labels,
                            html_url = EXCLUDED.html_url
                        """)
                .param("number", issue.number())
                .param("title", issue.title())
                .param("body", issue.body())
                .param("state", issue.state())
                .param("labels", issue.labels().stream().map(GitHubIssue.Label::name).toArray(String[]::new))
                .param("createdAt", issue.createdAt().atOffset(ZoneOffset.UTC))
                .param("htmlUrl", issue.htmlUrl())
                .update();
    }

    public long count() {
        return jdbc.sql("SELECT COUNT(*) FROM issues")
                .query(Long.class)
                .single();
    }
}
