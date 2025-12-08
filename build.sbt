import scala.collection.Seq

ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.7.3"

ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, UniversalPlugin, JlinkPlugin, JDKPackagerPlugin)
  .settings(
    name := "JCommander",
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "33.5.0-jre",
      "ch.qos.logback" % "logback-classic" % "1.5.20",
      "org.slf4j" % "slf4j-api" % "2.0.17",
      "org.slf4j" % "jul-to-slf4j" % "2.0.17",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.20.1",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.20.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.20.1",
      "com.jcraft" % "jsch" % "0.1.55",
      "com.github.oshi" % "oshi-core" % "6.9.1",
      "commons-net" % "commons-net" % "3.12.0",
      "org.apache.commons" % "commons-vfs2" % "2.10.0",
      "org.apache.commons" % "commons-compress" % "1.28.0",
      "org.kordamp.ikonli" % "ikonli-javafx" % "12.4.0",
      "org.kordamp.ikonli" % "ikonli-materialdesign2-pack" % "12.4.0",
      "net.java.dev.jna" % "jna" % "5.18.1"
    ),
    libraryDependencies ++= {
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux") => "linux"
        case n if n.startsWith("Mac") => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "25" classifier osName)
    },
    Compile / mainClass := Some("com.anjunar.jcommander.Launcher"),

    Universal / packageName := "jcommander",

    packageBin / packageOptions += {
      val cp = (Compile / dependencyClasspath).value.map(_.data.getName)

      val classPathString = cp.map(jar => s"lib/$jar").mkString(" ")

      Package.ManifestAttributes(
        "Main-Class" -> "com.anjunar.jcommander.Launcher",
        "Class-Path" -> classPathString
      )
    },

    Universal / mappings := {
      val stageDir = (Compile / packageBin).value
      val libDir = (Universal / mappings).value.filterNot(_._2.contains("lib/"))

      val mainJarMapping = stageDir -> stageDir.getName

      val deps = (Compile / dependencyClasspath).value.map { dep =>
        dep.data -> s"lib/${dep.data.getName}"
      }

      val distDir = baseDirectory.value / "target" / "dist"
      val distMappings =
        if (distDir.exists()) (distDir ** "*").get.map(f => f -> s"dist/${f.relativeTo(distDir).get.getPath}")
        else Seq.empty

      mainJarMapping +: (deps ++ distMappings)
    }
  )

lazy val jpackage = taskKey[Unit]("Create installer with jpackage")

jpackage := {
  val log = streams.value.log

  val javaHome = sys.props("java.home")
  val jpackageExe =
    if (scala.util.Properties.isWin) s"$javaHome\\bin\\jpackage.exe"
    else s"$javaHome/bin/jpackage"

  val stageDir = (Universal / stage).value
  val mainJar = (stageDir / "jcommander_3-1.0.0.jar")

  log.info(s"Stage dir  = ${stageDir.getAbsolutePath}")
  log.info(s"Main JAR   = ${mainJar.getAbsolutePath}")
  log.info(s"jpackage   = $jpackageExe")

  if (!mainJar.exists())
    sys.error(s"Main jar not found: $mainJar")

  val outputDir = (Compile / target).value / "jpackage"
  IO.createDirectory(outputDir)

  val cmd = Seq(
    jpackageExe,
    "--type", "exe",
    "--name", "JCommander",
    "--input", stageDir.getAbsolutePath,
    "--main-jar", mainJar.getName,
    "--main-class", "com.anjunar.jcommander.Launcher",
    "--dest", outputDir.getAbsolutePath,
    "--icon", "src/main/resources/icon.ico",
    "--win-menu",
    "--win-shortcut",
    "--win-dir-chooser"
  )

  log.info("Running: " + cmd.mkString(" "))

  val exit = sys.process.Process(cmd).!
  if (exit != 0) sys.error(s"jpackage failed with exit code $exit")
}
