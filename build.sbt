ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "asmd-project",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.1",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.7",
      "io.github.cdimascio" % "java-dotenv" % "5.2.2",
      "org.playframework" %% "play-json" % "3.0.4",
      ("de.sciss" %% "scala-chart" % "0.8.0").cross(CrossVersion.for2_13Use3),
      // Cucumber dependencies for BDD testing
      "io.cucumber" % "cucumber-java" % "7.18.0" % Test,
      "io.cucumber" % "cucumber-junit" % "7.18.0" % Test,
      "io.cucumber" % "cucumber-scala_2.13" % "8.20.0" % Test,
      "junit" % "junit" % "4.13.2" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test
    ),
  )
