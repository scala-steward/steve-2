ThisBuild / scalaVersion := "3.1.1"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

val commonSettings = Seq(
  scalacOptions -= "-Xfatal-warnings",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.5",
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
  ),
)

val shared = project.settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.19.3",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.19.3",
  ),
)

val server = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.23.9",
      "org.http4s" %% "http4s-ember-server" % "0.23.9",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.19.3",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
    ),
  )
  .dependsOn(shared)

val client = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.9",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % "0.19.3",
    ),
  )
  .dependsOn(shared)

val root = project
  .in(file("."))
  .settings(publish := {})
  .settings(commonSettings)
  .aggregate(shared, server, client)
