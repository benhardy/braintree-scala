package com.braintreegateway

import scala.math.BigDecimal

class FakeModificationRequest extends BaseRequest {

  private var amount: Option[BigDecimal] = None
  private var description: Option[String] = None
  private var id: Option[String] = None
  private var kind: Option[String] = None
  private var name: Option[String] = None
  private var neverExpires: Option[Boolean] = None
  private var numberOfBillingCycles: Option[Int] = None
  private var planId: Option[String] = None

  def amount(amount: BigDecimal): FakeModificationRequest = {
    this.amount = Option(amount)
    this
  }

  def kind(kind: String): FakeModificationRequest = {
    this.kind = Option(kind)
    this
  }

  def name(name: String): FakeModificationRequest = {
    this.name = Option(name)
    this
  }

  def description(description: String): FakeModificationRequest = {
    this.description = Option(description)
    this
  }

  def id(id: String): FakeModificationRequest = {
    this.id = Option(id)
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Integer): FakeModificationRequest = {
    this.numberOfBillingCycles = Option(numberOfBillingCycles)
    this
  }

  def neverExpires(neverExpires: Boolean): FakeModificationRequest = {
    this.neverExpires = Some(neverExpires)
    this
  }

  def planId(planId: String): FakeModificationRequest = {
    this.planId = Option(planId)
    this
  }

  override def toXmlString: String = {
    buildRequest("modification").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("amount", amount).
      addElement("description", description).
      addElement("id", id).
      addElement("kind", kind).
      addElement("name", name).
      addElement("neverExpires", neverExpires).
      addElement("numberOfBillingCycles", numberOfBillingCycles).
      addElement("planId", planId)
  }

}