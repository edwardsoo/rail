import com.typesafe.sbt.SbtScalariform._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

object Common {

  val settings =
    scalariformSettings ++
    List(
      // Core settings
      organization := "com.typesafe.training",
      version := "4.0.0",
      scalaVersion := Version.scala,
      crossScalaVersions := List(scalaVersion.value),
      scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.6",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
      unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(PreserveDanglingCloseParenthesis, true),
      // Eclipse settings
      EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
      EclipseKeys.eclipseOutput := Some(".target"),
      EclipseKeys.withSource := true
    )
}
