package com.braintreegateway

import scala.math.BigDecimal

class SubscriptionTransactionRequest extends BaseRequest {
  def amount(amount: BigDecimal): SubscriptionTransactionRequest = {
    this.amount = amount
    this
  }

  def subscriptionId(subscriptionId: String): SubscriptionTransactionRequest = {
    this.subscriptionId = subscriptionId
    this
  }

  override def toXmlString: String = {
    buildRequest("transaction").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("amount", amount).
      addElement("subscriptionId", subscriptionId).
      addElement("type", Transactions.Type.SALE.toString.toLowerCase)
  }

  private var amount: BigDecimal = null
  private var subscriptionId: String = null
}