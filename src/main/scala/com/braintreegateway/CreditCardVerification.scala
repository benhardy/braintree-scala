package com.braintreegateway

import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.CreditCardVerification.Status

object CreditCardVerification {

  sealed trait Status

  object Status {
    case object FAILED extends Status
    case object GATEWAY_REJECTED extends Status
    case object PROCESSOR_DECLINED extends Status
    case object VERIFIED extends Status

    case object UNRECOGNIZED extends Status
    case object UNDEFINED extends Status

    val values: List[Status] = FAILED :: GATEWAY_REJECTED :: PROCESSOR_DECLINED :: VERIFIED :: Nil

    def fromString(from:String) = {
      Option(from).map { s =>
        val up = s.toUpperCase
        val possible: Option[Status] = values.find(_.toString == up)
        possible.getOrElse(Status.UNRECOGNIZED)
      }.getOrElse(UNDEFINED)
    }
  }
}

final class CreditCardVerification(node: NodeWrapper) {
  val avsErrorResponseCode = node.findString("avs-error-response-code")
  val avsPostalCodeResponseCode = node.findString("avs-postal-code-response-code")
  val avsStreetAddressResponseCode = node.findString("avs-street-address-response-code")
  val cvvResponseCode = node.findString("cvv-response-code")
  val gatewayRejectionReason = EnumUtils.findByName(classOf[Transactions.GatewayRejectionReason], node.findString("gateway-rejection-reason"))
  val processorResponseCode = node.findString("processor-response-code")
  val processorResponseText = node.findString("processor-response-text")
  val merchantAccountId = node.findString("merchant-account-id")
  val status = node.findStringOpt("status") map { Status.fromString } getOrElse (Status.UNDEFINED)
  val id = node.findString("id")
  val creditCard = new CreditCard(node.findFirst("credit-card"))
  val billingAddress = new Address(node.findFirst("billing"))
  val createdAt = node.findDateTime("created-at")
}