package com.braintreegateway

import scala.math.BigDecimal

class SubscriptionTransactionRequest extends BaseRequest {
  def amount(amount: BigDecimal): SubscriptionTransactionRequest = {
    this.amount = Some(amount)
    this
  }

  def subscriptionId(subscriptionId: String): SubscriptionTransactionRequest = {
    this.subscriptionId = Some(subscriptionId)
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

  private var amount: Option[BigDecimal] = None
  private var subscriptionId: Option[String] = None
}