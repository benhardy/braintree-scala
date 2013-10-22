package com.braintreegateway.search

import com.braintreegateway.CreditCards

class CreditCardVerificationSearchRequest extends SearchRequest[CreditCardVerificationSearchRequest] {
  def id = textNode("id")

  def creditCardCardholderName = textNode("credit_card_cardholder_name")

  def creditCardExpirationDate = equalityNode("credit_card_expiration_date")

  def creditCardNumber = partialMatchNode("credit_card_number")

  def ids = multipleValueNode[String]("ids")

  def creditCardCardType = multipleValueNode[CreditCards.CardType]("credit_card_card_type")

  def createdAt = dateRangeNode("created_at")

  def getThis = this
}