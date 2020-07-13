package com.codacy.gosec

import java.nio.file.{Path, Paths}

import com.codacy.analysis.core.model.IssuesAnalysis.FileResults
import com.codacy.analysis.core.model.{FullLocation, Issue}
import com.codacy.plugins.api.results

object CommonTestMock {
  val currentDir = Paths.get(System.getProperty("user.dir"))
  val filename = "main.go"
  val fileNamePath = Paths.get(filename)

  val filePath: Path = Paths.get(currentDir.toAbsolutePath.toString, filename)
  val patternId = "G104"
  val resultPatternId = s"gosec_$patternId"
  val severity = "LOW"
  val confidence = "HIGH"
  val details = "Errors unhandled."
  val codeLine = "70-78"
  val codeColumn = "4"

  val code =
    """ioutil.WriteFile(path.Join(docsFolder,docsDescriptionFolder,example.ID+\".md\",),[]byte(exampleMD),0644,)"""

  val resultAsGosecResult: GosecResult = GosecResult(
    Seq(
      GosecIssue(
        severity,
        confidence,
        patternId,
        details,
        fileNamePath,
        GosecReportParser.parseLine(codeLine).getOrElse(throw new Exception("error parsing code line")),
        codeColumn.toInt
      )
    )
  )

  val fileResults: FileResults = FileResults(
    fileNamePath,
    Set(
      Issue(
        results.Pattern.Id(resultPatternId),
        fileNamePath,
        Issue.Message(details),
        results.Result.Level.Info,
        None,
        FullLocation(
          GosecReportParser.parseLine(codeLine).getOrElse(throw new Exception("error parsing code line")),
          codeColumn.toInt
        )
      )
    )
  )

  val resultJsonText: String = generateResultJsonText()

  def generateResultJsonText(
      patternId: String = patternId,
      line: String = codeLine,
      severity: String = severity
  ): String =
    s"""{
       |    "Golang errors": {},
       |    "Issues": [
       |        {
       |            "severity": "$severity",
       |            "confidence": "$confidence",
       |            "cwe": {
       |                "ID": "703",
       |                "URL": ""
       |            },
       |            "rule_id": "$patternId",
       |            "details": "$details",
       |            "file": "${fileNamePath.toString}",
       |            "code": "$code",
       |            "line": "$line",
       |            "column": "$codeColumn"
       |        }
       |    ],
       |    "Stats": {
       |        "files": 2
       |    }
       |}""".stripMargin
}
