[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4895f1eeb40c4a348ad5f8d749a276be)](https://www.codacy.com?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-gosec&utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4895f1eeb40c4a348ad5f8d749a276be)](https://www.codacy.com?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-gosec&utm_campaign=Badge_Coverage)

# Codacy gosec

A standalone tool that converts gosec results to Codacy's format. It allows the integration of gosec into your Codacy workflow.

## How it works

1.  The tool receives as input the gosec result from stdin. The gosec result must be in JSON format.

2.  Converts gosec result into Codacy's format

3.  Prints Codacy's format to stdout

> NOTE: the tool must be run in the project root folder.

## Usage

### Requirements

To get your gosec results into Codacy you'll need to:

-   [Enable Gosec](https://docs.codacy.com/repositories-configure/configuring-code-patterns/) and configure the corresponding code patterns on your repository **Code patterns** page
-   Enable the setting **Run analysis through build server** on your repository **Settings**, tab **General**, **Repository analysis**
-   Obtain a [project API token](https://docs.codacy.com/codacy-api/api-tokens/#project-api-tokens)
-   Install [gosec](https://github.com/securego/gosec#install)
-   Download the `codacy-gosec` binary (or Java jar) from [the releases page](https://github.com/codacy/codacy-gosec/releases)


### Sending the results to Codacy

Sending the results of running gosec to Codacy involves the steps below, which you can automate in your CI build process:

1.  Run gosec
2.  Convert the gosec output to a format that the Codacy API accepts using the [codacy-gosec](https://github.com/codacy/codacy-gosec/releases) binary
3.  Send the results to Codacy
4.  Finally, signal that Codacy can use the sent results and start a new analysis

```bash
export PROJECT_TOKEN="YOUR-TOKEN"
export COMMIT="COMMIT-UUID"

gosec -fmt json -log log.txt ./... | \
./codacy-gosec-"<version>" | \
curl -XPOST -L -H "project-token: $PROJECT_TOKEN" \
    -H "Content-type: application/json" -d @- \
    "https://api.codacy.com/2.0/commit/$COMMIT/issuesRemoteResults"

curl -XPOST -L -H "project-token: $PROJECT_TOKEN" \
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
curl -XPOST -L -H "project-token: $PROJECT_TOKEN"
    -H "Content-type: application/json" -d @- \
    "$CODACY_URL/2.0/commit/$COMMIT/issuesRemoteResults"

curl -XPOST -L -H "project-token: $PROJECT_TOKEN" \
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

#### Generate documentation

```bash
cd doc-generation
go run main.go -docFolder=../docs
```

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
