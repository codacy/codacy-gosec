package com.codacy.gosec

import com.codacy.analysis.core.model.IssuesAnalysis.{Failure, Success}
import org.scalatest.TryValues._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GosecSpec extends AnyFlatSpec with Matchers {

  "GoSec" should "parse correctly" in {
    val result = Gosec.convert(Seq(CommonTestMock.resultJsonText))

    val expectedResult = Success(Set(CommonTestMock.fileResults))

    assert(result == expectedResult)
  }

  it should "fail when invalid format given" in {
    val text = Seq("""<test><xmlformat></xmlformat></test>""")
    val result = Gosec.convert(text)
    assert(result.isInstanceOf[Failure])
  }

  it should "fail the parse when json with missing information is given" in {
    val lines = Seq(s"""{
         |    "Issues": [
         |        {
         |            "severity": "LOW"
         |        }
         |    ]
         |}""".stripMargin)

    val result = Gosec.convert(lines)

    assert(result.isInstanceOf[Failure])
  }

  it should "convert gosec report into codacy file results report" in {
    val fileResults = Gosec.gosecReportToFileResults(CommonTestMock.resultAsGosecResult)
    assert(fileResults.success.value == Set(CommonTestMock.fileResults))
  }

  it should "throw exception on null param" in {
    assert(Gosec.gosecReportToFileResults(null).isFailure)
  }

  it should "return success" in {
    val issuesAnalysis = Gosec.gosecReportToIssuesAnalysis(CommonTestMock.resultAsGosecResult)
    assert(issuesAnalysis.isInstanceOf[Success])
  }

  it should "return failure" in {
    val issuesAnalysis = Gosec.gosecReportToIssuesAnalysis(null)
    assert(issuesAnalysis.isInstanceOf[Failure])
  }
}
