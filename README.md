# issue-triage-bot

A bot that triages new GitHub issues on large open-source Java projects. For each issue it pulls out the key facts, finds likely duplicates, suggests labels and asks for missing details. It's measured against what the real maintainers did.

**Status:** early work in progress.

## Run it

Needs Java 21.

Optional but recommended: a GitHub token raises the limit from 60 to 5,000 requests per hour. Create a fine-grained token with read-only access to public repositories, then put it in a `.env` file in the project folder (already in `.gitignore`):

```text
GITHUB_TOKEN=your-token-here
```

```bash
./mvnw spring-boot:run
```

Right now it fetches the newest 300 Spring Boot issues and pull requests from the GitHub API, keeps the issues, and prints a summary.
