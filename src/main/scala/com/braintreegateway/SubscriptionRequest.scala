package com.braintreegateway

import scala.math.BigDecimal
import java.util.Calendar

/**
 * Provides a fluent interface to build up requests around {@link Subscription
 * Subscriptions}.
 */
class SubscriptionRequest extends BaseRequest {

  private var addOnsRequest: ModificationsRequest = null
  private var billingDayOfMonth: Integer = null
  private var descriptorRequest: DescriptorRequest[SubscriptionRequest] = null
  private var discountsRequest: ModificationsRequest = null
  private var firstBillingDate: Calendar = null
  private var hasTrialPeriod: java.lang.Boolean = null
  private var id: String = null
  private var merchantAccountId: String = null
  private var neverExpires: java.lang.Boolean = null
  private var numberOfBillingCycles: Integer = null
  private var _options: SubscriptionOptionsRequest = null
  private var paymentMethodToken: String = null
  private var planId: String = null
  private var price: BigDecimal = null
  private var trialDuration: Integer = null
  private var trialDurationUnit: Subscriptions.DurationUnit = null

  def addOns: ModificationsRequest = {
    addOnsRequest = new ModificationsRequest(this, "addOns")
    addOnsRequest
  }

  def billingDayOfMonth(billingDayOfMonth: Integer): SubscriptionRequest = {
    this.billingDayOfMonth = billingDayOfMonth
    this
  }

  def descriptor: DescriptorRequest[SubscriptionRequest] = {
    descriptorRequest = DescriptorRequest.apply(this)
    descriptorRequest
  }

  def discounts: ModificationsRequest = {
    discountsRequest = new ModificationsRequest(this, "discounts")
    discountsRequest
  }

  def firstBillingDate(firstBillingDate: Calendar): SubscriptionRequest = {
    this.firstBillingDate = firstBillingDate
    this
  }

  def id(id: String): SubscriptionRequest = {
    this.id = id
    this
  }

  def merchantAccountId(merchantAccountId: String): SubscriptionRequest = {
    this.merchantAccountId = merchantAccountId
    this
  }

  def neverExpires(neverExpires: Boolean): SubscriptionRequest = {
    this.neverExpires = neverExpires
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Integer): SubscriptionRequest = {
    this.numberOfBillingCycles = numberOfBillingCycles
    this
  }

  def options: SubscriptionOptionsRequest = {
    _options = new SubscriptionOptionsRequest(this)
    _options
  }

  def paymentMethodToken(token: String): SubscriptionRequest = {
    this.paymentMethodToken = token
    this
  }

  def planId(id: String): SubscriptionRequest = {
    this.planId = id
    this
  }

  def price(price: BigDecimal): SubscriptionRequest = {
    this.price = price
    this
  }

  override def toXmlString: String = {
    buildRequest("subscription").toXmlString
  }

  def trialDuration(trialDuration: Integer): SubscriptionRequest = {
    this.trialDuration = trialDuration
    this
  }

  def trialDurationUnit(trialDurationUnit: Subscriptions.DurationUnit): SubscriptionRequest = {
    this.trialDurationUnit = trialDurationUnit
    this
  }

  def trialPeriod(hasTrialPeriod: Boolean): SubscriptionRequest = {
    this.hasTrialPeriod = hasTrialPeriod
    this
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
        addElement("id", id).
        addElement("addOns", addOnsRequest).
        addElement("billingDayOfMonth", billingDayOfMonth).
        addElement("descriptor", descriptorRequest).
        addElement("discounts", discountsRequest).
        addElement("firstBillingDate", firstBillingDate).
        addElement("merchantAccountId", merchantAccountId).
        addElement("neverExpires", neverExpires).
        addElement("numberOfBillingCycles", numberOfBillingCycles).
        addElement("options", options).
        addElement("paymentMethodToken", paymentMethodToken).
        addElement("planId", planId).
        addElement("price", price).
        addElement("trialPeriod", hasTrialPeriod).
        addElement("trialDuration", trialDuration).
        addLowerCaseElementIfPresent("trialDurationUnit", trialDurationUnit)
  }
}