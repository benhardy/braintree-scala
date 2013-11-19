package com.braintreegateway

import scala.math.BigDecimal
import java.util.Calendar

/**
 * Provides a fluent interface to build up requests around {@link Subscription
 * Subscriptions}.
 */
class SubscriptionRequest extends BaseRequest {

  private var addOnsRequest: Option[ModificationsRequest] = None
  private var billingDayOfMonth: Option[Integer] = None
  private var descriptorRequest: Option[DescriptorRequest[SubscriptionRequest]] = None
  private var discountsRequest: Option[ModificationsRequest] = None
  private var firstBillingDate: Option[Calendar] = None
  private var hasTrialPeriod: Option[Boolean] = None
  private var id: Option[String] = None
  private var merchantAccountId: Option[String] = None
  private var neverExpires: Option[Boolean] = None
  private var numberOfBillingCycles: Option[Integer] = None
  private var _options: Option[SubscriptionOptionsRequest] = None
  private var paymentMethodToken: Option[String] = None
  private var planId: Option[String] = None
  private var price: Option[BigDecimal] = None
  private var trialDuration: Option[Integer] = None
  private var trialDurationUnit: Option[Subscriptions.DurationUnit] = None

  def addOns: ModificationsRequest = {
    val subRequest = new ModificationsRequest(this, "addOns")
    this.addOnsRequest = Some(subRequest)
    subRequest
  }

  def billingDayOfMonth(billingDayOfMonth: Integer): SubscriptionRequest = {
    this.billingDayOfMonth = Some(billingDayOfMonth)
    this
  }

  def descriptor: DescriptorRequest[SubscriptionRequest] = {
    val subRequest = DescriptorRequest.apply(this)
    this.descriptorRequest = Some(subRequest)
    subRequest
  }

  def discounts: ModificationsRequest = {
    val subRequest = new ModificationsRequest(this, "discounts")
    discountsRequest = Some(subRequest)
    subRequest
  }

  def firstBillingDate(firstBillingDate: Calendar): SubscriptionRequest = {
    this.firstBillingDate = Some(firstBillingDate)
    this
  }

  def id(id: String): SubscriptionRequest = {
    this.id = Some(id)
    this
  }

  def merchantAccountId(merchantAccountId: String): SubscriptionRequest = {
    this.merchantAccountId = Some(merchantAccountId)
    this
  }

  def neverExpires(neverExpires: Boolean): SubscriptionRequest = {
    this.neverExpires = Some(neverExpires)
    this
  }

  def numberOfBillingCycles(numberOfBillingCycles: Integer): SubscriptionRequest = {
    this.numberOfBillingCycles = Some(numberOfBillingCycles)
    this
  }

  def options: SubscriptionOptionsRequest = {
    val subRequest = new SubscriptionOptionsRequest(this)
    this._options = Some(subRequest)
    subRequest
  }

  def paymentMethodToken(token: String): SubscriptionRequest = {
    this.paymentMethodToken = Some(token)
    this
  }

  def planId(id: String): SubscriptionRequest = {
    this.planId = Some(id)
    this
  }

  def price(price: BigDecimal): SubscriptionRequest = {
    this.price = Some(price)
    this
  }

  override def toXmlString: String = {
    buildRequest("subscription").toXmlString
  }

  def trialDuration(trialDuration: Integer): SubscriptionRequest = {
    this.trialDuration = Some(trialDuration)
    this
  }

  def trialDurationUnit(trialDurationUnit: Subscriptions.DurationUnit): SubscriptionRequest = {
    this.trialDurationUnit = Some(trialDurationUnit)
    this
  }

  def trialPeriod(hasTrialPeriod: Boolean): SubscriptionRequest = {
    this.hasTrialPeriod = Some(hasTrialPeriod)
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
        addElement("trialDurationUnit", trialDurationUnit.map {_.toString.toLowerCase })
  }
}