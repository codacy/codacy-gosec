[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4895f1eeb40c4a348ad5f8d749a276be)](https://www.codacy.com?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-gosec&utm_campaign=Badge_Grade)

[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4895f1eeb40c4a348ad5f8d749a276be)](https://www.codacy.com?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-gosec&utm_campaign=Badge_Coverage)

# Codacy gosec

A standalone tool that converts gosec results to Codacy's format. It allows the integration of gosec into your Codacy workflow.

## How it works

1.  The tool receives as input the gosec result from stdin.

2.  Converts gosec result into Codacy's format

3.  Prints Codacy's format to stdout 

## Usage

The upload of results for a commit is done in two steps:

-   uploading all results
-   telling Codacy that it can run the rest of the analysis

For this a [project API](https://support.codacy.com/hc/en-us/articles/207994675-Project-API) token is required.

```bash
export PROJECT_TOKEN="YOUR-TOKEN"
export COMMIT="COMMIT-UUID"

gosec -fmt json -log log.txt ./... | \
./codacy-gosec-"<version>" | \
curl -XPOST -L -H "project_token: $PROJECT_TOKEN"
    -H "Content-type: application/json" -d @- \
    "https://api.codacy.com/2.0/commit/$COMMIT/issuesRemoteResults"

curl -XPOST -L -H 'project_token: $PROJECT_TOKEN' \
	-H "Content-type: application/json" \
	"https://api.codacy.com/2.0/commit/$COMMIT/resultsFinal"
```

For self-hosted installations:

```bash
export PROJECT_TOKEN="YOUR-TOKEN"
export COMMIT="COMMIT-UUID"
export CODACY_URL="CODACY-INSTALLATION-URL"

gosec -fmt json -log log.txt ./... | \
./codacy-gosec-"<version>" | \
curl -XPOST -L -H "project_token: $PROJECT_TOKEN"
    -H "Content-type: application/json" -d @- \
    "$CODACY_URL/2.0/commit/$COMMIT/issuesRemoteResults"

curl -XPOST -L -H 'project_token: $PROJECT_TOKEN' \
	-H "Content-type: application/json" \
	"$CODACY_URL/2.0/commit/$COMMIT/resultsFinal"
```

## Building

#### Compile

    sbt compile

#### Format

    sbt ";scalafmt;test:scalafmt;sbt:scalafmt"

#### Tests

    sbt test

##### Build native image (requires docker)

`sbt "graalvm-native-image:packageBin"`

#### Build fat-jar

    sbt assembly

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

-   Identify new Static Analysis issues
-   Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
-   Auto-comments on Commits and Pull Requests
-   Integrations with Slack, HipChat, Jira, YouTrack
-   Track issues Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
