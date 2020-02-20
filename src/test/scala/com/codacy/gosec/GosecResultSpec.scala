package com.codacy.gosec

import java.nio.file.Paths

import com.codacy.plugins.api.results
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GosecResultSpec extends AnyWordSpec with Matchers {
  "GosecIssue" should {
    "be converted into Issue correctly" in {
      val gosecIssue = GosecIssue("LOW", "HIGH", "TestId", "", Paths.get("test.go"), 1, 2)
      val issue = gosecIssue.toCodacyIssue

      issue.patternId.value mustEqual "TestId"
      issue.level mustEqual results.Result.Level.Info
    }
  }
}
