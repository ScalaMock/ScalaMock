import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.20")
lazy val specs2 = Def.setting("org.specs2" %%% "specs2-core" % "4.23.0")

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
    `scalamock-zio`.jvm,
    `scalamock-zio`.js,
    `scalamock-cats-effect`.jvm,
    `scalamock-cats-effect`.js
  )

lazy val scalamock = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(
    commonSettings,
    crossScalaSettings,
    name := "scalamock",
    Compile / doc / scalacOptions ++= Opts.doc.title("ScalaMock") ++
      Opts.doc.version(version.value) ++ Seq("-doc-root-content", "rootdoc.txt", "-version"),
    libraryDependencies ++= Seq(
      scalatest.value % Optional,
      specs2.value % Optional
    )
  )

lazy val `scalamock-zio` = crossProject(JSPlatform, JVMPlatform)
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
  .dependsOn(scalamock)


lazy val `scalamock-cats-effect` = crossProject(JSPlatform, JVMPlatform)
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
  .dependsOn(scalamock)

lazy val examples = project
  .in(file("core/examples"))
  .settings(
    commonSettings,
    crossScalaSettings,
    name := "ScalaMock Examples",
    publish / skip := true,
    libraryDependencies ++= Seq(
      scalatest.value % Test,
      specs2.value % Test
    )
  ) dependsOn scalamock.jvm

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