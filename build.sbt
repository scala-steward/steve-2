ThisBuild / scalaVersion := "3.1.1"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions =
  new {
    val catsEffect = "3.3.14"
    val munit = "1.0.7"
    val tapir = "0.20.1"
    val http4s = "0.23.10"
    val logback = "1.2.11"
  }

val commonSettings = Seq(
  scalacOptions -= "-Xfatal-warnings",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,
    "org.typelevel" %% "munit-cats-effect-3" % Versions.munit % Test,
    // compilerPlugin("org.polyvariant" %% "better-tostring" % "0.3.9" cross CrossVersion.full),
  ),
)

val nativeImageSettings = Seq(
  Compile / mainClass := Some("steve.Main"),
  nativeImageVersion := "21.2.0",
  nativeImageOptions ++= Seq(
    s"-H:ReflectionConfigurationFiles=${(Compile / resourceDirectory).value / "reflect-config.json"}",
    s"-H:ResourceConfigurationFiles=${(Compile / resourceDirectory).value / "resource-config.json"}",
    "-H:+ReportExceptionStackTraces",
    "--no-fallback", // Don't fall back to the JVM if the native build fails
    "--allow-incomplete-classpath",
  ),
  nativeImageAgentMerge := true,
)

val shared = project.settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Versions.tapir,
  ),
)

def full(p: Project) = p % "compile->compile;test->test"

val server = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % Versions.http4s,
      "org.http4s" %% "http4s-ember-server" % Versions.http4s,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.tapir,
      "ch.qos.logback" % "logback-classic" % Versions.logback,
      "org.http4s" %% "http4s-circe" % Versions.http4s % Test,
      "org.http4s" %% "http4s-client" % Versions.http4s % Test,
    ),
  )
  .dependsOn(full(shared))

val client = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % Versions.http4s,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % Versions.tapir,
      "ch.qos.logback" % "logback-classic" % Versions.logback,
    ),
    nativeImageSettings,
  )
  .enablePlugins(NativeImagePlugin)
  .dependsOn(full(shared))

val e2e = project
  .settings(commonSettings)
  .dependsOn(full(server), full(client))

val root = project
  .in(file("."))
  .settings(publish := {})
  .settings(commonSettings)
  .aggregate(shared, server, client, e2e)
