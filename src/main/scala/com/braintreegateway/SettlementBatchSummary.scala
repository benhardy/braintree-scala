package com.braintreegateway

import com.braintreegateway.util.NodeWrapper


case class SettlementBatchSummary private(records: List[Map[String, String]]) {

}

object SettlementBatchSummary {

  import scala.collection.JavaConversions._

  def apply(root: NodeWrapper) = {
    val records = root.findAll("records/record").toList.map {
      node => node.findMapOpt("*").toMap
    }
    new SettlementBatchSummary(records)
  }
}
