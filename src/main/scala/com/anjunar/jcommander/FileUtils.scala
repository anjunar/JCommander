package com.anjunar.jcommander

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

object FileUtils {

  def listAllFilesRecursive(root: Path): Seq[Path] = {
    if (Files.notExists(root)) return Seq.empty

    Files.walk(root).toScala(Seq)
      .filter(Files.isRegularFile(_))
  }

  def listAllDirsRecursive(root: Path): Seq[Path] = {
    if (Files.notExists(root)) return Seq.empty

    Files.walk(root).toScala(Seq)
      .filter(Files.isDirectory(_))
  }

  def listAll(root: Path): Seq[Path] = {
    if (Files.notExists(root)) return Seq.empty

    Files.walk(root).toScala(Seq)
  }
}
