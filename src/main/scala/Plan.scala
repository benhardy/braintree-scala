package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.NodeWrapper

object Plan {

  sealed trait DurationUnit {}
  object DurationUnit {
    case object DAY extends DurationUnit
    case object MONTH extends DurationUnit
    case object UNRECOGNIZED extends DurationUnit
    case object UNDEFINED extends DurationUnit

    def fromString(from:String): DurationUnit = {
      from.toUpperCase match {
        case "DAY" => DAY
        case "MONTH" => MONTH
        case _ => UNRECOGNIZED
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
  val trialPeriod = node.findBooleanOpt("trial-period").getOrElse(false)
  val trialDuration = node.findInteger("trial-duration")
  val trialDurationUnit = node.findStringOpt("trial-duration-unit").
    map(Plan.DurationUnit.fromString).
    getOrElse(Plan.DurationUnit.UNDEFINED)
  val updatedAt = node.findDateTime("updated-at")
}