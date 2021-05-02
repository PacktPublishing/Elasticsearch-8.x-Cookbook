organization := "com.packtpub"

name := """alpakka-example"""

version := "0.0.5"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val scalaTestV = "3.2.8"
  Seq(
    "com.github.pathikrit" %% "better-files" % "3.9.1",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.2",
    "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % "2.0.2",
    "org.mongodb.scala" %% "mongo-scala-bson" % "2.9.0",
    "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo
)
