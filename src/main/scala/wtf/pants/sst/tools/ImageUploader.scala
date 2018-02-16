package wtf.pants.sst.tools

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO

import org.apache.http.{HttpEntity, HttpResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class ImageUploader {
  //todo: Make this not hardcoded
  private val DEFAULT_URL = "https://your.website/upload"
  private val KEY = "blahblahblah"

  private val USER_AGENT = "scala-screenshot-tool"

  def saveImage(img: BufferedImage, fileName: String, format: String = "PNG", path: String = ""): Unit = {
    ImageIO.write(img, format, new File(s"${verifyPath(path)}$generateFileName.${format.toLowerCase}"))
  }

  def uploadImage(img: BufferedImage, format: String = "PNG", clipboard: Boolean = false): (Boolean, String) = {
    val http = HttpClients.createDefault
    val post = new HttpPost(DEFAULT_URL)

    val imageBytes = getImageBytes(img, format)

    val entity = MultipartEntityBuilder.create()
      .addBinaryBody("file", imageBytes, ContentType.IMAGE_PNG, "file.png")
      .addTextBody("key", KEY)
      .build()

    post.setEntity(entity)
    post.addHeader("user-agent", USER_AGENT)

    val responseBody = attemptRequest(http, post)

    val valid = isValidBody(responseBody)
    if (valid && clipboard) saveToClipboard(responseBody)

    (valid, responseBody)
  }

  private def attemptRequest(http: HttpClient, post: HttpPost): String = {
    try {
      EntityUtils.toString(http.execute(post).getEntity)
    } catch {
      case e: Exception => e.getMessage
    }
  }

  /**
    * Generates a file name based on time, (example: 2018-10-3_18-58-11)
    *
    * @return Returns a new filename
    */
  private def generateFileName = {
    val dateFormat = new SimpleDateFormat("yyyy-HH-dd_hh-mm-ss")
    dateFormat.format(new Date)
  }

  /**
    * Ensures if needed that the path ends with a '/'
    *
    * @param path The path to verify
    * @return Returns the same or a fixed path
    */
  private def verifyPath(path: String): String = {
    if (path.isEmpty || path.endsWith("/"))
      path
    else
      path + "/"
  }

  /**
    * Verifies that the response content is an actual URL and not something else
    *
    * @param body The response body
    * @return Returns true if valid
    */
  private def isValidBody(body: String): Boolean = {
    body.startsWith("http")
  }

  /**
    * Gets the bytes from a BufferedImage
    *
    * @param img    BufferedImage Instance
    * @param format Image format (png, gif, jpg, etc...)
    * @return Returns the bytes for an image
    */
  private def getImageBytes(img: BufferedImage, format: String): Array[Byte] = {
    val imageStream = new ByteArrayOutputStream
    ImageIO.write(img, format, imageStream)
    imageStream.toByteArray
  }

  /**
    * Writes a string to the user's clipboard
    *
    * @param str String to write
    */
  private def saveToClipboard(str: String): Unit = {
    val stringSelection = new StringSelection(str)
    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(stringSelection, stringSelection)
  }
}
