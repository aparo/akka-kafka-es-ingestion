name := "akka-kafka-es-ingestion"

version := "1.1"

scalaVersion := "2.12.5"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.11",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.19",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.18",
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % "6.2.3",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.2.3",
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % "6.2.3",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

parallelExecution in Test := false

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)
