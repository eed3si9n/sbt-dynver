val dynverRoot = project.in(file("."))
aggregateProjects(dynverLib, sbtdynver)

lazy val scalacOptions212 = Seq(
  "-Xlint",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Yno-adapted-args",
)

inThisBuild(List(
  organization := "com.github.sbt",
      licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
   description := "An sbt plugin to dynamically set your version from git",
    developers := List(Developer("dwijnand", "Dale Wijnand", "dale wijnand gmail com", url("https://dwijnand.com"))),
     startYear := Some(2016),
      homepage := scmInfo.value map (_.browseUrl),
       scmInfo := Some(ScmInfo(url("https://github.com/sbt/sbt-dynver"), "scm:git:git@github.com:sbt/sbt-dynver.git")),

            Global /      sbtVersion  := "1.1.0", // must be Global, otherwise ^^ won't change anything
  LocalRootProject / crossSbtVersions := List("1.1.0"),

  scalaVersion := "2.12.17",

  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-feature",
    "-unchecked",
  ) ++ scalacOptions212,
  Test /              fork := false,
  Test /       logBuffered := false,
  Test / parallelExecution := true,
))

val dynverLib = LocalProject("dynver")
val dynver    = project.settings(
  libraryDependencies += "org.eclipse.jgit"  % "org.eclipse.jgit" % "5.12.0.202106070339-r" % Test,
  libraryDependencies += "org.scalacheck"   %% "scalacheck"       % "1.15.4"                % Test,
  resolvers           += Resolver.sbtPluginRepo("releases"), // for prev artifacts, not repo1 b/c of mergly publishing
  publishSettings,
  crossScalaVersions ++= Seq("2.13.10", "3.2.2"),
  scalacOptions := {
    if (scalaVersion.value.startsWith("3") || scalaVersion.value.startsWith("2.13")) {
      scalacOptions.value.filterNot(scalacOptions212.contains(_))
    } else {
      scalacOptions.value
    }
  }
)

val sbtdynver = project.dependsOn(dynverLib).enablePlugins(SbtPlugin).settings(
                  name := "sbt-dynver",
  scriptedBufferLog    := true,
  scriptedDependencies := Seq(dynver / publishLocal, publishLocal).dependOn.value,
  scriptedLaunchOpts   += s"-Dplugin.version=${version.value}",
  scriptedLaunchOpts   += s"-Dsbt.boot.directory=${file(sys.props("user.home")) / ".sbt" / "boot"}",
  publishSettings,
)

lazy val publishSettings = Def.settings(
  MimaSettings.mimaSettings,
)

mimaPreviousArtifacts := Set.empty
publish / skip        := true

Global / excludeLintKeys += crossSbtVersions // Used by the "^" command (PluginCrossCommand)
Global / cancelable      := true
