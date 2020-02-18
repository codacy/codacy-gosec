package com.codacy.gosec

import java.nio.file.Paths

import com.codacy.plugins.api.results
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GosecResultSpec extends AnyFlatSpec with Matchers {
  "GosecIssue" should "be converted into Issue correctly" in {
    val gosecIssue = GosecIssue("LOW", "HIGH", "TestId", "", Paths.get("test.go"), 1, 2)
    val issue = gosecIssue.toCodacyIssue

    assert(issue.patternId.value == "TestId")
    assert(issue.level == results.Result.Level.Info)
  }
}
