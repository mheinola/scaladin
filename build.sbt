import org.vaadin.sbt.VaadinPlugin._

name := "Scaladin"

logLevel := Level.Debug

version in ThisBuild := "7.7-SNAPSHOT"

organization in ThisBuild := "org.vaadin.addons"

scalaVersion in ThisBuild := "2.11.8"

crossScalaVersions in ThisBuild := Seq("2.11.8")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-encoding", "UTF-8")

resolvers in ThisBuild += "Vaadin snapshots" at "https://oss.sonatype.org/content/repositories/vaadin-snapshots"

publishMavenStyle := false

publishArtifact in Test := false

publishArtifact in Compile := false

pomIncludeRepository := { _ => false }

publishTo := Some("Sonatype Nexus Repository Manager" at System.getProperty("nexusRepositoryUrl", "none"))

credentials += Credentials("Sonatype Nexus Repository Manager", System.getProperty("nexusUrl", "none"), System.getProperty("nexusUser", "none"), System.getProperty("nexusPassword", "none"))

lazy val addon = project
  .settings(vaadinAddOnSettings :_*)
  .settings(scalariformSettings :_*)
  .settings(
    name := "Scaladin",
    libraryDependencies := Dependencies.addonDeps(scalaVersion.value),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishArtifact in Compile := true,
    pomIncludeRepository := { _ => false },
    publishTo := Some("Sonatype Nexus Repository Manager" at System.getProperty("nexusRepositoryUrl", "none")),
    credentials += Credentials("Sonatype Nexus Repository Manager", System.getProperty("nexusUrl", "none"), System.getProperty("nexusUser", "none"), System.getProperty("nexusPassword", "none"))
  )

lazy val demo = project
  .enablePlugins(JettyPlugin)
  .settings(vaadinWebSettings :_*)
  .settings(scalariformSettings :_*)
  .settings(
    name := "scaladin-demo",
    libraryDependencies ++= Dependencies.demoDeps,
    publishMavenStyle := false,
    publishArtifact in Test := false,
    publishArtifact in Compile := false,
    pomIncludeRepository := { _ => false },
    publishTo := Some("Sonatype Nexus Repository Manager" at System.getProperty("nexusRepositoryUrl", "none")),
    credentials += Credentials("Sonatype Nexus Repository Manager", System.getProperty("nexusUrl", "none"), System.getProperty("nexusUser", "none"), System.getProperty("nexusPassword", "none"))
  ).dependsOn(addon)

lazy val root = project.in(file(".")).aggregate(addon, demo)