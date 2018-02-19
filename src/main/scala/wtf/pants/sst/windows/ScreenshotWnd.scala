package wtf.pants.sst.windows

import java.awt
import java.awt.image.BufferedImage
import java.awt.{GraphicsEnvironment, Robot}
import javafx.scene.input.KeyCode

import wtf.pants.sst.tools.ImageUploader

import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.Scene
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.image.{Image, WritableImage}
import scalafx.scene.paint.Color._
import scalafx.stage.{Stage, StageStyle}

class ScreenshotWnd(imageUploader: ImageUploader) extends Stage {

  var mouseStartX = 0.0d
  var mouseStartY = 0.0d

  val bounds: awt.Rectangle = getScreenBounds

  val canvas = new Canvas() {
    x = bounds.x
    y = bounds.y
    width = bounds.width
    height = bounds.height
  }

  x = bounds.x
  y = bounds.y
  width = bounds.width
  height = bounds.height

  initStyle(StageStyle.Undecorated)

  val screenshot: BufferedImage = new Robot().createScreenCapture(bounds)
  val image: WritableImage = SwingFXUtils.toFXImage(screenshot, null)

  //When the screenshot window appears, update the canvas and force window to front
  onShown = _ => {
    updateCanvas(canvas.graphicsContext2D, image)
    toFront()
  }

  scene = new Scene {
    fill = Orange
    getChildren.add(canvas)
    initListeners(this)
    alwaysOnTop = true
  }

  def initListeners(scene: Scene): Unit = {
    scene.onMousePressed = (event) => {
      mouseStartX = event.getX
      mouseStartY = event.getY
    }

    scene.onMouseMoved = (event) => {
      updateCanvas(canvas.graphicsContext2D, image, mouse = (event.getX, event.getY))
    }

    scene.onMouseDragged = (event) => {
      updateCanvas(canvas.graphicsContext2D, image, mouse = (event.getX, event.getY), dragging = true)
    }

    scene.onMouseReleased = event => {
      close()

      val region = screenshot.getSubimage(
        mouseStartX.asInstanceOf[Int],
        mouseStartY.asInstanceOf[Int],
        (event.getX - mouseStartX).asInstanceOf[Int],
        (event.getY - mouseStartY).asInstanceOf[Int])

      val url = imageUploader.uploadImage(region, clipboard = true)
      println(s"${if (url._1) "Success" else "Error"}: ${url._2}")
    }

    scene.onKeyPressed = event => {
      if (event.getCode == KeyCode.ESCAPE)
        hide()
    }
  }

  def showScreenshotWindow(screenshot: Image): Unit = {
    updateCanvas(canvas.graphicsContext2D, screenshot)
  }

  /**
    * Renders everything for the screenshotting
    *
    * @param gc         The canvas' GraphicsContext to render for
    * @param screenshot An image of the users entire screen
    * @param mouse      (optional) The user's current mouse position
    * @param dragging   (optional) Whether or not the user is currently dragging their mouse
    */
  def updateCanvas(gc: GraphicsContext, screenshot: Image, mouse: (Double, Double) = (0, 0), dragging: Boolean = false): Unit = {
    val mouseX = mouse._1
    val mouseY = mouse._2

    //Background image, the screenshot of all the monitors
    gc.drawImage(screenshot, 0, 0)

    //Transparent gray overlay to cover the background image
    gc.setFill(rgb(0, 0, 0, .4))
    gc.fillRect(0, 0, screenshot.getWidth, screenshot.getHeight)

    //If the user is dragging their mouse draw the image selection without the overlay
    if (dragging) {
      gc.drawImage(screenshot, mouseStartX, mouseStartY, mouseX - mouseStartX, mouseY - mouseStartY,
        mouseStartX, mouseStartY, mouseX - mouseStartX, mouseY - mouseStartY)
    }

    if (dragging) {
      drawSelectionRect(gc, mouse)
    }

    drawCrosshair(gc, image, mouse)
  }

  /**
    * Draws a crosshair that follows the mouse in the screenshot window
    *
    * @param gc         The canvas' GraphicsContext to render for
    * @param screenshot An image of the users entire screen
    * @param mouse      The user's current mouse position
    */
  private def drawCrosshair(gc: GraphicsContext, screenshot: Image, mouse: (Double, Double)): Unit = {
    val mouseX = mouse._1
    val mouseY = mouse._2

    gc.setLineWidth(2)
    gc.setLineDashes(0)
    gc.setStroke(Black)
    gc.strokeLine(0, mouseY, screenshot.getWidth, mouseY)
    gc.strokeLine(mouseX, 0, mouseX, screenshot.getHeight)

    gc.setStroke(White)
    gc.setLineDashes(7)
    gc.strokeLine(0, mouseY, screenshot.getWidth, mouseY)
    gc.strokeLine(mouseX, 0, mouseX, screenshot.getHeight)
  }

  /**
    * Draws a rectangle the show the selection the user is making while dragging
    *
    * @param gc    The canvas' GraphicsContext to render for
    * @param mouse The user's current mouse position
    */
  private def drawSelectionRect(gc: GraphicsContext, mouse: (Double, Double)): Unit = {
    gc.setLineWidth(2)
    gc.setLineDashes(0)
    gc.setStroke(Black)
    gc.strokeRect(mouseStartX, mouseStartY, mouse._1 - mouseStartX, mouse._2 - mouseStartY)

    gc.setStroke(White)
    gc.setLineDashes(7)
    gc.strokeRect(mouseStartX, mouseStartY, mouse._1 - mouseStartX, mouse._2 - mouseStartY)
  }

  //todo: maybe make this a tuple? Might be too much work
  def getScreenBounds: java.awt.Rectangle = {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
    val devices = ge.getScreenDevices

    devices.flatMap(device => device.getConfigurations)
      .map(cfg => cfg.getBounds)
      .reduceLeft((bounds, prev) => prev.union(bounds))
  }

}
