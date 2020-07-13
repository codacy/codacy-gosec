package com.codacy.gosec

import java.nio.file.Path

import com.codacy.analysis.core.model.{FullLocation, Issue}
import com.codacy.plugins.api.results

case class GosecResult(issues: Seq[GosecIssue])

case class GosecIssue(
    severity: String,
    confidence: String,
    ruleId: String,
    details: String,
    file: Path,
    line: Int,
    column: Int
) {

  def toCodacyIssue(toolName: String): Issue = {
    Issue(
      results.Pattern.Id(s"${toolName}_$ruleId"),
      file,
      Issue.Message(details),
      convertLevel(severity),
      convertCategory(ruleId),
      FullLocation(line, column)
    )
  }

  private def convertLevel(level: String): results.Result.Level.Value = level match {
    case "LOW" => results.Result.Level.Info
    case "MEDIUM" => results.Result.Level.Warn
    case "HIGH" => results.Result.Level.Err
    case _ => results.Result.Level.Info
  }

  private def convertCategory(checkName: String): Option[results.Pattern.Category] =
    None
}
