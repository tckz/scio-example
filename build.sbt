
enablePlugins(PackPlugin)

name := "scio-example"

version := "0.1"

scalaVersion := "2.12.8"


libraryDependencies ++= Seq(
  "com.spotify" %% "scio-core" % "0.7.0",
  "com.google.cloud" % "google-cloud-datastore" % "1.52.0",
  "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % "2.9.0",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "me.lyh" %% "shapeless-datatype-datastore_1.3" % "0.1.10",
)

val env = sys.env.get("BUILD_FOR_GCP")
libraryDependencies ++= Seq(
  "org.apache.beam" % "beam-runners-direct-java" % "2.9.0",
  "org.slf4j" % "slf4j-simple" % "1.8.0-beta2"
)
  .filter(e => env.isEmpty)

packMain := Map(
  "ds_create" -> "com.example.scio.DataStoreCreateEntities",
  "ds_delete" -> "com.example.scio.DataStoreDeleteEntities"
)
