package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.NodeWrapper


case class SettlementBatchSummary private(records: List[Map[String, String]]) {

}

object SettlementBatchSummary {

  def apply(root: NodeWrapper) = {
    val records = root.findAll("records/record").map {
      node => node.findMapOpt("*").toMap
    }
    new SettlementBatchSummary(records)
  }
}
