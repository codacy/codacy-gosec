package com.codacy.tool

import com.codacy.analysis.core.model.{IssuesAnalysis, ToolResults}
import com.codacy.analysis.core.serializer.IssuesReportSerializer

trait IssuesAnalysisConverter {
  protected def convert(lines: Seq[String]): IssuesAnalysis

  def run(toolName: String): Unit = {
    val lines = scala.io.Source.stdin.getLines().to(LazyList)
    val issuesResults = convert(lines)
    val toolResults = ToolResults(toolName, issuesResults)
    val jsonResult = IssuesReportSerializer.toJsonString(Set(toolResults))
    println(jsonResult)
  }
}
