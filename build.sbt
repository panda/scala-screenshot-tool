name := "SST"

version := "1.0"

scalaVersion := "2.12.1"
scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"
libraryDependencies += "com.github.tulskiy" % "jkeymaster" % "1.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.5"
libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.5.5"