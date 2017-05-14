name := """alfred-manages-leave"""
organization := "build-something-new"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.1"
libraryDependencies += "com.geteventstore" %% "akka-persistence-eventstore" % "4.1.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "build-something-new.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "build-something-new.binders._"
