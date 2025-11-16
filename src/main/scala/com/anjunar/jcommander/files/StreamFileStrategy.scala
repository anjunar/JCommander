package com.anjunar.jcommander.files

import com.anjunar.jcommander.utils.ProgressListener
import org.apache.commons.vfs2.FileObject

import java.nio.file.Path

trait StreamFileStrategy {

  def process(sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener) : Unit

}
