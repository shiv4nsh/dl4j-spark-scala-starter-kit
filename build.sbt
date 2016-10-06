// If you have JDK 6 and not JDK 7 then replace all three instances of the number 7 to the number 6

organization := "com.knoldus"

name := "dl4j-spark-scala-starter-kit"

version := "0.1.4"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

scalacOptions in(Compile, doc) <++= baseDirectory.map {
  (bd: File) => Seq[String](
    "-sourcepath", bd.getAbsolutePath,
    "-doc-source-url", "https://github.com/mslinn/changeMe/tree/master€{FILE_PATH}.scala"
  )
}

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

resolvers ++= Seq(
  "Lightbend Releases" at "http://repo.typesafe.com/typesafe/releases"
)

val nd4jVersion = "0.4.0"
libraryDependencies ++= Seq(
  "org.nd4j" % "nd4j-native" % nd4jVersion % "compile",
  "org.nd4j" % "nd4j-api" % nd4jVersion % "compile",
  "org.bytedeco" % "javacpp" % "1.2.3" % "compile",
  "org.deeplearning4j" % "dl4j-spark_2.11" % "0.4.0",
  "com.beust" % "jcommander" % "1.27",
  "org.bytedeco" % "javacpp" % "1.2.3" % "compile",
  //"com.github.nscala-time"  %% "nscala-time"   % "1.8.0" withSources(),
  "org.scalatest" %% "scalatest" % "2.2.6" % "test" withSources(),
  "junit" % "junit" % "4.12" % "test"
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

classpathTypes += "maven-plugin"

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console :=
  """
    | """.stripMargin

cancelable := true


