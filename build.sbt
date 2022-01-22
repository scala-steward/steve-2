ThisBuild / scalaVersion := "3.1.0"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

val commonSettings = Seq(
  scalacOptions -= "-Xfatal-warnings",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.2.9",
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.5" % Test
  )
)

val shared = project.settings(commonSettings)

val server = project.settings(commonSettings).dependsOn(shared)

val client = project.settings(commonSettings).dependsOn(shared)

val root = project
  .in(file("."))
  .settings(publish := {})
  .settings(commonSettings)
  .aggregate(shared, server, client)
