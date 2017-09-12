package io.megl.akka.ingestion.domain

import io.circe.generic.JsonCodec

@JsonCodec
case class Value (val1: Int, val2: String)

@JsonCodec
case class EnrichedValue (value: Value, val3: Int)
