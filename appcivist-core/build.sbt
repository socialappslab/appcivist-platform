name := """appcivist-core"""

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

// Resolvers for Maven2 repositories
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs, 
  "org.webjars" % "bootstrap" % "3.3.1", 			// jar-packaged version of twitter bootstrap  
  "org.webjars" % "angularjs" % "1.3.8",			// jar-packaged version of twitter angularjs
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0" // jar-packaged version of twitter angularjs-ui-bootstrap
)
