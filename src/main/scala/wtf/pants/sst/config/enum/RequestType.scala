package wtf.pants.sst.config.enum

object RequestType extends Enumeration {
  type RequestType = Value
  val POST, GET, PUT, DELETE = Value
}
