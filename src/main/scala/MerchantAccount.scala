package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.NodeWrapper
import net.bhardy.braintree.scala.MerchantAccount.Status

object MerchantAccount {

  sealed trait Status

  object Status {
    case object PENDING extends Status
    case object ACTIVE extends Status
    case object SUSPENDED extends Status

    private val reverse = List(PENDING, ACTIVE, SUSPENDED).map { i => i.toString -> i }.toMap

    // TODO naive but correct. refactor out common stuff later
    def from(string:String): Status = {
      reverse.get(string.toUpperCase.replace(' ', '_')).
        getOrElse { throw new IllegalArgumentException(string) }
    }
  }

  def apply(node: NodeWrapper): MerchantAccount = {
    MerchantAccount(id = node.findString("id"),
      status = Status.from(node.findStringOpt("status").getOrElse("[missing]")),
      masterMerchantAccount = node.findFirstOpt("master-merchant-account") map { apply }
    )
  }
}

/**
 * @param id - the public id of the Merchant Account
 * @param status - the merchant account status
 * @param masterMerchantAccount - Will be None if this merchant account is a master merchant account.
 */
case class MerchantAccount private (id: String, status: Status, masterMerchantAccount: Option[MerchantAccount] = None) {

  def isSubMerchant: Boolean = masterMerchantAccount.isDefined
}