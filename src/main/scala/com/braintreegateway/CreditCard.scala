package com.braintreegateway

import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.CreditCard._
import com.braintreegateway.CreditCards.CardType

sealed abstract class CustomerLocation(override val toString:String)
object CustomerLocation {
  case object INTERNATIONAL extends CustomerLocation("international")
  case object US extends CustomerLocation("us")
  case object UNRECOGNIZED extends CustomerLocation("unrecognized")
  case object UNDEFINED extends CustomerLocation("undefined")
}

object CreditCard {

  sealed abstract class KindIndicator(override val toString:String)
  object KindIndicator {
    case object YES extends KindIndicator("Yes")
    case object NO extends KindIndicator("No")
    case object UNKNOWN extends KindIndicator("Unknown")

    def apply(s:String): KindIndicator = {
      if (YES.toString.equalsIgnoreCase(s)) YES
      else if (NO.toString.equalsIgnoreCase(s)) NO
      else UNKNOWN
    }
  }

  def lookupCardType(typeString: String): CreditCards.CardType = {
    Option(typeString).map {
      CardType.lookup(_).getOrElse(CardType.UNRECOGNIZED)
    }.getOrElse {CardType.UNDEFINED}
  }
}

class CreditCard(node: NodeWrapper) {
  val token = node.findString("token")
  val createdAt = node.findDateTime("created-at")
  val updatedAt = node.findDateTime("updated-at")
  val bin = node.findString("bin")
  val cardType: CardType = lookupCardType(node.findString("card-type"))
  val cardholderName = node.findString("cardholder-name")
  val customerId = node.findString("customer-id")
  val customerLocation = node.findString("customer-location")
  val expirationMonth = node.findString("expiration-month")
  val expirationYear = node.findString("expiration-year")
  val imageUrl = node.findString("image-url")
  val isDefault = node.findBooleanOpt("default").getOrElse(false)
  val isVenmoSdk = node.findBooleanOpt("venmo-sdk").getOrElse(false)
  val isExpired = node.findBooleanOpt("expired").getOrElse(false)
  val last4 = node.findString("last-4")
  val commercial = KindIndicator(node.findString("commercial"))
  val debit = KindIndicator(node.findString("debit"))
  val durbinRegulated = KindIndicator(node.findString("durbin-regulated"))
  val healthcare = KindIndicator(node.findString("healthcare"))
  val payroll = KindIndicator(node.findString("payroll"))
  val prepaid = KindIndicator(node.findString("prepaid"))

  val countryOfIssuance = node.findStringOpt("country-of-issuance").filter{!_.isEmpty}.getOrElse("Unknown")
  val issuingBank = node.findStringOpt("issuing-bank").filter{!_.isEmpty}.getOrElse("Unknown")

  val uniqueNumberIdentifier = node.findString("unique-number-identifier")
  val billingAddressResponse: Option[NodeWrapper] = node.findFirstOpt("billing-address")

  val billingAddress = billingAddressResponse.map { new Address(_) }

  val subscriptions = node.findAll("subscriptions/subscription").map{ new Subscription(_) }

  def expirationDate = expirationMonth + "/" + expirationYear

  def maskedNumber = bin + "******" + last4
}