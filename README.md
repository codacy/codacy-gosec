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

> When the option **“Run analysis through build server”** is enabled, the Codacy analysis will not start until you call the endpoint `/2.0/commit/{commitUuid}/resultsFinal` signalling that Codacy can use the sent results and start a new analysis.

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

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo — most commonly bumping the wrapped gosec version, but also base image / sbt / orb / dependency bumps. Follow it top to bottom; it tells you what to change, how to regenerate derived files, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy engine**, but an unusual one: it is a Scala (sbt) command-line **converter**, not a wrapper that invokes the underlying tool. `src/main/scala/com/codacy/gosec/GosecReportParser.scala` and `Gosec.scala` read [gosec](https://github.com/securego/gosec)'s own JSON output from stdin and translate it into Codacy's issue format on stdout — the customer is expected to run gosec themselves and pipe its output into this tool (see the `## Usage` section above). The Docker image built from the root `Dockerfile` does **not** run gosec or this converter at all: its `entry.sh` simply prints `"gosec cannot be run by Codacy"` and exits 1. The only thing the Docker image is actually used for is producing the `docs/` folder contents (see below).

`docs/` is machine-consumed configuration, not just documentation:

- `docs/patterns.json` — the list of gosec rules ("patterns", e.g. `G101`, `G401`) Codacy knows about, plus the gosec version string. Generated file, do not hand-edit.
- `docs/description/description.json` + `docs/description/*.md` — one description file per rule ID, used in the Codacy UI. Generated file, do not hand-edit.
- `docs/tool-description.md` — short blurb about the tool, hand-maintained.

These are generated by a **separate, self-contained Go module** at `doc-generation/` (`doc-generation/main.go`), which is a completely different toolchain from the Scala engine itself. It imports `github.com/securego/gosec/v2/rules` directly and calls `rules.Generate(false).Rules` to enumerate every rule gosec knows about — there is no scraping or cloning of an external repo like some other Codacy engines do. It also reads `doc-generation/go.mod` at runtime (via `golang.org/x/mod/modfile`) to extract the pinned gosec version and stamp it into `patterns.json`. This means: **the gosec version in `docs/patterns.json` is entirely derived from `doc-generation/go.mod`** — you never hand-edit the version, you bump the Go dependency and regenerate.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `doc-generation/go.mod` → `github.com/securego/gosec/v2` | The gosec version whose rule set is documented in `docs/patterns.json` | This is the **single source of truth** for the gosec version. Bump it, then regenerate docs (step 3 below). |
| `Dockerfile` → `FROM golang:...` (builder stage) | The Go toolchain used to build `doc-generation` and run the doc generator | Must satisfy the `go`/`toolchain` directive in `doc-generation/go.mod` (currently `go 1.23.0`, `toolchain go1.24.5`). |
| `Dockerfile` → `FROM alpine:...` (final stage) | Runtime base of the (docs-only) published image | Bump opportunistically alongside the builder stage; not version-critical since no code runs in this stage. |
| `build.sbt` → `scalaVersionNumber`, `graalVersion`, `codacy-analysis-cli-model`, `scopt`, `scalatest` | The Scala engine's own toolchain/deps | Unrelated to the gosec version; only bump these when the task explicitly scopes a Scala/GraalVM/dependency update. **`graalVersion` is fragile** — see "Common failure modes" below. |
| `project/build.properties` → `sbt.version`, `project/plugins.sbt` → `codacy-sbt-plugin`/`sbt-assembly`/`sbt-scoverage`/`sbt-native-packager` | Build tooling versions | Only touch when the task scopes an sbt-tooling bump. |
| `.circleci/config.yml` → `codacy/base@<version>` orb | Shared CircleCI steps (checkout/version, sbt, docker publish, ghr, s3) | Check the latest published version if the task scopes a CI-tooling bump. |

### 3. Step-by-step update procedure (gosec version bump)

1. In `doc-generation/`, bump the gosec dependency: `cd doc-generation && go get github.com/securego/gosec/v2@v<new-version> && go mod tidy`. Confirm the resulting `go.mod`/`go.sum` diff only touches the expected dependency (and its transitive deps).
2. If the new gosec version requires a newer Go toolchain than `doc-generation/go.mod` currently declares, bump the `golang:...` builder image tag in `Dockerfile` to match (and the `toolchain` directive if `go get`/`go mod tidy` didn't already update it).
3. **Regenerate the docs**: `cd doc-generation && go run main.go -docFolder=../docs`. Review the diff in `docs/patterns.json` and `docs/description/` for added/removed/renamed rule IDs — new gosec versions can add or retire `G*` rules, which shows up as new/deleted `.md` files and a changed `Patterns` array plus `version` field in `patterns.json`.
4. Check `rules.Generate(...)` and the `rules.RuleDefinition` struct in the new gosec version still expose the same fields `doc-generation/main.go` relies on (`ID`, `Description`) — a gosec API change could break compilation of the doc generator even if `go get` succeeds.
5. **Compile and format** the Scala side (unaffected by the gosec bump, but must still pass): `sbt compile`, then `sbt ";scalafmt;test:scalafmt;sbt:scalafmt"`.
6. **Run the native test suite**: `sbt test` — this exercises `GosecReportParserSpec`, `GosecSpec`, and `GosecResultSpec` against fixture gosec JSON. A pure gosec-version/doc bump should not require touching these unless the JSON schema gosec emits has changed.
7. **Build the Docker image** to prove the doc-generation stage still compiles/runs with the new dependency: `docker build -t codacy-gosec .`
8. **Build the native image and fat-jar** if the task also touches `build.sbt`/GraalVM settings: `sbt "graalvm-native-image:packageBin"` (requires Docker) and `sbt assembly`.
9. **Commit** the version bump(s) together with the regenerated `docs/` files in one change.
10. **Push and open a PR.**
11. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. **The CI environment's toolchain can differ from your local one**, so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human.

### 4. Common failure modes and fixes

| Symptom | Cause | Fix |
|---|---|---|
| `sbt "graalvm-native-image:packageBin"` succeeds but the produced binary/jar mismatches or native-image build breaks | `graalVersion` in `build.sbt` bumped too aggressively (this happened for real in this repo's history: gosec 2.22.7 landed with `graalVersion` jumped to `24.2.2`, which broke jar packaging and had to be downgraded back to `22.3.3` in a follow-up commit) | Bump `graalVersion` conservatively and re-run both `sbt "graalvm-native-image:packageBin"` and `sbt assembly` before pushing; don't bump it as a drive-by change alongside an unrelated gosec bump. |
| `doc-generation` fails to `go build`/`go run` after `go get`-ing a new gosec version | Upstream gosec changed the `rules.RuleDefinition` struct or the `rules.Generate` signature | Read the new gosec version's `rules` package source (via `go doc` or the vendored module cache) and update `doc-generation/main.go` accordingly — this is a real dependency on gosec's internal Go API, not just its CLI/JSON contract. |

### 5. Definition of done

- Version bump(s) reflected in all files that encode them (`doc-generation/go.mod`, and `Dockerfile`'s builder image if the Go toolchain requirement changed).
- `docs/patterns.json` and `docs/description/*` regenerated via `doc-generation`, with added/removed rule IDs reviewed.
- `sbt compile`, `sbt scalafmt`/`scalafmtCheck`, and `sbt test` pass locally.
- Docker image builds successfully (`docker build -t codacy-gosec .`).
- If `build.sbt`/GraalVM settings were touched: `sbt "graalvm-native-image:packageBin"` and `sbt assembly` both succeed.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure until all pass.

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
