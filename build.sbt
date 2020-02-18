val scalaVersionNumber = "2.13.1"
val circeVersion = "0.12.3"
val graalVersion = "19.3.1-java11"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "codacy-gosec",
    organization := "com.codacy",
    organizationName := "codacy",
    version := "0.1",
    scalaVersion := scalaVersionNumber,
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-analysis-cli-model" % "2.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.scalatest" %% "scalatest" % "3.1.0" % Test
    ),
    graalVMNativeImageGraalVersion := Some(graalVersion),
    containerBuildImage := Some(s"oracle/graalvm-ce:$graalVersion"),
    graalVMNativeImageOptions ++= Seq(
      "-O1",
      "-H:+ReportExceptionStackTraces",
      "--no-fallback",
      "--no-server",
      "--initialize-at-build-time",
      "--report-unsupported-elements-at-runtime",
      "--static"
    )
  )
