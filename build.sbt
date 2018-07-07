name := "akka-kafka-es-ingestion"

version := "1.1"

scalaVersion := "2.12.6"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.22",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.20",
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % "6.3.3",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.3.3",
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % "6.3.3",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

parallelExecution in Test := false

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)
