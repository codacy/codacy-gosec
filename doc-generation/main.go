package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"path"

	codacy "github.com/codacy/codacy-engine-golang-seed"

	"github.com/securego/gosec/v2/rules"
)

const (
	toolName = "codacy-gosec"
)

var docFolder string

func main() {
	flag.StringVar(&docFolder, "docFolder", "docs", "Tool documentation folder")
	flag.Parse()
	os.Exit(run())
}

func run() int {
	rulesList := rules.Generate()

	codacyPatterns := toCodacyPatterns(rulesList)
	codacyPatternsDescription := toCodacyPatternsDescription(rulesList)

	err := createPatternsJSONFile(codacyPatterns, "")
	if err != nil {
		return 1
	}

	err = createDescriptionFiles(codacyPatternsDescription)
	if err != nil {
		return 1
	}

	return 0
}

func toCodacyPatterns(rules rules.RuleList) []codacy.Pattern {
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
	return "##" + id + "\n" + description
}

func toCodacyPatternsDescription(rules rules.RuleList) []codacy.PatternDescription {
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
