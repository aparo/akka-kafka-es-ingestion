name := "akka-kafka-es-ingestion"

version := "1.0"

scalaVersion := "2.12.3"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.17",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.11",
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % "5.5.3",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "5.5.3",
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % "5.5.3",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

parallelExecution in Test := false

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
)
