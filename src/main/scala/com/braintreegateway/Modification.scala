package com.braintreegateway

import com.braintreegateway.util.NodeWrapper

sealed abstract class Modification(node: NodeWrapper) {

  val amount = node.findBigDecimal("amount")
  val description = node.findString("description")
  val id = node.findString("id")
  val kind = node.findString("kind")    // this doesn't seem to be actually useful TODO
  val quantity = node.findInteger("quantity")
  val name = node.findString("name")
  val neverExpires = node.findBoolean("never-expires")
  val numberOfBillingCycles = node.findInteger("number-of-billing-cycles")
  val planId = node.findString("plan-id")
}

class AddOn(node:NodeWrapper) extends Modification(node)

class Discount(node:NodeWrapper) extends Modification(node)
