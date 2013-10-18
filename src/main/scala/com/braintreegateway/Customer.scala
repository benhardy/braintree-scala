package com.braintreegateway

import com.braintreegateway.util.NodeWrapper
import scala.collection.JavaConversions._

class Customer(node: NodeWrapper) {
  val id = node.findString("id")
  val firstName = node.findString("first-name")
  val lastName = node.findString("last-name")
  val company = node.findString("company")
  val email = node.findString("email")
  val fax = node.findString("fax")
  val phone = node.findString("phone")
  val website = node.findString("website")
  val createdAt = node.findDateTime("created-at")
  val updatedAt = node.findDateTime("updated-at")
  val customFields = node.findMap("custom-fields/*")
  val creditCards = node.findAll("credit-cards/credit-card").map {
    creditCardResponse => new CreditCard(creditCardResponse)
  }.toList

  val addresses = node.findAll("addresses/address").map {
    addressResponse => new Address(addressResponse)
  }.toList

  def getCreatedAt = createdAt

  def getUpdatedAt = updatedAt

  def getId = id

  def getCompany = company

  def getCustomFields = customFields

  def getFirstName = firstName

  def getLastName = lastName

  def getEmail = email

  def getFax = fax

  def getPhone = phone

  def getWebsite = website

  def getAddresses = addresses

  def getCreditCards = creditCards
}