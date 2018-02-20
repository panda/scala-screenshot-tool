package wtf.pants.sst.config

import io.circe.generic.JsonCodec
import wtf.pants.sst.config.enum.RequestType
import wtf.pants.sst.config.enum.RequestType.RequestType


class Destination(val url: String,
                                  val fileField: String,
                                  var arguments: Array[(String, String)] = new Array[(String, String)](0),
                                  val requestType: RequestType = RequestType.POST) {

  def updateArgs(newArgs: String): Unit ={
    arguments = newArgs.split("&").map(s => (s.split("=")(0), s.split("=")(1)))
  }

}