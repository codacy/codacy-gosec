
val circeVersion = "0.12.3"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "codacy-gosec",
    organization := "com.codacy",
    organizationName := "codacy",
    version := "0.1",
    scalaVersion := "2.13.1",
    mainClass in Compile := Some("com.codacy.gosec.GoSec"),
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-analysis-cli-model" % "2.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.scalatest" %% "scalatest" % "3.1.0" % Test
    )
  )
