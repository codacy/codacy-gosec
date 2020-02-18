package com.codacy.gosec

import com.codacy.analysis.core.model.IssuesAnalysis
import com.codacy.tool.ClientSideToolEngine

object Gosec extends ClientSideToolEngine(toolName = "gosec") {
  override def convert(lines: Seq[String]): IssuesAnalysis = ???
}
