package wtf.pants.sst

import java.awt.{MenuItem, PopupMenu}
import javax.swing.KeyStroke

import com.tulskiy.keymaster.common.Provider
import wtf.pants.sst.config.{Config, Destination, JsonConfig}
import wtf.pants.sst.tools.ImageUploader
import wtf.pants.sst.windows.ScreenshotWnd

import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color._

object SST extends JFXApp {

  private val popupMenu = new PopupMenu("SST")

  val urlTxt = createTextField("https://my.website/upload")
  val fileTxt = createTextField("file")
  val argumentsTxt = createTextField("arg=something&key=SlvW$5las")

  val saveBtn = new Button("Save") {
    onMouseClicked_=(_ => println("i do nothing"))
  }

  val config = new Config().shekels
  println(config.mkString)
  val uploader = new ImageUploader(config.getOrElse(JsonConfig("", "", Map[String, String]())))

  Platform.implicitExit = false

  stage = new JFXApp.PrimaryStage {
    setupSystemTray()
    setupKeybinds()

    title.value = "SST"
    width = 640
    height = 480

    scene = createScene()


    onShown = _ => {
      toFront()
    }
  }

  /**
    * Creates the main scene for the window.
    *
    * @return
    */
  private def createScene(): Scene = {
    new Scene {
      fill = White
      val vbox = new VBox()
      vbox.children.addAll(urlTxt, fileTxt, argumentsTxt, saveBtn)
      getChildren.add(vbox)
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
    import java.awt._ //Keep this imported here

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

  private def createTextField(prompt: String, p_Width: Int = 300): TextField = {
    new TextField() {
      promptText = prompt
      prefWidth = p_Width
    }
  }
}
