package com.example.scio

import org.apache.beam.sdk.options.{Default, Description, PipelineOptions}

trait MyOption extends PipelineOptions {
  @Description("Limit of query result")
  @Override
  @Default.Integer(1000)
  def getLimit: Int

  def setLimit(value: Int): Unit


  @Description("Output destination. Can be gs://bucket/key")
  @Override
  @Default.String("out")
  def getOutput: String

  def setOutput(value: String): Unit

  @Override
  @Default.String("MyEntity")
  def getKind: String

  def setKind(value: String): Unit
}