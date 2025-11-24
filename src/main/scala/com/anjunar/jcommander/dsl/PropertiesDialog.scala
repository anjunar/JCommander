package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.{gridX, gridY}
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, NodeBuilder, Ref}
import com.anjunar.javafx.scene.control.checkbox.IsCheckBox.{allowIndeterminate, indeterminate, selected}
import com.anjunar.javafx.scene.control.textField.promptText
import com.anjunar.javafx.scene.control.{button, checkbox, label, textField}
import com.anjunar.javafx.scene.layout.gridPane.{hgap, vgap}
import com.anjunar.javafx.scene.layout.{gridPane, hbox, vbox}
import com.anjunar.javafx.scene.window.IsWindow.close
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import javafx.geometry.Pos
import javafx.scene.Node
import scalafx.geometry.Insets

import java.io.File
import java.nio.file.attribute.{PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Paths}
import scala.sys.process.stringSeqToProcess

class PropertiesDialog(files: Seq[String]) extends ElementBuilder[Window[Unit]] {
  
  lazy val node: Window[Unit] = {
    val ownerR = Ref[checkbox]()
    val ownerW = Ref[checkbox]()
    val ownerX = Ref[checkbox]()
    

    val groupR = Ref[checkbox]()
    val groupW = Ref[checkbox]()
    val groupX = Ref[checkbox]()

    val otherR = Ref[checkbox]()
    val otherW = Ref[checkbox]()
    val otherX = Ref[checkbox]()

    val octal = Ref[textField]()
    val ownerField = Ref[textField]()
    val groupField = Ref[textField]()
    val recursive = Ref[checkbox]()
    val applyButton = Ref[button]()

    def setState(cb: Ref[checkbox], values: Seq[Boolean]): Unit =
      cb {
        allowIndeterminate = true
        val allTrue = values.forall(_ == true)
        val allFalse = values.forall(_ == false)
        if allTrue then
          indeterminate = false
          selected = true
        else if allFalse then
          indeterminate = false
          selected = false
        else
          indeterminate = true
      }

    def readAllPermissions(fs: Seq[String]): Seq[String] = fs.map(readPermissions)

    def applyMixedPermissions(perms: Seq[String]): Unit =
      def col(i: Int): Seq[Boolean] = perms.map(_.charAt(i) != '-')

      setState(ownerR, col(0))
      setState(ownerW, col(1))
      setState(ownerX, col(2))
      setState(groupR, col(3))
      setState(groupW, col(4))
      setState(groupX, col(5))
      setState(otherR, col(6))
      setState(otherW, col(7))
      setState(otherX, col(8))

    def clearIndeterminateAll(): Unit =
      Seq(ownerR, ownerW, ownerX,
        groupR, groupW, groupX,
        otherR, otherW, otherX).foreach { cb =>
        cb {
          allowIndeterminate = true
          indeterminate = false
        }
      }

    def applyPermissionsToChecks(perm: String): Unit =
      clearIndeterminateAll()
      ownerR {
        selected = perm.charAt(0) == 'r'
      }
      ownerW {
        selected = perm.charAt(1) == 'w'
      }
      ownerX {
        selected = perm.charAt(2) == 'x'
      }
      groupR {
        selected = perm.charAt(3) == 'r'
      }
      groupW {
        selected = perm.charAt(4) == 'w'
      }
      groupX {
        selected = perm.charAt(5) == 'x'
      }
      otherR {
        selected = perm.charAt(6) == 'r'
      }
      otherW {
        selected = perm.charAt(7) == 'w'
      }
      otherX {
        selected = perm.charAt(8) == 'x'
      }
      updateFromChecks()

    def installMixedFix(cb: Ref[checkbox]): Unit =
      cb {
        onAction = _ => {
          if indeterminate then
            indeterminate = false
            selected = true
          updateFromChecks()
        }
      }

    def updateFromChecks(): Unit =
      val anyIndeterminate =
        Seq(ownerR, ownerW, ownerX,
          groupR, groupW, groupX,
          otherR, otherW, otherX).exists(flag => indeterminate(using flag.get))
      if anyIndeterminate then {
        octal {
          text = ""
        }
        ()
      } else
        val o =
          (selected(using ownerR.get), selected(using ownerW.get), selected(using ownerX.get)) match
            case (true, true, true) => 7
            case (true, true, false) => 6
            case (true, false, true) => 5
            case (true, false, false) => 4
            case (false, true, true) => 3
            case (false, true, false) => 2
            case (false, false, true) => 1
            case _ => 0

        val g =
          (selected(using groupR.get), selected(using groupW.get), selected(using groupX.get)) match
            case (true, true, true) => 7
            case (true, true, false) => 6
            case (true, false, true) => 5
            case (true, false, false) => 4
            case (false, true, true) => 3
            case (false, true, false) => 2
            case (false, false, true) => 1
            case _ => 0

        val ot =
          (selected(using otherR.get), selected(using otherW.get), selected(using otherX.get)) match
            case (true, true, true) => 7
            case (true, true, false) => 6
            case (true, false, true) => 5
            case (true, false, false) => 4
            case (false, true, true) => 3
            case (false, true, false) => 2
            case (false, false, true) => 1
            case _ => 0

        octal {
          text = s"$o$g$ot"
        }

    def computeFinalMode(old: String): String =
      def bit(cbR: Ref[checkbox], cbW: Ref[checkbox], cbX: Ref[checkbox], base: String): String =
        val r =
          if indeterminate(using cbR.get) then base.charAt(0)
          else if selected(using cbR.get) then 'r' else '-'
        val w =
          if indeterminate(using cbW.get) then base.charAt(1)
          else if selected(using cbW.get) then 'w' else '-'
        val x =
          if indeterminate(using cbX.get) then base.charAt(2)
          else if selected(using cbX.get) then 'x' else '-'
        s"$r$w$x"

      bit(ownerR, ownerW, ownerX, old.substring(0, 3)) +
        bit(groupR, groupW, groupX, old.substring(3, 6)) +
        bit(otherR, otherW, otherX, old.substring(6, 9))

    def validateOwner(n: String): Boolean =
      if n.trim.isEmpty then true else Seq("id", "-u", n.trim).! == 0

    def validateGroup(n: String): Boolean =
      if n.trim.isEmpty then true else Seq("getent", "group", n.trim).! == 0

    def readPermissions(path: String): String = {
      val perms = Files.getPosixFilePermissions(Paths.get(path))
      PosixFilePermissions.toString(perms)
    }

    def rwxToOctal(perm: String): String = {
      def v(c: Char, b: Int) = if (c != '-') b else 0

      val u = v(perm(0), 4) + v(perm(1), 2) + v(perm(2), 1)
      val g = v(perm(3), 4) + v(perm(4), 2) + v(perm(5), 1)
      val o = v(perm(6), 4) + v(perm(7), 2) + v(perm(8), 1)

      s"$u$g$o"
    }

    def showError(msg: String): Unit =
      component[Window[Unit]] {
        window() {
          header() {
            label() {
              text = "Error"
            }
          }
          vbox() {
            label() {
              text = "Wrong input"
            }
            label() {
              text = msg
            }
            button() {
              text = "OK"
              onAction = _ => close()
            }
          }
        }
      }

    val labelText =
      if files.size == 1 then new File(files.head).getName
      else s"${files.size} files"

    component[Window[Unit]] {
      window(400) {

        header() {
          label() {
            text = "Properties"
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
          }
        }

        vbox() {
          spacing = 12
          padding = Insets(14, 18, 14, 18)

          label() {
            text = labelText
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
            padding = Insets(0,0,4,0)
          }

          label() {
            text = "Rights"
            style = "-fx-font-size: 13.5px; -fx-font-weight: bold;"
            padding = Insets(8,0,0,0)
          }

          gridPane() {
            hgap = 20
            vgap = 8
            padding = Insets(4, 0, 10, 0)

            label() {
              gridX = 0
              gridY = 0
              text = "Owner"
              style = "-fx-font-size: 12.5px;"
            }

            hbox() {
              gridX = 1
              gridY = 0
              spacing = 8
              checkbox(ownerR) { text = "r" }
              checkbox(ownerW) { text = "w" }
              checkbox(ownerX) { text = "x" }
            }

            label() {
              gridX = 0
              gridY = 1
              text = "Group"
              style = "-fx-font-size: 12.5px;"
            }

            hbox() {
              gridX = 1
              gridY = 1
              spacing = 8
              checkbox(groupR) { text = "r" }
              checkbox(groupW) { text = "w" }
              checkbox(groupX) { text = "x" }
            }

            label() {
              gridX = 0
              gridY = 2
              text = "Other"
              style = "-fx-font-size: 12.5px;"
            }

            hbox() {
              gridX = 1
              gridY = 2
              spacing = 8
              checkbox(otherR) { text = "r" }
              checkbox(otherW) { text = "w" }
              checkbox(otherX) { text = "x" }
            }
          }

          label() {
            text = "Octal"
            style = "-fx-font-size: 13.5px; -fx-font-weight: bold;"
            padding = Insets(4,0,0,0)
          }

          textField(octal) {
            text = ""
            prefWidth = 80
            style = "-fx-font-size: 13px;"
          }

          label() {
            text = "Owner and Groups"
            style = "-fx-font-size: 13.5px; -fx-font-weight: bold;"
            padding = Insets(8,0,0,0)
          }

          gridPane() {
            hgap = 20
            vgap = 10
            padding = Insets(4, 0, 10, 0)

            label() {
              gridX = 0
              gridY = 0
              text = "Owner"
            }
            textField(ownerField) {
              gridX = 1
              gridY = 0
              promptText = "owner"
              style = "-fx-font-size: 13px;"
              prefWidth = 180
            }

            label() {
              gridX = 0
              gridY = 1
              text = "Group"
            }
            textField(groupField) {
              gridX = 1
              gridY = 1
              promptText = "group"
              style = "-fx-font-size: 13px;"
              prefWidth = 180
            }
          }

          checkbox(recursive) {
            text = "Apply to subdirectories"
            padding = Insets(4,0,0,0)
          }

          button(applyButton) {
            text = "Apply"
            onAction = _ => {
              val oct = text(using octal.get)
              if oct.nonEmpty && !oct.matches("[0-7]{3}") then
                showError("Octal-Value is wrong")
              else
                val ownerName = text(using ownerField.get).trim
                val groupName = text(using groupField.get).trim

                if !validateOwner(ownerName) then
                  showError("Owner does not exist.")
                else if !validateGroup(groupName) then
                  showError("Group does not exist.")
                else
                  new Thread(() => {
                    files.foreach { f =>
                      val current = readPermissions(f)
                      val newPerm = computeFinalMode(current)
                      val newOct = rwxToOctal(newPerm)

                      recursive {
                        if selected then
                          Seq("pkexec", "chmod", "-R", newOct, f).!
                        else
                          Seq("pkexec", "chmod", newOct, f).!
                      }

                      if ownerName.nonEmpty || groupName.nonEmpty then
                        val spec =
                          if ownerName.nonEmpty && groupName.nonEmpty then s"$ownerName:$groupName"
                          else if ownerName.nonEmpty then ownerName
                          else ":" + groupName

                        recursive {
                          if selected then
                            Seq("pkexec", "chown", "-R", spec, f).!
                          else
                            Seq("pkexec", "chown", spec, f).!
                        }
                    }
                    ()
                  }).start()
                  close()
            }
          }

          Seq(ownerR, ownerW, ownerX,
            groupR, groupW, groupX,
            otherR, otherW, otherX).foreach(installMixedFix)
        }

        if files.size == 1 then
          val p = readPermissions(files.head)
          applyPermissionsToChecks(p)
          octal {
            text = rwxToOctal(p)
          }
          val path = Paths.get(files.head)
          val attrs = Files.readAttributes(path, classOf[PosixFileAttributes])
          ownerField {
            text = attrs.owner().getName
          }
          groupField {
            text = attrs.group().getName
          }
        else
          val perms = readAllPermissions(files)
          applyMixedPermissions(perms)

          val owners = files.map { f =>
            val p = Paths.get(f)
            val attrs = Files.readAttributes(p, classOf[PosixFileAttributes])
            attrs.owner().getName
          }

          val groups = files.map { f =>
            val p = Paths.get(f)
            val attrs = Files.readAttributes(p, classOf[PosixFileAttributes])
            attrs.group().getName
          }

          if owners.distinct.size == 1 then
            ownerField {
              text = owners.head
            }
          else
            ownerField {
              text = ""
            }

          if groups.distinct.size == 1 then
            groupField {
              text = groups.head
            }
          else
            groupField {
              text = ""
            }

          octal {
            text = ""
          }
      }
    }
  }


  override def build(): Window[Unit] = node
}

object PropertiesDialog {

  def apply[T](files: Seq[String])(body: (PropertiesDialog, BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): Window[Unit] =
    DSL.create[Window[Unit], PropertiesDialog](Ref(), new PropertiesDialog(files))(body)

}
