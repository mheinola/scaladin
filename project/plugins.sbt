resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += Classpaths.typesafeReleases

// sbt-vaadin-plugin
resolvers += "sbt-vaadin-plugin repo" at "http://henrikerola.github.io/repository/releases"

addSbtPlugin("org.vaadin.sbt" % "sbt-vaadin-plugin" % "1.2.0")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.1.0")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
