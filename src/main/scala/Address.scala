package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.util.NodeWrapper

/**
 * An address can belong to:
 * <ul>
 * <li>a CreditCard as the billing address
 * <li>a Customer as an address
 * <li>a Transaction as a billing or shipping address
 * </ul>
 *
 */
class Address(node: NodeWrapper) {

  val company = node.findString("company")
  val countryCodeAlpha2 = node.findString("country-code-alpha2")
  val countryCodeAlpha3 = node.findString("country-code-alpha3")
  val countryCodeNumeric = node.findString("country-code-numeric")
  val countryName = node.findString("country-name")
  val createdAt = node.findDateTime("created-at")
  val customerId = node.findString("customer-id")
  val extendedAddress = node.findString("extended-address")
  val firstName = node.findString("first-name")
  val id = node.findString("id")
  val lastName = node.findString("last-name")
  val locality = node.findString("locality")
  val postalCode = node.findString("postal-code")
  val region = node.findString("region")
  val streetAddress = node.findString("street-address")
  val updatedAt = node.findDateTime("updated-at")
}