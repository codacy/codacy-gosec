package com.codacy.gosec

import java.nio.file.Path

import com.codacy.analysis.core.model.IssuesAnalysis
import com.codacy.analysis.core.model.IssuesAnalysis.FileResults
import com.codacy.tool.ClientSideToolEngine

object Gosec extends ClientSideToolEngine(toolName = "gosec") {

  private def gosecReportToIssuesAnalysis(report: GosecResult): IssuesAnalysis = {
    val reportFileResults = report.issues
      .groupBy(_.file)
      .view
      .map {
        case (path, res) =>
          FileResults(path, res.view.map(_.toCodacyIssue(toolName)).toSet)
      }
      .to(Set)

    IssuesAnalysis.Success(reportFileResults)
  }

  override def convert(lines: Seq[String], relativizeTo: Path): IssuesAnalysis = {
    GosecReportParser.fromJson(lines, relativizeTo) match {
      case Right(report) => gosecReportToIssuesAnalysis(report)
      case Left(err) => IssuesAnalysis.Failure(err.getMessage)
    }
  }
}
