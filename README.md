# korekto-simple-web-app-grader
Korekto grader for the simple webapp exercise

[![Build](https://github.com/lernejo/korekto-simple-web-app-grader/actions/workflows/build.yml/badge.svg)](https://github.com/lernejo/korekto-simple-web-app-grader/actions)
[![codecov](https://codecov.io/gh/lernejo/korekto-simple-web-app-grader/branch/main/graph/badge.svg?token=S9Q1TH2AYR)](https://codecov.io/gh/lernejo/korekto-simple-web-app-grader)
![License](https://img.shields.io/badge/License-Elastic_License_v2-blue)

Exercise subject: [here](EXERCISE_fr.adoc)

# How to launch
You will need these 2 env vars:
* `GH_LOGIN` your GitHub login
* `GH_TOKEN` a [**P**ersonal **A**ccess **T**oken](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) with permissions:
    * (classic) : `repo` and `user`
    * (fine-grained):
        * Repository permissions:
            * **Actions**: `Read-only`
            * **Contents**: `Read-only`

```bash
git clone git@github.com:lernejo/korekto-simple-web-app-grader.git
cd korekto-simple-web-app-grader
./mvnw compile exec:java -Dexec.args="-s=$GH_LOGIN" -Dgithub_token="$GH_TOKEN"
```

## FAQ

#### What can I do if the grader cannot start my program within the 20sec allocated ?

If your PC is slow, 20 sec may not be enough to start your project. In that can, try to increase the timeout with `-Dserver_start_timeout=60` (for 60 sec).
