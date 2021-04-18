organization := "com.packtpub"

name := """elastic4s-sample"""

version := "0.0.5"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val elastic4sV = "7.12.0"
  val scalaTestV = "3.2.7"
  val Log4jVersion = "2.14.1"
  Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sV,
    // for the http client
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sV,
    // if you want to use reactive streams
    "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sV,
    // testing
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sV % "test",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.apache.logging.log4j" % "log4j-api" % Log4jVersion,
    "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
    "org.apache.logging.log4j" % "log4j-1.2-api" % Log4jVersion,
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}

