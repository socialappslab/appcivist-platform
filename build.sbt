name := """appcivist-core"""

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

herokuAppName in Compile := "appcivist-pb"

herokuJdkVersion in Compile := "1.8"

// Resolvers for Maven2 repositories
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  javaJdbc withSources(),
//  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.6.2" withSources(),     
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.7.3" withSources(),     
  "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "4.5.3" withSources(),
  cache,
  javaWs withSources(), 
  "javax.xml.bind" % "jaxb-api" % "2.2.12",           // library to support objects-to/from-xml mapping
  "org.webjars" % "bootstrap" % "3.3.1", 			        // jar-packaged version of twitter bootstrap  
  "org.webjars" % "angularjs" % "1.3.14",			        // jar-packaged version of twitter angularjs
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",  // jar-packaged version of twitter angularjs-ui-bootstrap
  "org.webjars" % "jquery" % "2.1.3",
  "com.feth" %% "play-authenticate" % "0.7.0-SNAPSHOT" withSources() withJavadoc(),        // authentication library that supports Oauth2, Oauth1, OpenID and Simple Auth
  "com.feth" %% "play-easymail" % "0.7.0-SNAPSHOT" withSources() withJavadoc(),        
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41", 
  "org.codehaus.jackson" % "jackson-core-asl" % "1.1.0",
  "org.springframework" % "spring-context" % "4.1.6.RELEASE" withSources(),
  "commons-io" % "commons-io" % "2.4",
  "be.objectify" %% "deadbolt-java" % "2.4.0" withSources() withJavadoc(),       
  "com.wordnik" %% "swagger-core" % "1.3.12" withSources() withJavadoc()        
  //"com.wordnik" %% "swagger-play2" % "1.3.11" withSources() withJavadoc()
  //"com.markusjura" %% "swagger-play2" % "1.3.7" withSources() withJavadoc()
)

libraryDependencies += evolutions

resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.io/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.io/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.bintrayRepo("markusjura", "maven")

// Enable Java Ebean
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)