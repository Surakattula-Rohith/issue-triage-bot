package com.surakattula.triage;

import com.surakattula.triage.github.GitHubClient;
import com.surakattula.triage.github.GitHubIssue;
import com.surakattula.triage.storage.IssueRepository;
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
     * Fetch every Spring Boot issue, page by page until the data ends, saving each page as it arrives.
     */
    @Bean
    CommandLineRunner saveAllIssues(GitHubClient gitHub, IssueRepository repository) {
        return args -> {
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
            long start = System.currentTimeMillis();
            int saved = 0;

            for (int page = 1; ; page++) {
                List<GitHubIssue> items = gitHub.listIssues("spring-projects", "spring-boot", page);
                if (items.isEmpty()) {
                    break; // no more pages
                }
                List<GitHubIssue> pageIssues = items.stream()
                        .filter(item -> !item.isPullRequest())
                        .toList();
                pageIssues.forEach(repository::save);
                saved += pageIssues.size();
                long seconds = (System.currentTimeMillis() - start) / 1000;
                System.out.println("Page " + page + ": saved " + pageIssues.size() + " issues"
                        + " (total " + saved + ", " + seconds + " s)");
            }

            System.out.println("Done. Issues in the database: " + repository.count());
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
        };
    }
}
