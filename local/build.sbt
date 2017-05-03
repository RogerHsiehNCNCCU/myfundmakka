name := "makka"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.8" withSources(),
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4" withSources(),
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4" withSources(),
  "org.yaml" % "snakeyaml" % "1.17" withSources(),
  "com.typesafe.akka" %% "akka-actor" % "2.4.0" withSources(),
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0" withSources(),
  "com.typesafe.akka" %% "akka-remote" % "2.4.14",
  "org.apache.kafka" % "kafka_2.11" % "0.8.2.2" withSources() excludeAll
    ExclusionRule("jline", "jline"),
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2" withSources() excludeAll
    ExclusionRule(organization = "org.scala-lang"),
  "com.typesafe.play" %% "play-json" % "2.5.4" withSources(),
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" withSources(),
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.54" withSources()
)

baseAssemblySettings
mainClass in assembly := Some("com.oring.smartcity.util.DummyPublisher")
assemblyMergeStrategy in assembly := {
  case PathList("lib", "static", "Windows", xs @ _*) => MergeStrategy.discard
  case PathList("lib", "static", "Mac OS X", xs @ _*) => MergeStrategy.discard
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

test in assembly := {}
fork in run := true