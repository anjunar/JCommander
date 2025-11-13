import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "JCommander",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.20",
      "org.slf4j" % "slf4j-api" % "2.0.17",
      "org.slf4j" % "jul-to-slf4j" % "2.0.17",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      "org.jboss.weld.se" % "weld-se-core" % "6.0.3.Final",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.20.1",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.20.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.20.1",
      "org.scalafx" %% "scalafx" % "24.0.2-R36",
      "io.methvin" % "directory-watcher" % "0.19.1"
    ),
    libraryDependencies ++= {
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux") => "linux"
        case n if n.startsWith("Mac") => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "24" classifier osName)
    }
  )
