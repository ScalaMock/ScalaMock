import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.19")
lazy val specs2 = Def.setting("org.specs2" %%% "specs2-core" % "4.20.8")

val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "3.4.3",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-experimental"
  )
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
    `scalamock-cats-effect`.native
  )

lazy val scalamock = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    commonSettings,
    name := "scalamock",
    Compile / doc / scalacOptions ++= Opts.doc.title("ScalaMock") ++
      Opts.doc.version(version.value) ++ Seq("-doc-root-content", "rootdoc.txt", "-version"),
    libraryDependencies ++= Seq(
      scalatest.value % Optional,
      specs2.value % Optional
    ),
  )

lazy val `scalamock-zio` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("zio"))
  .settings(
    name := "scalamock-zio",
    commonSettings,
    libraryDependencies ++= {
      val zioVersion = "2.1.14"
      Seq(
        "dev.zio" %%% "zio" % zioVersion,
        "dev.zio" %%% "zio-test" % zioVersion % Test,
        "dev.zio" %%% "zio-test-sbt" % zioVersion % Test
      )
    }
  )
  .jsSettings(name := "scalamock-zio")
  .jvmSettings(name := "scalamock-zio")
  .nativeSettings(name := "scalamock-zio")
  .dependsOn(scalamock)


lazy val `scalamock-cats-effect` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("cats-effect"))
  .settings(
    name := "scalamock-cats-effect",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test
    )
  )
  .jsSettings(name := "scalamock-cats-effect")
  .jvmSettings(name := "scalamock-cats-effect")
  .nativeSettings(name := "scalamock-cats-effect")
  .dependsOn(scalamock)

lazy val examples = project in file("core/examples") settings(
  commonSettings,
  name := "ScalaMock Examples",
  publish / skip := true,
  libraryDependencies ++= Seq(
    scalatest.value % Test,
    specs2.value % Test
  )
) dependsOn scalamock.jvm

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

