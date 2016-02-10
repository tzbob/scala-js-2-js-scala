name := "scala-js-2-js-scala"

version := "0.1-SNAPSHOT"

scalaOrganization := "org.scala-lang.virtualized"
scalaVersion := "2.11.2"

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
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  "org.scala-lang.virtualized" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang.virtualized" % "scala-library" % scalaVersion.value,
  "org.scala-lang.virtualized" % "scala-reflect" % scalaVersion.value,
  "EPFL" %% "lms" % "0.3-SNAPSHOT",
  "EPFL" %% "js-scala" % "0.4-SNAPSHOT",
  "org.scala-js" %% "scalajs-stubs" % "0.6.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
