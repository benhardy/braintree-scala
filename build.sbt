
organization := "net.bhardy.braintree"

name := "braintree-scala"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.0"

classpathTypes ~= (_ + "orbit")

javacOptions ++= Seq(
    "-source", "1.6",
    "-target", "1.6"
)

libraryDependencies ++= Seq(
    "org.json4s" %% "json4s-jackson" % "3.1.0",
    "org.scalatest" %% "scalatest" % "1.9.1"  % "test",
    "junit" % "junit" % "4.8.1"  % "test",
    "org.mockito" % "mockito-all" % "1.9.5"  % "test",
    "com.novocode" % "junit-interface" % "0.10" % "test"
)

