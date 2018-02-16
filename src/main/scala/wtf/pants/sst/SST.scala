package wtf.pants.sst

import java.awt._
import javax.swing.KeyStroke

import com.tulskiy.keymaster.common.Provider
import wtf.pants.sst.tools.ImageUploader
import wtf.pants.sst.windows.ScreenshotWnd

import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.paint.Color._

object SST extends JFXApp {

  private val popupMenu = new PopupMenu("SST")
  val uploader = new ImageUploader

  Platform.implicitExit = false

  stage = new JFXApp.PrimaryStage {
    setupSystemTray()
    setupKeybinds()

    title.value = "SST"
    width = 640
    height = 480

    scene = createScene()
  }

  /**
    * Creates the main scene for the window.
    *
    * @return
    */
  private def createScene(): Scene = {
    new Scene {
      fill = White
      getChildren.add(new Label("TODO"))
    }
  }

  private def setupKeybinds(): Unit = {
    val provider = Provider.getCurrentProvider(false)

    provider.register(KeyStroke.getKeyStroke("control shift A"), (_) => {
      Platform.runLater(() => {
        new ScreenshotWnd(uploader).show()
      })
    })
  }

  private def setupSystemTray(): Unit = {
    val tray: SystemTray = SystemTray.getSystemTray
    val image: Image = Toolkit.getDefaultToolkit.getImage("tray_icon.png")

    createMenuItem("Display GUI", () => {
      stage.show()
    })

    createMenuItem("Exit", () => {
      println("Exiting... bye!")
      System.exit(0)
    })

    tray.add(new TrayIcon(image, "SST", popupMenu))
  }

  private def createMenuItem(itemName: String, event: () => Unit): Unit = {
    val menuItem = new MenuItem(itemName) {
      addActionListener((_) => Platform.runLater(() => event.apply()))
    }
    popupMenu.add(menuItem)
  }
}
