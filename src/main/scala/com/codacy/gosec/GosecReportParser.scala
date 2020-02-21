package com.codacy.gosec

import java.nio.file.{Path, Paths}

import io.circe.parser.decode
import io.circe.{Decoder, DecodingFailure, HCursor}

import scala.util.{Failure, Success, Try}

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

      lineCursor = c.downField("line")
      lineString <- lineCursor.as[String]
      line <- parseLine(lineString).toRight(DecodingFailure("invalid line", lineCursor.history))

      column <- c.downField("column").as[Int]

    } yield GosecIssue(severity, confidence, ruleId, details, relativePathTo(file), line, column)

  def fromJson(lines: Seq[String]): Either[io.circe.Error, GosecResult] = {
    val entireJson = lines.mkString("")

    decode[GosecResult](entireJson)
  }

  def parseLine(line: String): Option[Int] = {
    line
      .split("-")
      .headOption
      .flatMap(s => s.toIntOption)
  }

  private def relativePathTo(filepath: String): Path = {
    val currentPath = Paths.get(System.getProperty("user.dir"))
    Try(currentPath.relativize(Paths.get(filepath))) match {
      case Success(path) => path
      case Failure(_) => Paths.get(filepath)
    }
  }
}
