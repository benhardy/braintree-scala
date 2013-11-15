package com.braintreegateway

import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper

class Subscription(node: NodeWrapper) {
  val addOns = node.findAll("add-ons/add-on").map { new AddOn(_) }
  val balance = node.findBigDecimal("balance")
  val billingDayOfMonth = node.findInteger("billing-day-of-month")
  val billingPeriodEndDate = node.findDate("billing-period-end-date")
  val billingPeriodStartDate = node.findDate("billing-period-start-date")
  val currentBillingCycle = node.findInteger("current-billing-cycle")
  val daysPastDue = node.findInteger("days-past-due")
  val descriptor = node.findFirstOpt("descriptor").map { Descriptor.apply }
  val discounts = node.findAll("discounts/discount").map { new Discount(_) }
  val failureCount = node.findInteger("failure-count")
  val firstBillingDate = node.findDate("first-billing-date")
  val id = node.findString("id")
  val merchantAccountId = node.findString("merchant-account-id")
  val neverExpires = node.findBoolean("never-expires")
  val nextBillingDate = node.findDate("next-billing-date")
  val nextBillingPeriodAmount = node.findBigDecimal("next-billing-period-amount")
  val numberOfBillingCycles = node.findInteger("number-of-billing-cycles")
  val paidThroughDate = node.findDate("paid-through-date")
  val paymentMethodToken = node.findString("payment-method-token")
  val planId = node.findString("plan-id")
  val price = node.findBigDecimal("price")
  val status = EnumUtils.findByName(classOf[Subscriptions.Status], node.findString("status"))
  val hasTrialPeriod = node.findBoolean("trial-period")
  val trialDuration = node.findInteger("trial-duration")
  val trialDurationUnit = EnumUtils.findByName(classOf[Subscriptions.DurationUnit], node.findString("trial-duration-unit"))
  val transactions = node.findAll("transactions/transaction").map{ new Transaction(_) }
}