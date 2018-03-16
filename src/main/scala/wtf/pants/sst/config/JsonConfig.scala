package wtf.pants.sst.config

import io.circe.generic.JsonCodec

@JsonCodec
case class JsonConfig(url: String, key: String, authKey: String, fileKey: String)
