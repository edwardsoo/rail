import sbt._

object Version {
  val akka         = "2.3.4"
  val logback      = "1.1.2"
  val scala        = "2.11.1"
  val scalaParsers = "1.0.1"
  val scalaTest    = "2.2.0"
}

object Library {
  val akkaActor      = "com.typesafe.akka"      %% "akka-actor"               % Version.akka
  val akkaSlf4j      = "com.typesafe.akka"      %% "akka-slf4j"               % Version.akka
  val akkaTestkit    = "com.typesafe.akka"      %% "akka-testkit"             % Version.akka
  val logbackClassic = "ch.qos.logback"         %  "logback-classic"          % Version.logback
  val scalaParsers   = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.scalaParsers
  val scalaTest      = "org.scalatest"          %% "scalatest"                % Version.scalaTest
}

object Dependencies {

  import Library._

  val hakkyHour = List(
    akkaActor,
    akkaSlf4j,
    logbackClassic,
    scalaParsers,
    akkaTestkit % "test",
    scalaTest   % "test"
  )
}
