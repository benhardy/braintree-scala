package com.braintreegateway.search

class CustomerSearchRequest extends SearchRequest[CustomerSearchRequest] {
  def addressCountryName = textNode("address_country_name")

  def addressExtendedAddress = textNode("address_extended_address")

  def addressFirstName = textNode("address_first_name")

  def addressLastName = textNode("address_last_name")

  def addressLocality = textNode("address_locality")

  def addressPostalCode = textNode("address_postal_code")

  def addressRegion = textNode("address_region")

  def addressStreetAddress = textNode("address_street_address")

  def cardholderName = textNode("cardholder_name")

  def company = textNode("company")

  def creditCardExpirationDate = equalityNode("credit_card_expiration_date")

  def email = textNode("email")

  def fax = textNode("fax")

  def firstName = textNode("first_name")

  def id = textNode("id")

  def lastName = textNode("last_name")

  def paymentMethodToken = textNode("payment_method_token")

  def phone = textNode("phone")

  def website = textNode("website")

  def paymentMethodTokenWithDuplicates = isNode("payment_method_token_with_duplicates")

  def ids = multipleValueNode[String]("ids")

  def creditCardNumber = partialMatchNode("credit_card_number")

  def createdAt = dateRangeNode("created_at")

  def getThis = this
}