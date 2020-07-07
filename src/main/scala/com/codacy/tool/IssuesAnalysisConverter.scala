package com.codacy.tool

import java.nio.file.{Path, Paths}

import com.codacy.analysis.core.model.{IssuesAnalysis, ToolResults}
import com.codacy.analysis.core.serializer.IssuesReportSerializer

trait IssuesAnalysisConverter {
  protected def convert(lines: Seq[String], relativizeTo: Path): IssuesAnalysis

  def run(toolName: String): Unit = {
    val stdin = scala.io.Source.fromInputStream(System.in)
    val lines = stdin.getLines().to(LazyList)
    val pwd = Paths.get(System.getProperty("user.dir"))
    val issuesResults = convert(lines, relativizeTo = pwd)
    val toolResults = ToolResults(toolName, issuesResults)
    val jsonResult = IssuesReportSerializer.toJsonString(Set(toolResults))
    println(jsonResult)
  }
}
