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
  "javax.xml.bind" % "jaxb-api" % "2.2.12",           // library to support objects-to/from-xml mapping
  "org.webjars" % "bootstrap" % "3.3.1", 			        // jar-packaged version of twitter bootstrap  
  "org.webjars" % "angularjs" % "1.3.14",			        // jar-packaged version of twitter angularjs
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",  // jar-packaged version of twitter angularjs-ui-bootstrap
  "com.feth" %% "play-authenticate" % "0.6.8",        // authentication library that supports Oauth2, Oauth1, OpenID and Simple Auth
  "be.objectify" %% "deadbolt-java" % "2.3.2",        // authoriazation framework that supports roles
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4", 
  "org.codehaus.jackson" % "jackson-core-asl" % "1.1.0"
)

resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.io/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.io/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)

