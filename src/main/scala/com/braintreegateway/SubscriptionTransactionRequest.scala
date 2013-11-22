package com.braintreegateway

import scala.math.BigDecimal

class SubscriptionTransactionRequest extends BaseRequest {

  private var amount: Option[BigDecimal] = None
  private var subscriptionId: Option[String] = None

  def amount(amount: BigDecimal): SubscriptionTransactionRequest = {
    this.amount = Option(amount)
    this
  }

  def subscriptionId(subscriptionId: String): SubscriptionTransactionRequest = {
    this.subscriptionId = Option(subscriptionId)
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
}