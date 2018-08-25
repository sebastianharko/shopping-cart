
name := "shopping-cart"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.21.0"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.14"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.89"

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.14"

libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % "2.5.14"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.4"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.14"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.14"

libraryDependencies +=  "com.typesafe.akka" %% "akka-slf4j" % "2.5.14"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.json4s" %% "json4s-core" % "3.6.0"

libraryDependencies +="org.json4s" %% "json4s-jackson" % "3.6.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.0"


enablePlugins (Cinnamon)

// Add the Cinnamon Agent for run and test
cinnamon in run := true
cinnamonLogLevel := "DEBUG"

libraryDependencies ++= Seq(
  // Use Coda Hale Metrics and Akka instrumentation
  Cinnamon.library.cinnamonCHMetrics3,
  Cinnamon.library.cinnamonJvmMetricsProducer,
  Cinnamon.library.cinnamonCHMetrics3ElasticsearchReporter,
  Cinnamon.library.cinnamonSlf4jMdc,
  Cinnamon.library.cinnamonAkka,
  Cinnamon.library.cinnamonAkkaHttp,
  Cinnamon.library.cinnamonAkkaStream)