val scalaVersionNumber = "2.13.1"
val circeVersion = "0.12.3"
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
      "org.scalatest" %% "scalatest" % "3.1.0" % Test
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
