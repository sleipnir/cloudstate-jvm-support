import java.io.File
import java.util.Date

import sbt.Keys.{developers, scmInfo}
import sbt.url

inThisBuild(
  Seq(
    organization := "io.cloudstate",
    version := dynverGitDescribeOutput.value.mkVersion(versionFmt, "latest"),
    dynver := sbtdynver.DynVer.getGitDescribeOutput(new Date).mkVersion(versionFmt, "latest"),
    scalaVersion := "2.12.11",
    // Needed for the akka-grpc 0.8.4 snapshot
    resolvers += Resolver.bintrayRepo("akka", "maven"), // TODO: Remove once we're switching to akka-grpc 0.8.5/1.0.0
    organizationName := "Lightbend Inc.",
    organizationHomepage := Some(url("https://lightbend.com")),
    startYear := Some(2019),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://cloudstate.io")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/cloudstateio/cloudstate"),
        "scm:git@github.com:cloudstateio/cloudstate.git"
      )
    ),
    developers := List(
      Developer(id = "jroper", name = "James Roper", email = "james@jazzy.id.au", url = url("https://jazzy.id.au")),
      Developer(id = "viktorklang",
        name = "Viktor Klang",
        email = "viktor.klang@gmail.com",
        url = url("https://viktorklang.com"))
    ),
    sonatypeProfileName := "io.cloudstate",
    scalafmtOnCompile := true,
    closeClassLoaders := false
  )
)

// Make sure the version doesn't change each time it gets built, this ensures we don't rebuild the native image
// every time we build a docker image based on it, since we actually build 3 different docker images for the proxy
// command.
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val dirtySuffix = if (out.isDirty()) "-dev" else ""
  if (out.isCleanAfterTag) out.ref.dropV.value
  else out.ref.dropV.value + out.commitSuffix.mkString("-", "-", "") + dirtySuffix
}

name := "jvm-support"

version := "0.1"

scalaVersion := "2.13.2"

val GrpcJavaVersion = "1.22.1"
val GraalAkkaVersion = "0.5.0"
val AkkaVersion = "2.5.31"
val AkkaHttpVersion = "10.1.11"
val AkkaManagementVersion = "1.0.5"
val PrometheusClientVersion = "0.6.0"
val ScalaTestVersion = "3.0.5"
val ProtobufVersion = "3.9.0"

def common: Seq[Setting[_]] = Seq(
  headerMappings := headerMappings.value ++ Seq(
    de.heikoseeberger.sbtheader.FileType("proto") -> HeaderCommentStyle.cppStyleLineComment,
    de.heikoseeberger.sbtheader.FileType("js") -> HeaderCommentStyle.cStyleBlockComment
  ),
  // Akka gRPC overrides the default ScalaPB setting including the file base name, let's override it right back.
  akkaGrpcCodeGeneratorSettings := Seq(),
  excludeFilter in headerResources := HiddenFileFilter || GlobFilter("reflection.proto"),
  javaOptions in Test ++= Seq("-Xms1G", "-XX:+CMSClassUnloadingEnabled", "-XX:+UseConcMarkSweepGC")
)

lazy val root = (project in file("."))
  // Don't forget to add your sbt module here!
  // A missing module here can lead to failing Travis test results
  .aggregate(
    `jvm-support`
  )
  .settings(common)

val cloudstateProtocolsName = "cloudstate-protocols"
val cloudstateTCKProtocolsName = "cloudstate-tck-protocols"

