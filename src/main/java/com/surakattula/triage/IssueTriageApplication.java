package com.surakattula.triage;

import com.surakattula.triage.github.GitHubClient;
import com.surakattula.triage.github.GitHubIssue;
import com.surakattula.triage.storage.IssueRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
     * Fetch every Spring Boot issue, page by page, saving each page as it arrives.
     */
    @Bean
    CommandLineRunner saveAllIssues(GitHubClient gitHub, IssueRepository repository) {
        return args -> {
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
            long start = System.currentTimeMillis();
            // A lambda can't change a local int, so the counters are objects it can update
            AtomicInteger pages = new AtomicInteger();
            AtomicInteger saved = new AtomicInteger();

            gitHub.forEachPage("spring-projects", "spring-boot", items -> {
                List<GitHubIssue> issues = items.stream()
                        .filter(item -> !item.isPullRequest())
                        .toList();
                issues.forEach(repository::save);
                long seconds = (System.currentTimeMillis() - start) / 1000;
                System.out.println("Page " + pages.incrementAndGet() + ": saved " + issues.size() + " issues"
                        + " (total " + saved.addAndGet(issues.size()) + ", " + seconds + " s)");
            });

            long seconds = (System.currentTimeMillis() - start) / 1000;
            System.out.println("Done in " + seconds + " s. Issues in the database: " + repository.count());
            System.out.println("GitHub requests left: " + gitHub.requestsLeft());
        };
    }
}
