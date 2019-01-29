package com.example.scio

import com.google.datastore.v1.client.DatastoreHelper.makeKey
import com.spotify.scio._
import com.spotify.scio.runners.dataflow.DataflowResult
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.io.gcp.datastore.DatastoreIO
import shapeless.datatype.datastore._

/**
  * Delete entities from DataStore
  *
  * (if non GCP) GOOGLE_APPLICATION_CREDENTIALS=path/to/cred.json
  * --project=mypj-11ffd --runner=DataflowRunner
  */
object DataStoreDeleteEntities {

  case class DummyEntity()

  private val entityType = DatastoreType[DummyEntity]

  def main(cmdline: Array[String]): Unit = {

    val (sc, args) = ContextAndArgs(cmdline)

    val opt = sc.options.as(classOf[DataflowPipelineOptions])
    val projectId = opt.getProject

    sc.parallelize(1 to 5)
      .map(e => {
        entityType.toEntityBuilder(DummyEntity())
          .setKey(makeKey("MyEntity", java.lang.Long.valueOf(e)))
          .build()
      })
      .saveAsCustomOutput("deleteEntity", DatastoreIO.v1().deleteEntity().withProjectId(projectId))

    val r = sc.close()
    val rr = r.as[DataflowResult]

    System.err.println(rr.getJob.getId)
    r.waitUntilFinish()
  }
}
