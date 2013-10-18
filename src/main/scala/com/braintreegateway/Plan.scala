package com.braintreegateway

import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper
import scala.collection.JavaConversions._

object Plan {

  trait DurationUnit {}
  object DurationUnit {
    case object DAY extends DurationUnit
    case object MONTH extends DurationUnit
    case object UNRECOGNIZED extends DurationUnit
    case object UNDEFINED extends DurationUnit

    def fromString(from:String): DurationUnit = {
      if (from == null) {
        UNDEFINED   // TODO null cleanup
      } else {
        from.toUpperCase match {
          case "DAY" => DAY
          case "MONTH" => MONTH
          case _ => UNRECOGNIZED
        }
      }
    }
  }

}

class Plan(node: NodeWrapper) {
  val id = node.findString("id")
  val addOns = node.findAll("add-ons/add-on") map {new AddOn(_)}
  val merchantId = node.findString("merchant-id")
  val billingDayOfMonth = node.findInteger("billing-day-of-month")
  val billingFrequency = node.findInteger("billing-frequency")
  val createdAt = node.findDateTime("created-at")
  val currencyIsoCode = node.findString("currency-iso-code")
  val description = node.findString("description")
  val discounts = node.findAll("discounts/discount") map {new Discount(_)}
  val name = node.findString("name")
  val numberOfBillingCycles = node.findInteger("number-of-billing-cycles")
  val price = node.findBigDecimal("price")
  val trialPeriod = node.findBooleanOpt("trial-period")
  val trialDuration = node.findInteger("trial-duration")
  val trialDurationUnit = Plan.DurationUnit.fromString(node.findString("trial-duration-unit"))
  val updatedAt = node.findDateTime("updated-at")

  def getAddOns = addOns
  def getBillingFrequency = billingFrequency
  def getDescription = description
  def getDiscounts = discounts
  def getId = id
  def getNumberOfBillingCycles = numberOfBillingCycles
  def getPrice = price
  def hasTrialPeriod = trialPeriod.getOrElse(false)
  def getTrialDuration = trialDuration
  def getTrialDurationUnit =  trialDurationUnit
  def getMerchantId = merchantId
  def getBillingDayOfMonth = billingDayOfMonth
  def getCurrencyIsoCode = currencyIsoCode
  def getName = name
  def getCreatedAt = createdAt
  def getUpdatedAt = updatedAt
}