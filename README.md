# issue-triage-bot

A bot that triages new GitHub issues on large open-source Java projects. For each issue it pulls out the key facts, finds likely duplicates, suggests labels and asks for missing details. It's measured against what the real maintainers did.

**Status:** early work in progress.

## Run it

Needs Java 21.

```bash
./mvnw spring-boot:run
```

Right now it fetches one public Spring Boot issue from the GitHub API and prints it.
