package main

import (
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"path"
	"sort"

	codacy "github.com/codacy/codacy-engine-golang-seed"
	"golang.org/x/mod/modfile"

	"github.com/securego/gosec/v2/rules"
)

const (
	toolName = "gosec"
)

var docFolder string

func main() {
	flag.StringVar(&docFolder, "docFolder", "docs", "Tool documentation folder")
	flag.Parse()
	os.Exit(run())
}

func run() int {
	rulesList := listGosecRules()

	codacyPatterns := toCodacyPatterns(rulesList)
	codacyPatternsDescription := toCodacyPatternsDescription(rulesList)

	version, err := gosecVersion()
	if err != nil {
		return 1
	}

	err = createPatternsJSONFile(codacyPatterns, version)
	if err != nil {
		return 1
	}

	err = createDescriptionFiles(codacyPatternsDescription)
	if err != nil {
		return 1
	}

	return 0
}

func gosecVersion() (string, error) {
	goModFilename := "go.mod"
	gosecDependency := "github.com/securego/gosec/v2"

	goMod, err := ioutil.ReadFile(goModFilename)
	if err != nil {
		return "", err
	}

	file, err := modfile.Parse(goModFilename, goMod, nil)
	for _, r := range file.Require {
		if r.Mod.Path == gosecDependency {
			return r.Mod.Version, nil
		}
	}
	return "", errors.New("Gosec not found")
}

func listGosecRules() []rules.RuleDefinition {
	rulesMap := rules.Generate()

	var rulesList []rules.RuleDefinition
	for _, ruleDefinition := range rulesMap {
		rulesList = append(rulesList, ruleDefinition)
	}

	sort.Slice(rulesList, func(i, j int) bool {
		return rulesList[i].ID < rulesList[j].ID
	})
	return rulesList
}

func toCodacyPatterns(rules []rules.RuleDefinition) []codacy.Pattern {
	codacyPatterns := []codacy.Pattern{}

	for _, value := range rules {
		codacyPatterns = append(codacyPatterns, codacy.Pattern{
			PatternID: value.ID,
			Category:  "Security",
			Level:     "Error",
		})
	}
	return codacyPatterns
}

func patternExtendedDescription(id string, description string) string {
	return "## " + id + "\n" + description
}

func toCodacyPatternsDescription(rules []rules.RuleDefinition) []codacy.PatternDescription {
	codacyPatternsDescription := []codacy.PatternDescription{}

	for _, value := range rules {
		codacyPatternsDescription = append(codacyPatternsDescription, codacy.PatternDescription{
			PatternID:   value.ID,
			Description: value.Description,
			Title:       value.ID,
		})
	}

	return codacyPatternsDescription
}

func createPatternsJSONFile(patterns []codacy.Pattern, toolVersion string) error {
	fmt.Println("Creating patterns.json file...")

	tool := codacy.ToolDefinition{
		Name:     toolName,
		Version:  toolVersion,
		Patterns: patterns,
	}

	toolAsJSON, err := json.MarshalIndent(tool, "", "  ")

	if err != nil {
		return err
	}

	return ioutil.WriteFile(path.Join(docFolder, "patterns.json"), toolAsJSON, 0644)
}

func createDescriptionFiles(patternsDescriptionsList []codacy.PatternDescription) error {
	fmt.Println("Creating description files...")

	for _, pattern := range patternsDescriptionsList {

		patternDescription := codacy.PatternDescription{
			PatternID:   pattern.PatternID,
			Description: pattern.Description,
			Title:       pattern.Title,
			Parameters:  []codacy.PatternParameter{},
		}

		patternsDescriptionsList = append(
			patternsDescriptionsList,
			patternDescription,
		)

		err := ioutil.WriteFile(
			path.Join(
				docFolder,
				"description",
				pattern.PatternID+".md",
			),
			[]byte(patternExtendedDescription(pattern.PatternID, pattern.Description)),
			0644,
		)

		if err != nil {
			return err
		}
	}

	descriptionsJSON, err := json.MarshalIndent(patternsDescriptionsList, "", "  ")
	if err != nil {
		return err
	}

	return ioutil.WriteFile(
		path.Join(docFolder, "description", "description.json"),
		descriptionsJSON,
		0644,
	)
}
