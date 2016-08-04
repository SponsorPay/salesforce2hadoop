name := "salesforce2hadoop"

organization := "co.datadudes"

version := "1.0"

scalaVersion := "2.11.4"

resolvers += "Cloudera Repositories" at "https://repository.cloudera.com/artifactory/cloudera-repos"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging"  %% "scala-logging"    % "3.1.0",
  "ch.qos.logback"              % "logback-classic"   % "1.1.2",
  "org.slf4j"                   % "slf4j-api"         % "1.7.10",
  "org.scala-lang.modules"      %% "scala-xml"        % "1.0.2",
  "org.apache.avro"             % "avro"              % "1.7.5",
  "com.force.api"               % "force-wsc"         % "37.0.3" exclude("org.antlr", "ST4"),
  "com.force.api"               % "force-partner-api" % "37.0.3",
  "co.datadudes"                %% "wsdl2avro"        % "0.2.1",
  "org.kitesdk"                 % "kite-hadoop-cdh5-dependencies" % "1.0.0" pomOnly()
    exclude("commons-beanutils", "commons-beanutils")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("org.slf4j", "slf4j-log4j12"),
  "org.kitesdk"                 % "kite-data-core"    % "1.0.0",
  "com.github.scopt"            %% "scopt"            % "3.3.0",
  "org.specs2"                  %% "specs2-junit"     % "2.4.15"    % "test",
  "org.specs2"                  %% "specs2-mock"      % "2.4.15"    % "test"
)

mainClass in assembly := Some("co.datadudes.sf2hadoop.SFImportCLIRunner")

net.virtualvoid.sbt.graph.Plugin.graphSettings

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "maven", "org.slf4j", "slf4j-api", xs @ _*) => MergeStrategy.first
  case "org/apache/hadoop/yarn/factories/package-info.class"          => MergeStrategy.first
  case "org/apache/hadoop/yarn/factory/providers/package-info.class"  => MergeStrategy.first
  case "org/apache/hadoop/yarn/util/package-info.class"               => MergeStrategy.first
  case PathList("org", "apache", "commons", "collections", xs @ _*)   => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
