resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += Classpaths.typesafeReleases

// xsbt-web-plugin

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.1")

// sbtidea
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

// sbteclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.3.0")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.1.0")
