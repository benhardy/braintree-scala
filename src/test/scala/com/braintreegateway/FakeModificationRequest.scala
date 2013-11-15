package com.braintreegateway

import scala.math.BigDecimal

class FakeModificationRequest extends BaseRequest {

  private var amount: BigDecimal = null
  private var description: String = null
  private var id: String = null
  private var kind: String = null
  private var name: String = null
  private var neverExpires: java.lang.Boolean = null
  private var numberOfBillingCycles: Integer = null
  private var planId: String = null

  def amount(amount: BigDecimal): FakeModificationRequest = {
    this.amount = amount
    this
  }

  def kind(kind: String): FakeModificationRequest = {
    this.kind = kind
    this
  }

  def name(name: String): FakeModificationRequest = {
    this.name = name
    this
  }

  def description(description: String): FakeModificationRequest = {
    this.description = description
    this
  }

  def id(id: String): FakeModificationRequest = {
    this.id = id
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Integer): FakeModificationRequest = {
    this.numberOfBillingCycles = numberOfBillingCycles
    this
  }

  def neverExpires(neverExpires: Boolean): FakeModificationRequest = {
    this.neverExpires = neverExpires
    this
  }

  def planId(planId: String): FakeModificationRequest = {
    this.planId = planId
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