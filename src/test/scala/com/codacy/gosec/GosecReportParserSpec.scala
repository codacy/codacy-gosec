package com.codacy.gosec

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GosecReportParserSpec extends AnyWordSpec with Matchers {

  def assertSuccess(result: Either[io.circe.Error, GosecResult], expectedResult: GosecResult) = {
    result.isRight mustBe true
    result.foreach(x => x mustEqual expectedResult)
  }

  def assertFailure(result: Either[io.circe.Error, GosecResult]) = {
    result.isLeft mustBe true
  }

  "Gosec Reporter parser" should {
    "parse the json correctly" in {
      val resultLines = CommonTestMock.resultJsonText.split("\n").to(Seq)
      val result = GosecReportParser.fromJson(resultLines)
      assertSuccess(result, CommonTestMock.resultAsGosecResult)
    }

    "not fail when single line string is parsed" in {
      val result = GosecReportParser.fromJson(Seq(CommonTestMock.resultJsonText))
      assertSuccess(result, CommonTestMock.resultAsGosecResult)
    }

    "fail parsing on invalid json" in {
      val result = GosecReportParser.fromJson(Seq("""{"invalid_json": "}"""))
      assertFailure(result)
    }

    "fail parsing on no lines" in {
      val result = GosecReportParser.fromJson(Seq.empty)
      assertFailure(result)
    }

    "fail when line not defined" in {
      val result = GosecReportParser.fromJson(Seq(CommonTestMock.generateResultJsonText(line = "")))
      assertFailure(result)
    }

    "fail parsing the line" in {
      assertThrows[java.lang.RuntimeException](GosecReportParser.parseLine(null))
    }

    "parse the line correctly" in {
      assert(GosecReportParser.parseLine("1").contains(1))
    }

    "return the first line in the range" in {
      assert(GosecReportParser.parseLine("2-10").contains(2))
    }
  }
}
