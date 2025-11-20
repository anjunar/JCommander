package com.anjunar.jcommander.manager

import com.typesafe.scalalogging.Logger
import oshi.SystemInfo

import java.io.{BufferedReader, File, InputStreamReader}
import scala.jdk.CollectionConverters.*

class DriveDetectionService {

  private val log = Logger[DriveDetectionService]

  def listDrives(): Seq[Drive] = {
    val os = System.getProperty("os.name").toLowerCase
    if (os.contains("linux")) linuxDrives()
    else if (os.contains("win")) windowsDrives()
    else if (os.contains("mac")) macDrives()
    else genericOshiDrives()
  }


  def mountDrive(drive: Drive): Option[Drive] = {
    val os = System.getProperty("os.name").toLowerCase
    if (!os.contains("linux")) Some(drive)
    else {
      drive.device match {
        case Some(dev) if !drive.mounted && drive.mountable =>
          mountDevice(dev) match {
            case Some(path) =>
              Some(drive.copy(file = new File(path), mounted = true, mountable = false))
            case None =>
              None
          }
        case _ => Some(drive)
      }
    }
  }

  private def windowsDrives(): Seq[Drive] = {
    val si = new SystemInfo()
    val fs = si.getOperatingSystem.getFileSystem
    fs.getFileStores.asScala.toSeq.flatMap { store =>
      val mount = Option(store.getMount).getOrElse("")
      if (mount.nonEmpty && mount.matches("^[A-Za-z]:\\\\$")) {
        Some(
          Drive(
            name = mount,
            file = new File(mount),
            mounted = true,
            mountable = false,
            device = None,
            fsType = Option(store.getType)
          )
        )
      } else {
        None
      }
    }
  }

  private def genericOshiDrives(): Seq[Drive] = {
    val si = new SystemInfo()
    val fs = si.getOperatingSystem.getFileSystem
    fs.getFileStores.asScala.toSeq.flatMap { store =>
      val mount = Option(store.getMount).getOrElse("")
      if (mount.nonEmpty) {
        Some(
          Drive(
            name = mount,
            file = new File(mount),
            mounted = true,
            mountable = false,
            device = None,
            fsType = Option(store.getType)
          )
        )
      } else {
        None
      }
    }
  }

  private def linuxDrives(): Seq[Drive] = {
    try {
      val pb = new ProcessBuilder("lsblk", "-pnro", "NAME,FSTYPE,PARTTYPE,PARTLABEL,MOUNTPOINT")
      pb.redirectErrorStream(true)
      val process = pb.start()
      val br = new BufferedReader(new InputStreamReader(process.getInputStream))
      val lines = Iterator.continually(br.readLine()).takeWhile(_ != null).toSeq
      process.waitFor()

      lines.flatMap { line =>
        val trimmed = line.trim
        if (trimmed.isEmpty) None
        else {
          val parts = trimmed.split("\\s+")
          if (parts.isEmpty) None
          else {
            val name = parts(0)
            if (!name.startsWith("/dev/")) None
            else if (name.startsWith("/dev/loop")) None
            else if (!name.matches(".+\\d+")) None
            else {
              val lower = trimmed.toLowerCase
              if (isSystemEntry(lower)) None
              else {
                val mountOpt = parts.lastOption.filter(_.startsWith("/"))
                val fstype =
                  if (parts.length > 1 && parts(1) != "-" && parts(1).nonEmpty) Some(parts(1)) else None
                val mounted = mountOpt.isDefined
                val path = mountOpt.getOrElse(name)
                Some(
                  Drive(
                    name = name,
                    file = new File(path),
                    mounted = mounted,
                    mountable = !mounted,
                    device = Some(name),
                    fsType = fstype
                  )
                )
              }
            }
          }
        }
      }
    } catch {
      case e: Exception =>
        log.error("Fehler bei lsblk", e)
        Seq.empty
    }
  }

  private def macDrives(): Seq[Drive] = {
    val si = new SystemInfo()
    val fs = si.getOperatingSystem.getFileSystem

    fs.getFileStores.asScala.toSeq.flatMap { store =>
      val mount = Option(store.getMount).getOrElse("").trim
      val typeName = Option(store.getType).getOrElse("").toLowerCase

      if (mount.isEmpty) None
      else if (!mount.startsWith("/Volumes/")) None
      else if (mount == "/Volumes") None
      else if (mount.contains("Preboot")) None
      else if (mount.contains("Update")) None
      else if (mount.contains("VM")) None
      else if (mount.contains("Recovery")) None
      else if (mount.contains("Data")) None
      else if (mount.contains("com.apple")) None
      else {
        Some(
          Drive(
            name = mount.replace("/Volumes/", ""),
            file = new File(mount),
            mounted = true,
            mountable = false,
            device = None,
            fsType = Some(typeName)
          )
        )
      }
    }
  }


  private def mountDevice(dev: String): Option[String] = {
    try {
      val pb = new ProcessBuilder("udisksctl", "mount", "-b", dev)
      pb.redirectErrorStream(true)
      val process = pb.start()
      val br = new BufferedReader(new InputStreamReader(process.getInputStream))
      val out = Iterator.continually(br.readLine()).takeWhile(_ != null).mkString(" ")
      process.waitFor()
      val atIdx = out.indexOf("at ")
      if (atIdx > 0) {
        val path = out.substring(atIdx + 3).replace(".", "").trim
        if (path.nonEmpty) Some(path) else None
      } else None
    } catch {
      case e: Exception =>
        log.error(s"Fehler beim Mounten von $dev", e)
        None
    }
  }

  private def isSystemEntry(lower: String): Boolean = {
    if (lower.contains("de94bba4")) return true
    if (lower.contains("ef00")) return true
    if (lower.contains("reserved")) return true
    if (lower.contains("efi")) return true
    if (lower.contains("esp")) return true
    if (lower.contains("boot")) return true
    if (lower.contains("bios")) return true
    if (lower.contains("recovery")) return true
    if (lower.contains("msftres")) return true
    if (lower.contains("hidden")) return true
    false
  }
}
