# korekto-simple-web-app-grader
Korekto grader for the simple webapp exercise

[![Build](https://github.com/lernejo/korekto-simple-web-app-grader/actions/workflows/build.yml/badge.svg)](https://github.com/lernejo/korekto-simple-web-app-grader/actions)
[![codecov](https://codecov.io/gh/lernejo/korekto-simple-web-app-grader/branch/main/graph/badge.svg?token=S9Q1TH2AYR)](https://codecov.io/gh/lernejo/korekto-simple-web-app-grader)

## Launch locally

This **grader** uses [Testcontainers](https://www.testcontainers.org/) which needs Docker.  
On Windows this means that the Docker engine must be running.

To launch the tool locally, run `com.github.lernejo.korekto.toolkit.launcher.GradingJobLauncher` with the
argument `-s=mySlug`

### With Maven

```bash
mvn compile exec:java -Dexec.args="-s=yourGitHubLogin"
```

### With intelliJ

![Demo Run Configuration](https://raw.githubusercontent.com/lernejo/korekto-toolkit/main/docs/demo_run_configuration.png)

## GitHub API rate limiting

When using the grader a lot, GitHub may block API calls for a certain amount of time (criterias change regularly).
This is because by default GitHub API are accessed anonymously.

To increase the API usage quota, use a [Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) in order to access GitHub APIs authenticated.

Such a token can be supplied to the grader tool through the system property : `-Dgithub_token=<your token>`

Like so:

```bash
mvn compile exec:java -Dexec.args="-s=yourGitHubLogin" -Dgithub_token=<your token>
```
## FAQ

#### What can I do if the grader cannot start my program within the 20sec allocated ?

If your PC is slow, 20 sec may not be enough to start your project. In that can, try to increase the timeout with `-Dserver_start_timeout=60` (for 60 sec).
