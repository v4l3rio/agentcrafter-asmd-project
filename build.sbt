ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "asmd-project",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.12" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0",
      ("de.sciss" %% "scala-chart" % "0.8.0").cross(CrossVersion.for2_13Use3)
    ),
  )
