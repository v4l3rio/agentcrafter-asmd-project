ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"

val mockitoVersion = "5.18.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(
    name := "asmd-project",

    libraryDependencies ++= Seq(
      "org.scalatest"                  %% "scalatest"        % "3.2.19" % Test,
      "org.scalacheck"                 %% "scalacheck"       % "1.18.1" % Test,
      "org.mockito"                     % "mockito-core"     % mockitoVersion % Test,
      "com.softwaremill.sttp.client4"  %% "core"             % "4.0.9",
      "io.github.cdimascio"             % "java-dotenv"      % "5.2.2",
      "org.playframework"              %% "play-json"        % "3.0.4",
      ("de.sciss" %% "scala-chart"      % "0.8.0").cross(CrossVersion.for2_13Use3),
      "io.cucumber"                     % "cucumber-java"    % "7.23.0" % Test,
      "io.cucumber"                     % "cucumber-junit"   % "7.23.0" % Test,
      "io.cucumber"                    %% "cucumber-scala"   % "8.28.0" % Test,
      "junit"                           % "junit"            % "4.13.2" % Test,
      "com.novocode"                    % "junit-interface"  % "0.11"   % Test
    ),

    testFrameworks += new TestFramework("io.cucumber.scala.ScalaDslTestFramework"),
    javaAgents += "org.mockito" % "mockito-core" % mockitoVersion % Test
  )