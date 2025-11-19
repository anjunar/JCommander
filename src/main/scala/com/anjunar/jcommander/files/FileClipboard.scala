package com.anjunar.jcommander.files

import java.nio.file.Path
import scala.sys.process.stringSeqToProcess

object FileClipboard {
  private var copied: Seq[String] = Seq.empty

  def copy(path: String): Unit =
    copied = Seq(path)

  def copyMany(paths: Seq[String]): Unit =
    copied = paths

  def pasteToDirectory(dir: String): Unit =
    copied.foreach { src =>
      val name = Path.of(src).getFileName.toString
      val target = Path.of(dir, name).toString
      new Thread(() => { Seq("cp", "-r", src, target).!; () }).start()
    }
}
