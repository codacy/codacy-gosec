package com.codacy.tool

import com.codacy.analysis.core.model.ToolResults
import com.codacy.analysis.core.serializer.IssuesReportSerializer

abstract class ClientSideToolEngine(toolName: String) extends IssuesAnalysisConverter {
  def main(args: Array[String]): Unit = {
    val lines = scala.io.Source.stdin.getLines().to(LazyList)
    val issuesResults = convert(lines)
    val toolResults = ToolResults(this.toolName, issuesResults)
    val jsonResult = IssuesReportSerializer.toJsonString(Set(toolResults))
    println(jsonResult)
  }
}
