package wtf.pants.sst.windows

import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javafx.scene.canvas.GraphicsContext
import javax.imageio.ImageIO

import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.image.Image
import scalafx.scene.paint.Color
import scalafx.stage.{Stage, StageStyle}

class ImageNotificationWnd(imgUrl: Option[String] = None, savedImage: Option[BufferedImage] = None) extends Stage {

  //todo: Not hardcoded, for config
  val MONITOR_NUMBER = 1

  initStyle(StageStyle.Undecorated)
  initStyle(StageStyle.Transparent)

  scene = new Scene {
    alwaysOnTop = true
    toFront()

    //todo: Not this, however it's for keeping aspect ratio and resizing with ease
    val bos = new ByteArrayOutputStream()

    val bufferedImage = savedImage.get
    ImageIO.write(savedImage.get, "png", bos)

    val imageWidth: Int = bufferedImage.getWidth
    val imageHeight: Int = bufferedImage.getHeight

    val requestWidth: Int = if (imageWidth > 340) 340 else imageWidth
    val requestHeight: Int = if (imageHeight > 340) 340 else imageHeight

    val image: Image = new Image(new ByteArrayInputStream(bos.toByteArray), requestWidth, requestHeight, true, true)

    val pos: (Int, Int) = prefWindowPosition(MONITOR_NUMBER)

    val offsetX = 40
    val offsetY = 40

    val windowX: Int = (pos._1 - image.width.value.asInstanceOf[Int]) - offsetX
    val windowY: Int = (pos._2 - image.height.value.asInstanceOf[Int]) - offsetY
    moveWindow(windowX, windowY)

    val canvas = new Canvas(requestWidth, requestHeight)
    addToCanvas(canvas.getGraphicsContext2D, image)

    getChildren.add(canvas)
    hideLater(2000)
  }

  /**
    * Renders the image and extras onto the canvas
    *
    * @param graphics Canvas' GraphicsContext
    * @param image    Image we're drawing to the canvas
    */
  def addToCanvas(graphics: GraphicsContext, image: Image): Unit = {
    graphics.drawImage(image, 0, 0)
    graphics.setStroke(Color.Black)
    graphics.setLineWidth(2)
    graphics.strokeRect(0, 0, image.getWidth, image.getHeight)
  }

  def moveWindow(x: Int, y: Int): Unit = {
    this.setX(x)
    this.setY(y)
  }

  /**
    * Gets the position we need on the monitor we want to display the window on
    *
    * @return Returns monitor's position on the computer
    */
  def prefWindowPosition(monitor: Int): (Int, Int) = {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
    val devices = ge.getScreenDevices

    val conf = devices(monitor).getConfigurations
    val bounds = conf(0).getBounds

    (bounds.x + bounds.width, bounds.y + bounds.height)
  }

  /**
    * After timeVisible the window will fade away
    *
    * @param timeVisible Time until the window disappears
    */
  def hideLater(timeVisible: Int): Unit = {
    new Thread(() => {
      Thread.sleep(timeVisible)
      println("Going transparent...")
      for (a <- (20 until 255).reverse) {
        Thread.sleep(10)
        Platform.runLater(() => opacity_=(a / 255.0D))
      }
      Platform.runLater(() => close())
    }).start()
  }
}