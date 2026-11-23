package com.surakattula.triage;

import com.surakattula.triage.github.GitHubClient;
import com.surakattula.triage.github.GitHubIssue;
import com.surakattula.triage.storage.IssueRepository;
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
     * Fetch the newest 3 pages of Spring Boot issues, keep only real issues, and save them.
     */
    @Bean
    CommandLineRunner saveFirstPages(GitHubClient gitHub, IssueRepository repository) {
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

            issues.forEach(repository::save);
            System.out.println("Saved " + issues.size() + " issues. Issues in the database: " + repository.count());
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
        };
    }
}
