package com.codacy.gosec

import java.nio.file.Paths

import io.circe.parser.decode
import io.circe.{Decoder, HCursor}

import scala.util.Try

object GosecReportParser {
  private[GosecReportParser] implicit val gosecResultDecoder: Decoder[GosecResult] = (c: HCursor) =>
    for {
      issues <- c.downField("Issues").as[Seq[GosecIssue]]
    } yield GosecResult(issues)

  private[GosecReportParser] implicit val gosecIssueDecoder: Decoder[GosecIssue] = (c: HCursor) =>
    for {
      severity <- c.downField("severity").as[String]
      confidence <- c.downField("confidence").as[String]
      ruleId <- c.downField("rule_id").as[String]
      details <- c.downField("details").as[String]
      file <- c.downField("file").as[String]
      line <- c.downField("line").as[String]
      column <- c.downField("column").as[Int]

      lineAsInt = parseLine(line)
    } yield GosecIssue(severity, confidence, ruleId, details, Paths.get(file), lineAsInt, column)

  def fromJson(lines: Seq[String]): Try[GosecResult] = Try {
    val entireJson = lines.mkString("")

    decode[GosecResult](entireJson) match {
      case Right(res) => res
      case Left(ex) => throw ex
    }
  }

  def parseLine(line: String): Int = {
    line
      .split("-")
      .headOption
      .getOrElse(throw new RuntimeException("Line is empty"))
      .toInt
  }
}
