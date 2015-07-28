import com.github.play2war.plugin._

name := "consolidate-services-app"

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.0"
Play2WarKeys.targetName := Some("mauro")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

disablePlugins(PlayLayoutPlugin)

//scalaVersion := "2.10.4"

//scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  ws,
  servletApi % Provided)

libraryDependencies += specs2 % Test

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

import com.github.play2war.plugin._

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases/"
resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
//resolvers += "Rocketlawyer Snapshots" at "http://f1tst-linbld100/nexus/content/repositories/snapshots"
//resolvers += "Rocketlawyer Releases" at "http://f1tst-linbld100/nexus/content/repositories/releases"

/*
val packageWarFile = taskKey[File]("mauro")

// create an Artifact for publishing the .war file
artifact in (Compile, packageWarFile) := {
  val previous: Artifact = (artifact in (Compile, packageWarFile)).value
  previous.copy(`type` = "war", extension = "war", classifier = Some("webapp"))
}
*/

// add the .war file to what gets published
//addArtifact(artifact in (Compile, packageWarFile), packageWarFile)


// disable .jar publishing
publishArtifact in (Compile, packageBin) := false

sbtPlugin := true

publishMavenStyle := true
