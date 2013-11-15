package com.braintreegateway

import com.braintreegateway.gw.BraintreeGateway
import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper

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
  val escrowStatus = EnumUtils.findByName(classOf[Transactions.EscrowStatus], node.findString("escrow-status"))
  val gatewayRejectionReason = EnumUtils.findByName(classOf[Transactions.GatewayRejectionReason], node.findString("gateway-rejection-reason"))
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
  val status = EnumUtils.findByName(classOf[Transactions.Status], node.findString("status"))
  val subscription = new Subscription(node.findFirst("subscription"))
  val subscriptionId = node.findString("subscription-id")
  val taxAmount = node.findBigDecimal("tax-amount")
  val taxExempt = node.findBoolean("tax-exempt")
  val `type` = EnumUtils.findByName(classOf[Transactions.Type], node.findString("type"))
  val updatedAt = node.findDateTime("updated-at")
  val refundIds = node.findAll("refund-ids/item").map {
    _.findString(".")
  }.toList

  val statusHistory = node.findAll("status-history/status-event").map {
    new StatusEvent(_)
  }

  val addOns = node.findAll("add-ons/add-on").map {
    new AddOn(_)
  }.toList

  val discounts = node.findAll("discounts/discount").map {
    new Discount(_)
  }.toList

  // TODO lose the getters
  def getAddOns = addOns

  def getAmount = amount

  def getAvsErrorResponseCode = avsErrorResponseCode

  def getAvsPostalCodeResponseCode = avsPostalCodeResponseCode

  def getAvsStreetAddressResponseCode = avsStreetAddressResponseCode

  def getBillingAddress = billingAddress

  def getChannel = channel

  def getCreatedAt = createdAt

  def getCreditCard = creditCard

  def getCurrencyIsoCode = currencyIsoCode

  def getCustomer = customer

  def getCustomFields = customFields

  def getCvvResponseCode = cvvResponseCode

  def getDisbursementDetails = disbursementDetails

  def getDescriptor = descriptor

  def getDiscounts = discounts

  def getEscrowStatus = escrowStatus

  def getGatewayRejectionReason = gatewayRejectionReason

  def getId = id

  def getMerchantAccountId = merchantAccountId

  def getOrderId = orderId

  def getPlanId = planId

  def getProcessorAuthorizationCode = processorAuthorizationCode

  def getProcessorResponseCode = processorResponseCode

  def getProcessorResponseText = processorResponseText

  def getPurchaseOrderNumber = purchaseOrderNumber

  def getRefundedTransactionId = refundedTransactionId

  def getServiceFeeAmount = serviceFeeAmount

  def getSettlementBatchId = settlementBatchId

  def getShippingAddress = shippingAddress

  def getStatus = status

  def getStatusHistory = statusHistory

  def getSubscriptionId = subscriptionId

  def getSubscription = subscription

  def getTaxAmount = taxAmount

  def getType = `type`

  def getRecurring = recurring

  def getUpdatedAt = updatedAt

  def getVaultBillingAddress(implicit gateway: BraintreeGateway): Option[Address] = {
    for {
      billingAddressId <- Option(billingAddress.id)
      address <- Option(gateway.address.find(customer.getId, billingAddressId))
    } yield address
  }

  def getVaultCreditCard(implicit gateway: BraintreeGateway): Option[CreditCard] = {
    for {
      cardToken <- Option(creditCard.getToken)
      card <- Option(gateway.creditCard.find(cardToken))
    } yield card
  }

  def getVaultCustomer(implicit gateway: BraintreeGateway): Option[Customer] = {
    for {
      customerId <- Option(customer.getId)
      customer <- Option(gateway.customer.find(customerId))
    } yield customer
  }

  def getVaultShippingAddress(implicit gateway: BraintreeGateway): Option[Address] = {
    for {
      addressId <- Option(shippingAddress.id)
      address <- Option(gateway.address.find(customer.getId, addressId))
    } yield address
  }

  def isTaxExempt = taxExempt

  def isDisbursed = getDisbursementDetails.isValid
}