package wtf.pants.sst.config

import io.circe.generic.JsonCodec

@JsonCodec
case class JsonConfig(url: String, fileKey: String, arguments: Map[String, String])
