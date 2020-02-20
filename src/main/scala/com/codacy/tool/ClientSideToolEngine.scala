package com.codacy.tool

abstract class ClientSideToolEngine(toolName: String) extends IssuesAnalysisConverter {

  def main(args: Array[String]): Unit = {
    run(this.toolName)
  }
}
