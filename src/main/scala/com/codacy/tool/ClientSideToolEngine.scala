package com.codacy.tool

abstract class ClientSideToolEngine(toolName: String) extends IssuesAnalysisConverter {
  def main(args: Array[String]): Unit = {
    val lines = scala.io.Source.stdin.getLines().to(LazyList)
    val jsonResult = convert(lines)
    println(jsonResult)
  }
}
