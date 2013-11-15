package com.braintreegateway

import scala.math.BigDecimal

abstract class ModificationRequest(parent: ModificationsRequest) extends BaseRequest {

  private var amount: BigDecimal = null
  private var numberOfBillingCycles: Integer = null
  private var quantity: Integer = null
  private var neverExpires: java.lang.Boolean = null

  def amount(amount: BigDecimal): this.type = {
    this.amount = amount
    this
  }

  def done: ModificationsRequest = {
    parent
  }

  def numberOfBillingCycles(numberOfBillingCycles: Integer): this.type = {
    this.numberOfBillingCycles = numberOfBillingCycles
    this
  }

  def neverExpires(neverExpires: java.lang.Boolean): this.type = {
    this.neverExpires = neverExpires
    this
  }

  def quantity(quantity: Integer): this.type = {
    this.quantity = quantity
    this
  }

  override def toXmlString: String = {
    buildRequest("modification").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).addElement("amount", amount).
      addElement("neverExpires", neverExpires).
      addElement("numberOfBillingCycles", numberOfBillingCycles).
      addElement("quantity", quantity)
  }
}