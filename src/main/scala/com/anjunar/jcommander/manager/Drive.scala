package com.anjunar.jcommander.manager

import java.io.File

case class Drive(name: String,
                 file: File,
                 mounted: Boolean,
                 mountable: Boolean,
                 device: Option[String] = None,
                 fsType: Option[String] = None)