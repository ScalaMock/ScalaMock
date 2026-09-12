import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.20")
lazy val specs2 = Def.setting("org.specs2" %%% "specs2-core" % "4.23.0")
lazy val specs2_5 = Def.setting("org.specs2" %%% "specs2-core" % "5.9.1")

val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "3.9.0",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
)

lazy val root = project.in(file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(
    scalamock.jvm,
    scalamock.js,
    scalamock.native,
    `scalamock-zio`.jvm,
    `scalamock-zio`.js,
    `scalamock-zio`.native,
    `scalamock-cats-effect`.jvm,
    `scalamock-cats-effect`.js,
    `scalamock-cats-effect`.native,
    `scalamock-specs2-4`.jvm,
    `scalamock-specs2-4`.js,
    `scalamock-specs2-4`.native,
    `scalamock-specs2-5`.jvm,
    `scalamock-specs2-5`.js,
    `scalamock-specs2-5`.native,
    `scalamock-scalatest`.jvm,
    `scalamock-scalatest`.js,
    `scalamock-scalatest`.native
  )

lazy val scalamock = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    commonSettings,
    crossScalaSettings,
    name := "scalamock",
    Compile / doc / scalacOptions ++= Opts.doc.title("ScalaMock") ++
      Opts.doc.version(version.value) ++ Seq("-doc-root-content", "rootdoc.txt", "-version"),
    libraryDependencies ++= Seq(
      scalatest.value % Test
    )
  )
  // Scala Native 0.5 dropped java.lang.reflect support, which the Scala 2 macros rely on.
  // Only Scala 3 (which uses scala.reflect.Selectable instead) is supported on Native.
  .nativeSettings(
    crossScalaVersions := Seq(scalaVersion.value)
  )

lazy val `scalamock-zio` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("zio"))
  .settings(
    name := "scalamock-zio",
    commonSettings,
    crossScalaSettings,
    libraryDependencies ++= {
      val zioVersion = "2.1.26"
      Seq(
        "dev.zio" %%% "zio" % zioVersion,
        "dev.zio" %%% "zio-test" % zioVersion,
        "dev.zio" %%% "zio-test-sbt" % zioVersion % Test
      )
    }
  )
  .jsSettings(name := "scalamock-zio")
  .jvmSettings(name := "scalamock-zio")
  .nativeSettings(
    name := "scalamock-zio",
    crossScalaVersions := Seq(scalaVersion.value)
  )
  .dependsOn(scalamock)


lazy val `scalamock-cats-effect` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("cats-effect"))
  .settings(
    name := "scalamock-cats-effect",
    commonSettings,
    crossScalaSettings,
    libraryDependencies ++= Seq(

      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.typelevel" %% "munit-cats-effect" % "2.2.0" % Test
    )
  )
  .jsSettings(name := "scalamock-cats-effect")
  .jvmSettings(name := "scalamock-cats-effect")
  .nativeSettings(
    name := "scalamock-cats-effect",
    crossScalaVersions := Seq(scalaVersion.value)
  )
  .dependsOn(scalamock)

lazy val `scalamock-specs2-4` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("specs2/specs2-4"))
  .settings(
    name := "scalamock-specs2-4",
    commonSettings,
    crossScalaSettings,
    libraryDependencies += specs2.value
  )
  // Scala Native 0.5 dropped java.lang.reflect support, which the Scala 2 macros rely on.
  // Only Scala 3 (which uses scala.reflect.Selectable instead) is supported on Native.
  .nativeSettings(
    crossScalaVersions := Seq(scalaVersion.value)
  )
  .dependsOn(scalamock)

// specs2 5.x dropped Scala 2 support entirely - only Scala 3 artifacts are published
// (for JVM, JS and Native), so this module is restricted to Scala 3 across all platforms.
lazy val `scalamock-specs2-5` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("specs2/specs2-5"))
  .settings(
    name := "scalamock-specs2-5",
    commonSettings,
    crossScalaSettings,
    crossScalaVersions := Seq(scalaVersion.value),
    libraryDependencies += specs2_5.value
  )
  .dependsOn(scalamock)

lazy val `scalamock-scalatest` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("scalatest"))
  .settings(
    name := "scalamock-scalatest",
    commonSettings,
    crossScalaSettings,
    libraryDependencies += scalatest.value
  )
  // Scala Native 0.5 dropped java.lang.reflect support, which the Scala 2 macros rely on.
  // Only Scala 3 (which uses scala.reflect.Selectable instead) is supported on Native.
  .nativeSettings(
    crossScalaVersions := Seq(scalaVersion.value)
  )
  .dependsOn(scalamock % "compile->compile;test->test")

lazy val examples = project
  .in(file("core/examples"))
  .settings(
    commonSettings,
    crossScalaSettings,
    name := "ScalaMock Examples",
    publish / skip := true
  ) dependsOn (scalamock.jvm, `scalamock-scalatest`.jvm, `scalamock-specs2-4`.jvm)

def crossScalaSettings = {
  def addDirsByScalaVersion(path: String): Def.Initialize[Seq[sbt.File]] =
    scalaVersion.zip(baseDirectory) { case (v, base) =>
      CrossVersion.partialVersion(v) match {
        case Some((v, _)) if Set(2L, 3L).contains(v) =>
          Seq(base / path / s"scala-$v")
        case _ =>
          Seq.empty
      }
    }
  Seq(
    crossScalaVersions := Seq("2.12.21", "2.13.18", scalaVersion.value),
    Compile / unmanagedSourceDirectories ++= addDirsByScalaVersion("src/main").value,
    Test / unmanagedSourceDirectories ++= addDirsByScalaVersion("src/test").value,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
        case _ =>
          Seq.empty
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq("-Xlint:adapted-args", "-Xfatal-warnings")
        case Some((3, _)) =>
          Seq("-Yexplicit-nulls", "-language:unsafeNulls")
        case _ =>
          Seq.empty
      }
    }
  )
}

inThisBuild(
  List(
    organization := "org.scalamock",
    homepage := Some(url("http://scalamock.org/")),
    licenses := List(
      "MIT" -> url("https://opensource.org/licenses/MIT")
    ),
    developers := List(
      Developer("paulbutcher", "Paul Butcher", "", url("http://paulbutcher.com/")),
      Developer("barkhorn", "Philipp Meyerhoefer", "", url("https://github.com/barkhorn")),
      Developer("goshacodes", "Georgii Kovalev", "", url("https://github.com/goshacodes"))
    )
  )
)