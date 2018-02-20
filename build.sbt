name := "SST"

version := "1.0"

scalaVersion := "2.12.1"
scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq("org.scalafx" %% "scalafx" % "8.0.144-R12",
  "com.github.tulskiy" % "jkeymaster" % "1.2",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "org.apache.httpcomponents" % "httpmime" % "4.5.5",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "io.circe" %% "circe-core" % "0.9.1",
  "io.circe" %% "circe-generic" % "0.9.1",
  "io.circe" %% "circe-parser" % "0.9.1")