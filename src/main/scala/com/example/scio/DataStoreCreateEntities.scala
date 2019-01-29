package com.example.scio

import com.google.datastore.v1.client.DatastoreHelper.makeKey
import com.spotify.scio._
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.joda.time.{DateTime, Instant}
import shapeless.datatype.datastore._

/**
  * Upsert entities to DataStore
  *
  * (if non GCP) GOOGLE_APPLICATION_CREDENTIALS=path/to/cred.json
  * --project=mypj-11ffd --runner=DataflowRunner
  */
object DataStoreCreateEntities {

  case class MyEntity(id: Int, name: String, updated: Instant)

  private val entityType = DatastoreType[MyEntity]

  def main(cmdline: Array[String]): Unit = {

    val (sc, args) = ContextAndArgs(cmdline)

    val opt = sc.options.as(classOf[DataflowPipelineOptions])
    val projectId = opt.getProject
    val now = DateTime.now.toInstant

    sc.parallelize(1 to 5)
      .map(e => {
        val kb = makeKey("MyEntity", java.lang.Long.valueOf(e))
        // If want to specify namespace.
        //kb.setPartitionId(kb.getPartitionIdBuilder.setNamespaceId("myns").build())
        entityType.toEntityBuilder(MyEntity(id = e + 30, name = s"name${e}", updated = now))
          .setKey(kb)
          .build()
      })
      .saveAsDatastore(projectId)

    val r = sc.close()
    /*
    val rr = r.as[DataflowResult]
    System.err.println(rr.getJob.getId)
    */
    r.waitUntilFinish()
  }
}
