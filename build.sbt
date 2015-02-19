name := "scala-js-2-js-scala"

version := "0.1-SNAPSHOT"

scalaOrganization := "org.scala-lang.virtualized"
scalaVersion := "2.10.2"

isSnapshot := true
organization := "com.github.tzbob"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Yvirtualize"
)

libraryDependencies ++= Seq(
  "org.scala-lang.virtualized" % "scala-compiler" % "2.10.2",
  "org.scala-lang.virtualized" % "scala-library" % "2.10.2",
  "org.scala-lang.virtualized" % "scala-reflect" % "2.10.2",
  "EPFL" %% "lms" % "0.3-SNAPSHOT",
  "EPFL" %% "js-scala" % "0.4-SNAPSHOT",
  "org.scalamacros" %% "quasiquotes" % "2.1.0-M5",
  "org.scala-js" %% "scalajs-stubs" % "0.6.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
