package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.NodeWrapper

final class DisbursementDetails(node: NodeWrapper) {

  val disbursementDate = node.findDateOpt("disbursement-date")
  val settlementCurrencyIsoCode = node.findString("settlement-currency-iso-code")
  val isFundsHeld = node.findBoolean("funds-held")
  val settlementCurrencyExchangeRate = node.findBigDecimal("settlement-currency-exchange-rate")
  val settlementAmount = node.findBigDecimal("settlement-amount")

  def isValid = disbursementDate.isDefined

}