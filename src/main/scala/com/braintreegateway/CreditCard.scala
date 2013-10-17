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
  val isDefault = node.findBoolean("default")
  val isVenmoSdk = node.findBoolean("venmo-sdk")
  val isExpired = node.findBoolean("expired")
  val last4 = node.findString("last-4")
  val commercial = KindIndicator(node.findString("commercial"))
  val debit = KindIndicator(node.findString("debit"))
  val durbinRegulated = KindIndicator(node.findString("durbin-regulated"))
  val healthcare = KindIndicator(node.findString("healthcare"))
  val payroll = KindIndicator(node.findString("payroll"))
  val prepaid = KindIndicator(node.findString("prepaid"))
  val countryOfIssuance = node.findString("country-of-issuance")
  val issuingBank = node.findString("issuing-bank")
  val uniqueNumberIdentifier = node.findString("unique-number-identifier")
  val billingAddressResponse: NodeWrapper = node.findFirst("billing-address")
  val billingAddress = if (billingAddressResponse != null) {
    new Address(billingAddressResponse)
  } else null

  val subscriptions = {
    import scala.collection.JavaConversions._
    node.findAll("subscriptions/subscription").map{ new Subscription(_) }.toList
  }

  def getBillingAddress = billingAddress

  def getBin = bin

  def getCardholderName = cardholderName

  def getCardType = cardType

  def getCreatedAt = createdAt

  def getCustomerId = customerId

  def getCustomerLocation = customerLocation

  def getExpirationDate = expirationMonth + "/" + expirationYear

  def getExpirationMonth = expirationMonth

  def getExpirationYear = expirationYear

  def getImageUrl = imageUrl

  def getLast4 = last4

  def getMaskedNumber = getBin + "******" + getLast4

  def getCommercial = commercial

  def getDebit = debit

  def getDurbinRegulated = durbinRegulated

  def getHealthcare = healthcare

  def getPayroll = payroll

  def getPrepaid = prepaid

  def getCountryOfIssuance: String = {
    if (countryOfIssuance == "") {
      "Unknown"
    }
    else {
      countryOfIssuance
    }
  }

  def getIssuingBank: String = {
    if (issuingBank == "") {
      "Unknown"
    }
    else {
      issuingBank
    }
  }

  def getUniqueNumberIdentifier = uniqueNumberIdentifier

  def getSubscriptions: List[Subscription] = {
    subscriptions
  }

  def getToken = token

  def getUpdatedAt = updatedAt

}