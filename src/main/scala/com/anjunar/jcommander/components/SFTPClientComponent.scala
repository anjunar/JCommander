package com.anjunar.jcommander.components

import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import jakarta.enterprise.context.ApplicationScoped
import javafx.concurrent.Task
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider
import org.apache.commons.vfs2.{FileObject, FileSystemManager}
import scalafx.scene.control.{Button, Label, PasswordField, TextField}
import scalafx.scene.layout.{HBox, Priority, VBox}

class SFTPClientComponent extends Component[ThemedDialog[FileSystemManager]] {

  val manager = FileSystemManagerBuilder.build()

  var base: FileObject = _

  def resolve(path: String): FileObject =
    manager.resolveFile(base, path)

  override val node: ThemedDialog[FileSystemManager] = new ThemedDialog[FileSystemManager] {

    private val hostField = new TextField {
      text = "patricks-mbp.fritz.box"
      promptText = "Host"
    }
    private val portField = new TextField {
      promptText = "Port"; text = "22"
    }
    private val userField = new TextField {
      text = "patrick"
      promptText = "Username"
    }
    private val passField = new PasswordField {
      text = "cubase"
      promptText = "Password"
    }
    private val connectButton = new Button("Connect")
    private val statusLabel = new Label("Not connected")

    title = "SFTP Client"

    dialogPane.content = new VBox(10) {
      children = Seq(
        hostField,
        portField,
        userField,
        passField,
        new HBox(10) {
          children = Seq(connectButton)
        },
        statusLabel
      )
      VBox.setVgrow(statusLabel, Priority.Always)
    }

    connectButton.setOnAction(_ => {
      val host = hostField.text.value
      val port = portField.text.value
      val user = userField.text.value
      val pass = passField.text.value

      val task = new Task[FileSystemManager] {
        override def call(): FileSystemManager = {
          val uri = s"sftp://$user:$pass@$host:$port/"
          val remoteFile = manager.resolveFile(uri)
          if (!remoteFile.exists()) throw new Exception("Connection failed or directory empty")
          manager
        }
      }

      task.setOnSucceeded(_ => {
        statusLabel.text = "Connected successfully!"
        setResult(task.get())
      })

      task.setOnFailed(_ => {
        statusLabel.text = s"Error: ${task.getException.getMessage}"
      })

      new Thread(task).start()
    })
  }
}
