package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.gw.BraintreeGateway
import net.bhardy.braintree.scala.util.EnumUtils
import net.bhardy.braintree.scala.util.NodeWrapper

class Transaction(node: NodeWrapper) {
  val amount = node.findBigDecimal("amount")
  val avsErrorResponseCode = node.findString("avs-error-response-code")
  val avsPostalCodeResponseCode = node.findString("avs-postal-code-response-code")
  val avsStreetAddressResponseCode = node.findString("avs-street-address-response-code")
  val billingAddress = new Address(node.findFirst("billing"))
  val channel = node.findString("channel")
  val createdAt = node.findDateTime("created-at")
  val creditCard = new CreditCard(node.findFirst("credit-card"))
  val currencyIsoCode = node.findString("currency-iso-code")
  val customFields = node.findMap("custom-fields/*")
  val customer = new Customer(node.findFirst("customer"))
  val cvvResponseCode = node.findString("cvv-response-code")
  val disbursementDetails = new DisbursementDetails(node.findFirst("disbursement-details"))
  val descriptor = Descriptor.apply(node.findFirst("descriptor"))
  val escrowStatus = EnumUtils.findByNameOpt(classOf[Transactions.EscrowStatus])(node("escrow-status"))
  val gatewayRejectionReason = EnumUtils.findByNameOpt(classOf[Transactions.GatewayRejectionReason])(node("gateway-rejection-reason"))
  val id = node.findString("id")
  val merchantAccountId = node.findString("merchant-account-id")
  val orderId = node.findString("order-id")
  val planId = node.findString("plan-id")
  val processorAuthorizationCode = node.findString("processor-authorization-code")
  val processorResponseCode = node.findString("processor-response-code")
  val processorResponseText = node.findString("processor-response-text")
  val purchaseOrderNumber = node.findString("purchase-order-number")
  val recurring = node.findBoolean("recurring")
  val refundedTransactionId = node.findString("refunded-transaction-id")
  val refundId = node.findString("refund-id")
  val serviceFeeAmount = node.findBigDecimal("service-fee-amount")
  val settlementBatchId = node.findString("settlement-batch-id")
  val shippingAddress = new Address(node.findFirst("shipping"))
  val status = EnumUtils.findByNameOpt(classOf[Transactions.Status])(node("status"))
  val subscription = new Subscription(node.findFirst("subscription"))
  val subscriptionId = node.findString("subscription-id")
  val taxAmount = node.findBigDecimal("tax-amount")
  val taxExempt = node.findBoolean("tax-exempt")
  val transactionType = EnumUtils.findByNameOpt(classOf[Transactions.Type])(node("type"))
  val updatedAt = node.findDateTime("updated-at")

  val refundIds = node.findAll("refund-ids/item").map {
    _.findString(".")
  }

  val statusHistory = node.findAll("status-history/status-event").map {
    new StatusEvent(_)
  }

  val addOns = node.findAll("add-ons/add-on").map {
    new AddOn(_)
  }.toList

  val discounts = node.findAll("discounts/discount").map {
    new Discount(_)
  }.toList

  def getVaultBillingAddress(implicit gateway: BraintreeGateway): Option[Address] = {
    for {
      billingAddressId <- Option(billingAddress.id)
      address <- Option(gateway.address.find(customer.id, billingAddressId))
    } yield address
  }

  def getVaultCreditCard(implicit gateway: BraintreeGateway): Option[CreditCard] = {
    for {
      cardToken <- Option(creditCard.token)
      card <- Option(gateway.creditCard.find(cardToken))
    } yield card
  }

  def getVaultCustomer(implicit gateway: BraintreeGateway): Option[Customer] = {
    for {
      customerId <- Option(customer.id)
      customer <- Option(gateway.customer.find(customerId))
    } yield customer
  }

  def getVaultShippingAddress(implicit gateway: BraintreeGateway): Option[Address] = {
    for {
      addressId <- Option(shippingAddress.id)
      address <- Option(gateway.address.find(customer.id, addressId))
    } yield address
  }

  def isTaxExempt = taxExempt

  def isDisbursed = disbursementDetails.isValid
}