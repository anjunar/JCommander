package com.anjunar.jcommander.utils

import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider

object FileSystemManagerBuilder {
  
  def build(): DefaultFileSystemManager = {
    val manager = new DefaultFileSystemManager()
    manager.addProvider("sftp", new SftpFileProvider())
    manager.addProvider("file", new org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider())
    manager.setCacheStrategy(org.apache.commons.vfs2.CacheStrategy.ON_CALL)
    manager.init()
    manager
  }

}
