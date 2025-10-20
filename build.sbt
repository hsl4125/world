// Version definitions
val akkaVersion = "2.10.9"
val akkaHttpVersion = "10.7.2"
val logbackVersion = "1.5.19"
val sprayJsonVersion = "1.3.6"
val configVersion = "1.4.5"
val scalatestVersion = "3.2.19"
val jdkVersion = "25"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

// Dependency version management - Force override akka-pki version to resolve version conflicts
ThisBuild / dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-pki" % akkaVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "world",
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "org.slf4j" % "slf4j-api" % "2.0.16",
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      
      // JSON processing
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "io.spray" %% "spray-json" % sprayJsonVersion,
      
      // Configuration management
      "com.typesafe" % "config" % configVersion,
      
      // Test dependencies
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    ),
    
    // JDK 25 settings
    javacOptions ++= Seq(
      "-source", jdkVersion,
      "-target", jdkVersion
    ),
    
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )
  )

