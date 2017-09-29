name := """play-java-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val dbDeps = Seq(
    "org.postgresql" % "postgresql" % "42.1.4",
    "com.h2database" % "h2" % "1.4.194"
  )

  val defaults = Seq(
    guice
  )

  val testDeps = Seq(
    "org.assertj" % "assertj-core" % "3.6.2" % Test,
    "org.awaitility" % "awaitility" % "2.0.0" % Test
  )

  dbDeps ++ defaults ++ testDeps
}

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

playEbeanModels in Compile := Seq("models.*")
