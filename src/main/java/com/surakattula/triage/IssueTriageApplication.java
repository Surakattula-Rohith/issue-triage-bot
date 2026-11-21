package com.surakattula.triage;

import com.surakattula.triage.github.GitHubClient;
import com.surakattula.triage.github.GitHubIssue;
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
     * Fetch one real Spring Boot issue and print it.
     */
    @Bean
    CommandLineRunner printOneIssue(GitHubClient gitHub) {
        return args -> {
            GitHubIssue issue = gitHub.getIssue("spring-projects", "spring-boot", 52761);

            System.out.println("#" + issue.number() + " " + issue.title());
            System.out.println("State:   " + issue.state());
            System.out.println("Created: " + issue.createdAt());
            System.out.println("Labels:  " + issue.labels().stream().map(GitHubIssue.Label::name).toList());
            System.out.println("Link:    " + issue.htmlUrl());
            System.out.println();
            String body = issue.body() == null ? "" : issue.body();
            System.out.println(body.length() > 300 ? body.substring(0, 300) + "..." : body);
        };
    }
}
