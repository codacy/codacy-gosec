val scalaVersionNumber = "2.13.14"
val circeVersion = "0.14.12"
val graalVersion = "21.2.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "codacy-gosec",
    organization := "com.codacy",
    organizationName := "codacy",
    scalaVersion := scalaVersionNumber,
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-analysis-cli-model" % "2.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.github.scopt" %% "scopt" % "3.7.1",
      "com.frugalmechanic" %% "fm-sbt-s3-resolver" % "0.21.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    graalVMNativeImageGraalVersion := Some(graalVersion),
    graalVMNativeImageOptions ++= Seq(
      "-O1",
      "-H:+ReportExceptionStackTraces",
      "--no-fallback",
      "--report-unsupported-elements-at-runtime",
      "--static"
    )
  )
