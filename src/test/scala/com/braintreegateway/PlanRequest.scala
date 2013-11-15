package com.braintreegateway

import scala.math.BigDecimal

// TODO make this idiomatic scala
// TODO figure out why this isn't in src/main
class PlanRequest extends BaseRequest {
  private var id: String = null
  private var merchantId: String = null
  private var billingDayOfMonth: Integer = null
  private var billingFrequency: Integer = null
  private var currencyIsoCode: String = null
  private var description: String = null
  private var name: String = null
  private var numberOfBillingCycles: Integer = null
  private var price: BigDecimal = null
  private var hasTrialPeriod: java.lang.Boolean = null
  private var trialDuration: Integer = null
  private var trialDurationUnit: Plan.DurationUnit = null

  def id(id: String): PlanRequest = {
    this.id = id
    this
  }

  def billingFrequency(billingFrequency: Int): PlanRequest = {
    this.billingFrequency = billingFrequency
    this
  }

  def description(description: String): PlanRequest = {
    this.description = description
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Int): PlanRequest = {
    this.numberOfBillingCycles = numberOfBillingCycles
    this
  }

  def price(price: BigDecimal): PlanRequest = {
    this.price = price
    this
  }

  def trialPeriod(trialPeriod: Boolean): PlanRequest = {
    this.hasTrialPeriod = trialPeriod
    this
  }

  def trialDuration(trialDuration: Int): PlanRequest = {
    this.trialDuration = trialDuration
    this
  }

  def trialDurationUnit(trialDurationUnit: Plan.DurationUnit): PlanRequest = {
    this.trialDurationUnit = trialDurationUnit
    this
  }

  def merchantId(merchantId: String): PlanRequest = {
    this.merchantId = merchantId
    this
  }

  def billingDayOfMonth(billingDayOfMonth: Int): PlanRequest = {
    this.billingDayOfMonth = billingDayOfMonth
    this
  }

  def currencyIsoCode(currencyIsoCode: String): PlanRequest = {
    this.currencyIsoCode = currencyIsoCode
    this
  }

  def name(name: String): PlanRequest = {
    this.name = name
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
      addElement("trialDuration", trialDuration)

    if (trialDurationUnit != null) {
      builder.addElement("trialDurationUnit", trialDurationUnit.toString.toLowerCase)
    }
    builder
  }
}