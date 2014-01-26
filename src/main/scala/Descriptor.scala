package net.bhardy.braintree.scala

import util.NodeWrapper

/**
 * An address can belong to:
 * <ul>
 * <li>a CreditCard as the billing address
 * <li>a Customer as an address
 * <li>a Transaction as a billing or shipping address
 * </ul>
 * 
 */
case class Descriptor(name:String, phone:String)

object Descriptor {
  def apply(node:NodeWrapper): Descriptor = Descriptor(
    name = node.findString("name"),
    phone = node.findString("phone")
  )
}
