moduleName := "sbt-ci-release-early"
organization := "io.shiftleft"
sbtPlugin := true

scalaVersion := "2.12.6"
libraryDependencies ++= List(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.0.2.201807311906-r",
  "com.michaelpollmeier" % "versionsort" % "1.0.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.5")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

homepage := Some(url("https://github.com/ShiftLeftSecurity/sbt-ci-release-early"))
scmInfo := Some(ScmInfo(
  url("https://github.com/ShiftLeftSecurity/sbt-ci-release-early"),
  "scm:git@github.com:ShiftLeftSecurity/sbt-ci-release-early.git"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
developers := List(
  Developer(
    "olafurpg",
    "Ólafur Páll Geirsson",
    "olafurpg@gmail.com",
    url("https://geirsson.com")
  ),
  Developer(
    "mpollmeier",
    "Michael Pollmeier",
    "michael@michaelpollmeier.com",
    url("https://michaelpollmeier.com")
  )
)
resolvers += Resolver.sonatypeRepo("releases")

publishTo := sonatypePublishTo.value
