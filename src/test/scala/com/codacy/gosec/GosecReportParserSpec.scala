package com.codacy.gosec

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Try

class GosecReportParserSpec extends AnyFlatSpec with Matchers {
  def assertSuccess(result: Try[GosecResult], expectedResult: GosecResult) = {
    assert(result.isSuccess)
    assert(result.getOrElse(null) == expectedResult)
  }

  "Gosec Reporter parser" should "parse the json correctly" in {
    val resultLines = CommonTestMock.resultJsonText.split("\n").to(Seq)
    val result = GosecReportParser.fromJson(resultLines)
    assertSuccess(result, CommonTestMock.resultAsGosecResult)
  }

  it should "not fail when single line string is parsed" in {
    val result = GosecReportParser.fromJson(Seq(CommonTestMock.resultJsonText))
    assertSuccess(result, CommonTestMock.resultAsGosecResult)
  }

  it should "fail parsing on invalid json" in {
    val result = GosecReportParser.fromJson(Seq("""{"invalid_json": "}"""))
    assert(result.isFailure)
  }

  it should "fail parsing on no lines" in {
    val result = GosecReportParser.fromJson(Seq.empty)
    assert(result.isFailure)
  }

  it should "fail when line not defined" in {
    val result = GosecReportParser.fromJson(Seq(CommonTestMock.generateResultJsonText(line = "")))
    assert(result.isFailure)
  }

  it should "fail parsing the line" in {
    assertThrows[java.lang.RuntimeException](GosecReportParser.parseLine(""))
  }

  it should "parse the line correctly" in {
    assert(GosecReportParser.parseLine("1") == 1)
  }

  it should "return the first line in the range" in {
    assert(GosecReportParser.parseLine("2-10") == 2)
  }
}