lazy val protocols = (project in file("protocols"))
  .settings(
    name := "protocols",
    publish / skip := true,
    packageBin in Compile := {
      val base = baseDirectory.value
      val cloudstateProtos = base / s"$cloudstateProtocolsName.zip"
      val cloudstateTCKProtos = base / s"$cloudstateTCKProtocolsName.zip"

      def archiveStructure(topDirName: String, files: PathFinder): Seq[(File, String)] =
        files pair Path.relativeTo(base) map {
          case (f, s) => (f, s"$topDirName${File.separator}$s")
        }

      // Common Language Support Proto Dependencies
      IO.zip(
        archiveStructure(cloudstateProtocolsName,
          (base / "frontend" ** "*.proto" +++
            base / "protocol" ** "*.proto" +++
            base / "proxy" ** "*.proto")),
        cloudstateProtos
      )

      // Common TCK Language Support Proto Dependencies
      IO.zip(archiveStructure(cloudstateTCKProtocolsName, base / "example" ** "*.proto"), cloudstateTCKProtos)

      cloudstateProtos
    },
    cleanFiles ++= Seq(
      baseDirectory.value / s"$cloudstateProtocolsName.zip",
      baseDirectory.value / s"$cloudstateTCKProtocolsName.zip"
    )
  )

lazy val `jvm-support` = (project in file("jvm-support"))
  .enablePlugins(AkkaGrpcPlugin, BuildInfoPlugin)
  .settings(
    name := "jvm-support",
    common,
    crossPaths := false,
    publishMavenStyle := true,
    publishTo := sonatypePublishTo.value,
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "io.cloudstate.jvmsupport",
    // Generate javadocs by just including non generated Java sources
    sourceDirectories in (Compile, doc) := Seq((javaSource in Compile).value),
    sources in (Compile, doc) := {
      val javaSourceDir = (javaSource in Compile).value.getAbsolutePath
      (sources in (Compile, doc)).value.filter(_.getAbsolutePath.startsWith(javaSourceDir))
    },
    // javadoc (I think java 9 onwards) refuses to compile javadocs if it can't compile the entire source path.
    // but since we have java files depending on Scala files, we need to include ourselves on the classpath.
    dependencyClasspath in (Compile, doc) := (fullClasspath in Compile).value,
    javacOptions in (Compile, doc) ++= Seq(
      "-overview",
      ((javaSource in Compile).value / "overview.html").getAbsolutePath,
      "-notimestamp",
      "-doctitle",
      "Cloudstate Java Support"
    ),
    libraryDependencies ++= Seq(
      // Remove these explicit gRPC/netty dependencies once akka-grpc 0.7.1 is released and we've upgraded to using that
      "io.grpc" % "grpc-core" % GrpcJavaVersion,
      "io.grpc" % "grpc-netty-shaded" % GrpcJavaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-core" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,
      "com.google.protobuf" % "protobuf-java" % ProtobufVersion % "protobuf",
      "com.google.protobuf" % "protobuf-java-util" % ProtobufVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "org.slf4j" % "slf4j-simple" % "1.7.26",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9.3"
    ),
    javacOptions in Compile ++= Seq("-encoding", "UTF-8"),
    javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8"),
    akkaGrpcGeneratedSources in Compile := Seq(AkkaGrpc.Server),
    akkaGrpcGeneratedLanguages in Compile := Seq(AkkaGrpc.Scala), // FIXME should be Java, but here be dragons

    // Work around for https://github.com/akka/akka-grpc/pull/673
    (PB.targets in Compile) := {
      val old = (PB.targets in Compile).value
      val ct = crossTarget.value

      old.map(_.copy(outputPath = ct / "akka-grpc" / "main"))
    },
    PB.protoSources in Compile ++= {
      val baseDir = (baseDirectory in ThisBuild).value / "protocols"
      Seq(baseDir / "protocol", baseDir / "frontend")
    },
    // We need to generate the java files for things like entity_key.proto so that downstream libraries can use them
    // without needing to generate them themselves
    PB.targets in Compile += PB.gens.java -> crossTarget.value / "akka-grpc" / "main",
    inConfig(Test)(
      sbtprotoc.ProtocPlugin.protobufConfigSettings ++ Seq(
        PB.protoSources ++= {
          val baseDir = (baseDirectory in ThisBuild).value / "protocols"
          Seq(baseDir / "example")
        },
        PB.targets := Seq(
          PB.gens.java -> crossTarget.value / "akka-grpc" / "test"
        )
      )
    )
  )