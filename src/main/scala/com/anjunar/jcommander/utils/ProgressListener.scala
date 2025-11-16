package com.anjunar.jcommander.utils

import org.apache.commons.vfs2.FileObject

trait ProgressListener {

  def onFileProgress(file: FileObject, bytesCopied: Long, totalBytes: Long): Unit
  
}
