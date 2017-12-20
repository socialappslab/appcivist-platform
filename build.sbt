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
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.7.3" withSources(),     
  "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "4.5.3" withSources(),
  cache,
  javaWs withSources(), 
  "javax.xml.bind" % "jaxb-api" % "2.2.12",           // library to support objects-to/from-xml mapping
  "com.lowagie" % "itext" % "2.1.7",                  // pdf and rtf
  "com.lowagie" % "itext-rtf" % "2.1.7",                  // pdf and rtf
  "org.webjars" % "bootstrap" % "3.3.1", 			        // jar-packaged version of twitter bootstrap  
  "org.webjars" % "angularjs" % "1.3.14",			        // jar-packaged version of twitter angularjs
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",  // jar-packaged version of twitter angularjs-ui-bootstrap
  "org.webjars" % "jquery" % "2.1.3",
  "com.feth" %% "play-authenticate" % "0.7.0" withSources() withJavadoc(),        // authentication library that supports Oauth2, Oauth1, OpenID and Simple Auth
  "com.feth" %% "play-easymail" % "0.7.0" withSources() withJavadoc(),        
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41", 
  "org.codehaus.jackson" % "jackson-core-asl" % "1.1.0",
  "org.springframework" % "spring-context" % "4.1.6.RELEASE" withSources(),
  "commons-io" % "commons-io" % "2.4",
  "be.objectify" %% "deadbolt-java" % "2.4.0" withSources() withJavadoc(),       
  "io.swagger" % "swagger-core" % "1.5.8" withSources() withJavadoc(),        
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1" withSources() withJavadoc(),        
  "io.swagger" %% "swagger-play2" % "1.5.2" withSources() withJavadoc(), 
  "net.sf.dozer" % "dozer" % "5.4.0" withSources() withJavadoc(), 
  "net.gjerull.etherpad" % "etherpad_lite_client" % "1.2.12" withSources() withJavadoc(), 
  "com.squareup.retrofit" % "retrofit" % "2.0.0-beta2" withSources() withJavadoc(), 
  "com.squareup.retrofit" % "converter-gson" % "2.0.0-beta2" withSources() withJavadoc(), 
  "com.squareup.retrofit" % "adapter-rxjava" % "2.0.0-beta2" withSources() withJavadoc(), 
  "org.hamcrest" % "hamcrest-library" % "1.3",
  "com.novocode" % "junit-interface" % "0.8" % "test->default", 
  "com.amazonaws" % "aws-java-sdk" % "1.10.42", 
  "com.zaxxer" % "HikariCP" % "2.4.6", 
  "org.jsoup" % "jsoup" % "1.10.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.json" % "json" % "20171018",
  "com.github.opendevl" % "json2flat" % "1.0.3",
  "org.apache.poi" % "poi-ooxml" % "3.17",
  "com.google.api-client" % "google-api-client" % "1.23.0",
  "com.rabbitmq" % "amqp-client" % "5.1.0"

)

libraryDependencies += evolutions
libraryDependencies += filters

resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))

resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))

resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.io/play-authenticate/repo/releases/"))

resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.io/play-authenticate/repo/snapshots/"))

resolvers += Resolver.bintrayRepo("markusjura", "maven")

// Enable Java Ebean
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  
// Eclipse configurations
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes 
