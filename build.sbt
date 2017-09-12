name := """dyndns"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "startbootstrap-sb-admin" % "3.3.7"
)

resolvers ++= Seq(
    "maven.javastream.de" at "http://maven.javastream.de"
)