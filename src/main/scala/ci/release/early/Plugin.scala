package ci.release.early

import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.SbtPgp.autoImport._
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport._
import xerial.sbt.Sonatype.SonatypeCommand.sonatypeReleaseAll

object Plugin extends AutoPlugin {
  object autoImport {
    val verifyNoSnapshotDependencies = taskKey[Unit]("Verify there are no snapshot dependencies (fail otherwise)")
    // val currentTag = settingKey[String]("tag for this version")
  }
  import autoImport._

  override def globalSettings: Seq[Def.Setting[_]] = List(
    publishArtifact.in(Test) := false,
    publishMavenStyle := true,
    /* I tried to define these commands as tasks, but had the following errors:
     * - didn't know how to update the version within the task
     * - didn't figure out how to automatically cross-build without lot's of extra code
    */
    commands += Command.command("ci-release") { state =>
      println("Running ci-release (cross scala versions)")
      val versionAndTag = Utils.determineAndTagTargetVersion
      // TODO push *after* release is complete
      Utils.push(versionAndTag.tag)
      s"""set ThisBuild/version := "${versionAndTag.version}"""" ::
        "verifyNoSnapshotDependencies" ::
        "+publish" ::
        state
    },
    commands += Command.command("ci-release-sonatype") { state =>
      println("Running ci-release-sonatype (non-cross-scala versions)")
      assert(pgpPassphrase.value.isDefined,
        "please specify PGP_PASSPHRASE as an environment variable (e.g. `export PGP_PASSPHRASE='secret')")
      val versionAndTag = Utils.determineAndTagTargetVersion
      // TODO push *after* release is complete
      Utils.push(versionAndTag.tag)
      s"""set ThisBuild/version := "${versionAndTag.version}"""" ::
        "verifyNoSnapshotDependencies" ::
        "sonatypeOpen \"sbt-ci-release-early\"" ::
        "publishSigned" ::
        "sonatypeRelease" ::
        state
    },
    commands += Command.command("ci-cross-release-sonatype") { state =>
      println("Running ci-release-sonatype (cross-scala versions)")
      println("note: only use this mode if you actually have a cross build for multiple scala versions. it uses `sonatypeReleaseAll` which will fail if any previous staging repo had any problems (e.g. timeout during release)")
      assert(pgpPassphrase.value.isDefined,
        "please specify PGP_PASSPHRASE as an environment variable (e.g. `export PGP_PASSPHRASE='secret')")
      val versionAndTag = Utils.determineAndTagTargetVersion
      // TODO push *after* release is complete
      Utils.push(versionAndTag.tag)
      s"""set ThisBuild/version := "${versionAndTag.version}"""" ::
        "verifyNoSnapshotDependencies" ::
        "sonatypeOpen \"sbt-ci-release-early cross\"" :: //sets `sonatypeStagingRepositoryProfile`
        "+publishSigned" :: //discards `sonatypeStagingRepositoryProfile` :(
        "sonatypeReleaseAll" :: //fails if there's previous inconsistent staging repos
        state
    }
  )

  override def requires = SbtPgp && Sonatype
  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    verifyNoSnapshotDependencies := verifyNoSnapshotDependenciesTask.value
    // currentTag := currentTag.value
  )

  override def buildSettings: Seq[Def.Setting[_]] = List(
    pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray)
  )

  lazy val verifyNoSnapshotDependenciesTask = Def.task {
    val moduleIds = (managedClasspath in Runtime).value.flatMap(_.get(moduleID.key))
    val snapshots = moduleIds.filter(m => m.isChanging || m.revision.endsWith("-SNAPSHOT")).toList
    assert(snapshots.size == 0, s"expected 0 snapshot dependencies, but found: $snapshots")
  }
}
