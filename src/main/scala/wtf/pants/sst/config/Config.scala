package wtf.pants.sst.config

import java.io.File

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto._

import scala.io.Source

class Config {



  def readJson(file: String): String = {
    Source.fromFile(file).mkString
  }

  def getConfig(file: String): Either[Error, JsonConfig] = {
    decode[JsonConfig](readJson(file))
  }

  val configDirectory = new File("config")
  if (!configDirectory.exists()) {
    val err = !configDirectory.mkdir()
    if (err) println("fucc")
  }

  val configs: Array[JsonConfig] = configDirectory.listFiles().map(f => getConfig(f.getAbsolutePath)).flatMap(_.toOption)

  //TODO: make configs selectable
  val shekels: Option[JsonConfig] = getConfig("/home/alice/dev/scala/scala-screenshot-tool/config/shekels.json").toOption

}
