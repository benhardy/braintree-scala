package com.braintreegateway.search

import com.braintreegateway.Subscriptions

/**
 * Provides a fluent interface to build up requests around {@link com.braintreegateway.Subscription}
 * searches.
 */
class SubscriptionSearchRequest extends SearchRequest[SubscriptionSearchRequest] {

  def daysPastDue = rangeNode("days_past_due")

  def id = textNode("id")

  def ids = multipleValueNode[String]("ids")

  def inTrialPeriod = multipleValueNode[Boolean]("in_trial_period")

  def merchantAccountId = multipleValueNode[String]("merchant_account_id")

  def nextBillingDate = dateRangeNode("next_billing_date")

  def planId = multipleValueOrTextNode[String]("plan_id")

  def price = rangeNode("price")

  def status = multipleValueNode[Subscriptions.Status]("status")

  def transactionId = textNode("transaction-id")

  def billingCyclesRemaining = rangeNode("billing_cycles_remaining")

  def getThis = this
}