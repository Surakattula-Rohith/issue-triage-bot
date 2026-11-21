package com.surakattula.triage;

import com.surakattula.triage.github.GitHubClient;
import com.surakattula.triage.github.GitHubIssue;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IssueTriageApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssueTriageApplication.class, args);
    }

    /**
     * Fetch the newest 3 pages of Spring Boot issues and keep only real issues.
     */
    @Bean
    CommandLineRunner printFirstPages(GitHubClient gitHub) {
        return args -> {
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
            List<GitHubIssue> issues = new ArrayList<>();

            for (int page = 1; page <= 3; page++) {
                List<GitHubIssue> items = gitHub.listIssues("spring-projects", "spring-boot", page);
                if (items.isEmpty()) {
                    break; // no more pages
                }
                List<GitHubIssue> pageIssues = items.stream()
                        .filter(item -> !item.isPullRequest())
                        .toList();
                System.out.println("Page " + page + ": " + items.size() + " items = "
                        + pageIssues.size() + " issues + " + (items.size() - pageIssues.size()) + " pull requests");
                issues.addAll(pageIssues);
            }

            System.out.println("Total issues: " + issues.size());
            issues.stream()
                    .limit(5)
                    .forEach(issue -> System.out.println("#" + issue.number() + " " + issue.title()));
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
        };
    }
}
