package com.braintreegateway

import scala.math.BigDecimal

// TODO figure out why this isn't in src/main
class PlanRequest extends BaseRequest {
  private var id: Option[String] = None
  private var merchantId: Option[String] = None
  private var billingDayOfMonth: Option[Int] = None
  private var billingFrequency: Option[Int] = None
  private var currencyIsoCode: Option[String] = None
  private var description: Option[String] = None
  private var name: Option[String] = None
  private var numberOfBillingCycles: Option[Int] = None
  private var price: Option[BigDecimal] = None
  private var hasTrialPeriod: Option[Boolean] = None
  private var trialDuration: Option[Int] = None
  private var trialDurationUnit: Option[Plan.DurationUnit] = None

  def id(id: String): PlanRequest = {
    this.id = Some(id)
    this
  }

  def billingFrequency(billingFrequency: Int): PlanRequest = {
    this.billingFrequency = Some(billingFrequency)
    this
  }

  def description(description: String): PlanRequest = {
    this.description = Some(description)
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Int): PlanRequest = {
    this.numberOfBillingCycles = Some(numberOfBillingCycles)
    this
  }

  def price(price: BigDecimal): PlanRequest = {
    this.price = Some(price)
    this
  }

  def trialPeriod(trialPeriod: Boolean): PlanRequest = {
    this.hasTrialPeriod = Some(trialPeriod)
    this
  }

  def trialDuration(trialDuration: Int): PlanRequest = {
    this.trialDuration = Some(trialDuration)
    this
  }

  def trialDurationUnit(trialDurationUnit: Plan.DurationUnit): PlanRequest = {
    this.trialDurationUnit = Some(trialDurationUnit)
    this
  }

  def merchantId(merchantId: String): PlanRequest = {
    this.merchantId = Some(merchantId)
    this
  }

  def billingDayOfMonth(billingDayOfMonth: Int): PlanRequest = {
    this.billingDayOfMonth = Some(billingDayOfMonth)
    this
  }

  def currencyIsoCode(currencyIsoCode: String): PlanRequest = {
    this.currencyIsoCode = Some(currencyIsoCode)
    this
  }

  def name(name: String): PlanRequest = {
    this.name = Some(name)
    this
  }

  override def toXmlString: String = {
    buildRequest("plan").toXmlString
  }

  private def buildRequest(root: String): RequestBuilder = {
    val builder = new RequestBuilder(root).
      addElement("id", id).
      addElement("merchantId", merchantId).
      addElement("billingDayOfMonth", billingDayOfMonth).
      addElement("billingFrequency", billingFrequency).
      addElement("currencyIsoCode", currencyIsoCode).
      addElement("description", description).
      addElement("name", name).
      addElement("numberOfBillingCycles", numberOfBillingCycles).
      addElement("price", price).
      addElement("trialPeriod", hasTrialPeriod).
      addElement("trialDuration", trialDuration).
      addElement("trialDurationUnit", trialDurationUnit.map {_.toString.toLowerCase} )

    builder
  }
}