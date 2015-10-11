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
  "com.wordnik" %% "swagger-core" % "1.3.12" withSources() withJavadoc(),        
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1" withSources() withJavadoc(),        
  "pl.matisoft" %% "swagger-play24" % "1.4" withSources() withJavadoc(), 
  "net.sf.dozer" % "dozer" % "5.4.0" withSources() withJavadoc(), 
  "net.gjerull.etherpad" % "etherpad_lite_client" % "1.2.12" withSources() withJavadoc(), 
  "com.squareup.retrofit" % "retrofit" % "2.0.0-beta2" withSources() withJavadoc(), 
  "com.squareup.retrofit" % "converter-gson" % "2.0.0-beta2" withSources() withJavadoc(), 
  "org.hamcrest" % "hamcrest-library" % "1.3",
  "com.novocode" % "junit-interface" % "0.8" % "test->default"
  // The official old swagger play installation. Uncomment when it works with our current play version (2.4.x)  
  //"com.wordnik" %% "swagger-play2" % "1.3.11" withSources() withJavadoc()
  // Another Implementation of swagger-play2
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
  
// Eclipse configurations
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes 
