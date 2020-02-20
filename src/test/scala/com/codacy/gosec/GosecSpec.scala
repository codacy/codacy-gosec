package com.codacy.gosec

import com.codacy.analysis.core.model.IssuesAnalysis
import com.codacy.analysis.core.model.IssuesAnalysis.{Failure, FileResults, Success}
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GosecSpec extends AnyWordSpec with Matchers with PrivateMethodTester {
  val gosecReportToFileResults = PrivateMethod[Set[FileResults]](Symbol("gosecReportToFileResults"))
  val gosecReportToIssuesAnalysis = PrivateMethod[IssuesAnalysis](Symbol("gosecReportToIssuesAnalysis"))

  "GoSec" should {
    "parse correctly" in {
      val result = Gosec.convert(Seq(CommonTestMock.resultJsonText))

      val expectedResult = Success(Set(CommonTestMock.fileResults))

      result mustEqual expectedResult
    }

    "fail when invalid format given" in {
      val text = Seq("""<test><xmlformat></xmlformat></test>""")
      val result = Gosec.convert(text)

      result mustBe a[Failure]
    }

    "fail the parse when json with missing information is given" in {
      val lines = Seq(s"""{
                         |    "Issues": [
                         |        {
                         |            "severity": "LOW"
                         |        }
                         |    ]
                         |}""".stripMargin)

      val result = Gosec.convert(lines)

      result mustBe a[Failure]
    }

    "convert gosec report into codacy file results report" in {
      val fileResults = Gosec invokePrivate gosecReportToFileResults(CommonTestMock.resultAsGosecResult)

      fileResults mustEqual Set(CommonTestMock.fileResults)
    }

    "return success" in {
      val issuesAnalysis = Gosec invokePrivate gosecReportToIssuesAnalysis(CommonTestMock.resultAsGosecResult)
      issuesAnalysis mustBe a[Success]
    }
  }

}
