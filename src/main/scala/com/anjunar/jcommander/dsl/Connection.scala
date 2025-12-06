package com.anjunar.jcommander.dsl

import org.apache.commons.vfs2.FileSystemManager

case class Connection(url : String, manager : FileSystemManager)
