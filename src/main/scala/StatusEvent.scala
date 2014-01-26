package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.EnumUtils
import net.bhardy.braintree.scala.util.NodeWrapper

class StatusEvent(node: NodeWrapper) {
  val amount = node.findBigDecimal("amount")
  val status = EnumUtils.findByNameOpt(classOf[Transactions.Status])(node("status"))
  val timestamp = node.findDateTime("timestamp")
  val source = EnumUtils.findByNameOpt(classOf[Transactions.Source])(node("source"))
  val user = node.findString("user")
}