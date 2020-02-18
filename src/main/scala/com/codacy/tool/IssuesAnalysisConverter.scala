package com.codacy.tool

import com.codacy.analysis.core.model.IssuesAnalysis

trait IssuesAnalysisConverter {
  def convert(lines: Seq[String]): IssuesAnalysis
}
