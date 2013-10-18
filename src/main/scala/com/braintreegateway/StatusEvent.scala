package com.braintreegateway

import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper

class StatusEvent(node: NodeWrapper) {
  val amount = node.findBigDecimal("amount")
  val status = EnumUtils.findByName(classOf[Transactions.Status], node.findString("status"))
  val timestamp = node.findDateTime("timestamp")
  val source = EnumUtils.findByName(classOf[Transactions.Source], node.findString("source"))
  val user = node.findString("user")
}