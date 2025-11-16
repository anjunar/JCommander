package com.anjunar.jcommander.utils

import org.apache.commons.vfs2.{FileObject, FileType}

import java.io.{InputStream, OutputStream}

object VFSUtils {

  def copyMultiple(sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener): Unit = {
    sources.foreach(src => copyRecursive(src, destDir, listener))
  }

  def moveMultiple(sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener): Unit = {
    sources.foreach(src => moveRecursive(src, destDir, listener))
  }

  def deleteMultiple(sources: Seq[FileObject], listener: ProgressListener): Unit = {
    sources.foreach(src => deleteRecursive(src, listener))
  }

  def copyStream(src: FileObject, dest: FileObject, listener: ProgressListener): Unit = {
    val in: InputStream = src.getContent.getInputStream
    val out: OutputStream = dest.getContent.getOutputStream
    val totalBytes = src.getContent.getSize
    val buffer = new Array[Byte](16 * 1024)

    var bytesCopied: Long = 0
    var read = 0

    while ({
      read = in.read(buffer)
      read != -1
    }) {
      out.write(buffer, 0, read)
      bytesCopied += read
      listener.onFileProgress(src, bytesCopied, totalBytes)
    }

    in.close()
    out.close()
  }

  def copyRecursive(src: FileObject, destDir: FileObject, listener: ProgressListener): Unit = {
    src.getType match {
      case FileType.FILE =>
        val destFile = destDir.resolveFile(src.getName.getBaseName)
        copyStream(src, destFile, listener)

      case FileType.FOLDER =>
        val newDestDir = destDir.resolveFile(src.getName.getBaseName)
        if (!newDestDir.exists()) newDestDir.createFolder()
        src.getChildren.foreach(child => copyRecursive(child, newDestDir, listener))

      case _ =>
    }
  }

  def moveRecursive(src: FileObject, destDir: FileObject, listener: ProgressListener): Unit = {
    src.getType match {
      case FileType.FILE =>
        val destFile = destDir.resolveFile(src.getName.getBaseName)
        copyStream(src, destFile, listener)
        src.delete()

      case FileType.FOLDER =>
        val newDestDir = destDir.resolveFile(src.getName.getBaseName)
        if (!newDestDir.exists()) newDestDir.createFolder()
        src.getChildren.foreach(c => moveRecursive(c, newDestDir, listener))
        src.delete()

      case _ =>
    }
  }

  def deleteRecursive(src: FileObject, listener: ProgressListener): Unit = {
    src.getType match {
      case FileType.FILE =>
        val size = src.getContent.getSize
        listener.onFileProgress(src, size, size)
        src.delete()

      case FileType.FOLDER =>
        src.getChildren.foreach(c => deleteRecursive(c, listener))
        src.delete()

      case _ =>
    }
  }

}
