package com.example.scio

import java.time.Instant

import com.google.datastore.v1.Query
import com.google.datastore.v1.Value.ValueTypeCase
import com.spotify.scio._
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions

import scala.collection.JavaConverters._
import scala.collection.immutable.TreeMap

/**
  * Enum all entities from the kind of DataStore
  *
  * (if non GCP) GOOGLE_APPLICATION_CREDENTIALS=path/to/cred.json
  * --project=mypj-11ffd --runner=DataflowRunner --kind=MyEntity
  */
object DataStoreQueryEntities {

  def main(cmdline: Array[String]): Unit = {

    val (sc, args) = ContextAndArgs(cmdline)

    val opt = sc.options.as(classOf[DataflowPipelineOptions])
    val projectId = opt.getProject

    val myOpt = sc.options.as(classOf[MyOption])

    val qb = Query.newBuilder()
    qb.addKindBuilder().setName(myOpt.getKind)
    qb.setLimit(qb.getLimitBuilder.setValue(myOpt.getLimit).build())
    sc.datastore(projectId, qb.build())
      .map(e => {
        val keyPartition = TreeMap(
          "namespace_id" -> e.getKey.getPartitionId.getNamespaceId,
          "project_id" -> e.getKey.getPartitionId.getProjectId)
          .filter(e => {
            e._2.nonEmpty
          })
        val keyPath = e.getKey.getPathList.asScala.map(e => {
          TreeMap("kind"-> e.getKind, "name" -> e.getName, "id" -> e.getId)
            .filter(e => {
              e._2 match {
                case x: String => x.nonEmpty
                case _ => true
              }
            })
            .asJava
        })
        val values = e.getPropertiesMap.asScala.mapValues(v => {
          v.getValueTypeCase match {
            case ValueTypeCase.STRING_VALUE => v.getStringValue
            case ValueTypeCase.NULL_VALUE => null
            case ValueTypeCase.INTEGER_VALUE => v.getIntegerValue
            case ValueTypeCase.BOOLEAN_VALUE => v.getBooleanValue
            case ValueTypeCase.DOUBLE_VALUE => v.getDoubleValue
            case ValueTypeCase.TIMESTAMP_VALUE => Instant.ofEpochSecond(v.getTimestampValue.getSeconds, v.getTimestampValue.getNanos).toString
            case _ => s"(${v.getValueTypeCase.toString})"
          }
        })
        s"${keyPartition.asJava}:${keyPath.asJava}:${TreeMap(values.toArray:_*).asJava}"
      }).saveAsTextFile(myOpt.getOutput)

    val r = sc.close()
    r.waitUntilFinish()
  }
}
