package com.braintreegateway.search

import com.braintreegateway.CreditCards
import com.braintreegateway.CustomerLocation
import com.braintreegateway.Transactions

class TransactionSearchRequest extends SearchRequest[TransactionSearchRequest] {
  def id = textNode("id")

  def billingCompany = textNode("billing_company")

  def billingCountryName = textNode("billing_country_name")

  def billingExtendedAddress = textNode("billing_extended_address")

  def billingFirstName = textNode("billing_first_name")

  def billingLastName = textNode("billing_last_name")

  def billingLocality = textNode("billing_locality")

  def billingPostalCode = textNode("billing_postal_code")

  def billingRegion = textNode("billing_region")

  def billingStreetAddress = textNode("billing_street_address")

  def creditCardCardholderName = textNode("credit_card_cardholder_name")

  def creditCardExpirationDate = equalityNode("credit_card_expiration_date")

  def creditCardNumber = partialMatchNode("credit_card_number")

  def currency = textNode("currency")

  def customerCompany = textNode("customer_company")

  def customerEmail = textNode("customer_email")

  def customerFax = textNode("customer_fax")

  def customerFirstName = textNode("customer_first_name")

  def customerId = textNode("customer_id")

  def customerLastName = textNode("customer_last_name")

  def customerPhone = textNode("customer_phone")

  def customerWebsite = textNode("customer_website")

  def ids = multipleValueNode[String]("ids")

  def orderId = textNode("order_id")

  def paymentMethodToken = textNode("payment_method_token")

  def processorAuthorizationCode = textNode("processor_authorization_code")

  def settlementBatchId = textNode("settlement_batch_id")

  def shippingCompany = textNode("shipping_company")

  def shippingCountryName = textNode("shipping_country_name")

  def shippingExtendedAddress = textNode("shipping_extended_address")

  def shippingFirstName = textNode("shipping_first_name")

  def shippingLastName = textNode("shipping_last_name")

  def shippingLocality = textNode("shipping_locality")

  def shippingPostalCode = textNode("shipping_postal_code")

  def shippingRegion = textNode("shipping_region")

  def shippingStreetAddress = textNode("shipping_street_address")

  def createdUsing = multipleValueNode[Transactions.CreatedUsing]("created_using")

  def creditCardCustomerLocation = multipleValueNode[CustomerLocation]("credit_card_customer_location")

  def merchantAccountId = multipleValueNode[String]("merchant_account_id")

  def creditCardCardType = multipleValueNode[CreditCards.CardType]("credit_card_card_type")

  def status = multipleValueNode[Transactions.Status]("status")

  def source = multipleValueNode[Transactions.Source]("source")

  def transactionType = multipleValueNode[Transactions.Type]("type")

  def refund = keyValueNode("refund")

  def amount = rangeNode("amount")

  def authorizationExpiredAt = dateRangeNode("authorization_expired_at")

  def authorizedAt = dateRangeNode("authorized_at")

  def createdAt = dateRangeNode("created_at")

  def failedAt = dateRangeNode("failed_at")

  def gatewayRejectedAt = dateRangeNode("gateway_rejected_at")

  def processorDeclinedAt = dateRangeNode("processor_declined_at")

  def settledAt = dateRangeNode("settled_at")

  def submittedForSettlementAt = dateRangeNode("submitted_for_settlement_at")

  def voidedAt = dateRangeNode("voided_at")

  def disbursementDate = dateRangeNode("disbursement_date")

  def getThis = this
}