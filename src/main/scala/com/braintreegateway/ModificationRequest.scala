package com.braintreegateway

import scala.math.BigDecimal

abstract class ModificationRequest(parent: ModificationsRequest) extends BaseRequest {

  private var amount: Option[BigDecimal] = None
  private var numberOfBillingCycles: Option[Int] = None
  private var quantity: Option[Int] = None
  private var neverExpires: Option[Boolean] = None

  def amount(amount: BigDecimal): this.type = {
    this.amount = Option(amount)
    this
  }

  def done: ModificationsRequest = {
    parent
  }

  def numberOfBillingCycles(numberOfBillingCycles: Int): this.type = {
    this.numberOfBillingCycles = Some(numberOfBillingCycles)
    this
  }

  def neverExpires(neverExpires: Boolean): this.type = {
    this.neverExpires = Some(neverExpires)
    this
  }

  def quantity(quantity: Int): this.type = {
    this.quantity = Some(quantity)
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