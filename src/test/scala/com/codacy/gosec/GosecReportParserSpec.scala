package com.codacy.gosec

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GosecReportParserSpec extends AnyFlatSpec with Matchers {

  def assertSuccess(result: Either[io.circe.Error, GosecResult], expectedResult: GosecResult) = {
    assert(result.isRight)
    result.foreach(x => assert(x == expectedResult))
  }

  def assertFailure(result: Either[io.circe.Error, GosecResult]) = {
    assert(result.isLeft)
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
    assertFailure(result)
  }

  it should "fail parsing on no lines" in {
    val result = GosecReportParser.fromJson(Seq.empty)
    assertFailure(result)
  }

  it should "fail when line not defined" in {
    val result = GosecReportParser.fromJson(Seq(CommonTestMock.generateResultJsonText(line = "")))
    assertFailure(result)
  }

  it should "fail parsing the line" in {
    assertThrows[java.lang.RuntimeException](GosecReportParser.parseLine(null))
  }

  it should "parse the line correctly" in {
    assert(GosecReportParser.parseLine("1").contains(1))
  }

  it should "return the first line in the range" in {
    assert(GosecReportParser.parseLine("2-10").contains(2))
  }
}
