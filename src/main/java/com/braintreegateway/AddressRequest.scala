package com.braintreegateway

/**
 * Provides a fluent interface to build up requests around {@link Address Addresses}.
 */
class AddressRequest extends BaseRequest {

  // TODO clean up nulls and vars
  private var countryCodeAlpha2: String = null
  private var countryCodeAlpha3: String = null
  private var countryCodeNumeric: String = null
  private var countryName: String = null
  private var extendedAddress: String = null
  private var firstName: String = null
  private var lastName: String = null
  private var locality: String = null
  private var postalCode: String = null
  private var region: String = null
  private var streetAddress: String = null
  private var company: String = null

  protected def tagName = "address"

  def company(company: String): this.type = {
    this.company = company
    this
  }

  def countryCodeAlpha2(countryCodeAlpha2: String): this.type = {
    this.countryCodeAlpha2 = countryCodeAlpha2
    this
  }

  def countryCodeAlpha3(countryCodeAlpha3: String): this.type = {
    this.countryCodeAlpha3 = countryCodeAlpha3
    this
  }

  def countryCodeNumeric(countryCodeNumeric: String): this.type = {
    this.countryCodeNumeric = countryCodeNumeric
    this
  }

  def countryName(countryName: String): this.type = {
    this.countryName = countryName
    this
  }

  def extendedAddress(extendedAddress: String): this.type = {
    this.extendedAddress = extendedAddress
    this
  }

  def firstName(firstName: String): this.type = {
    this.firstName = firstName
    this
  }

  def lastName(lastName: String): this.type = {
    this.lastName = lastName
    this
  }

  def locality(locality: String): this.type = {
    this.locality = locality
    this
  }

  def postalCode(postalCode: String): this.type = {
    this.postalCode = postalCode
    this
  }

  def region(region: String): this.type = {
    this.region = region
    this
  }

  def streetAddress(streetAddress: String): this.type = {
    this.streetAddress = streetAddress
    this
  }

  override def toQueryString: String = {
    toQueryString(this.tagName)
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  override def toXmlString: String = {
    buildRequest(this.tagName).toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("firstName", firstName).
      addElement("lastName", lastName).
      addElement("company", company).
      addElement("countryName", countryName).
      addElement("countryCodeAlpha2", countryCodeAlpha2).
      addElement("countryCodeAlpha3", countryCodeAlpha3).
      addElement("countryCodeNumeric", countryCodeNumeric).
      addElement("extendedAddress", extendedAddress).
      addElement("locality", locality).
      addElement("postalCode", postalCode).
      addElement("region", region).
      addElement("streetAddress", streetAddress)
  }
}

class AddressRequestWithParent[P <: Request](val done: P) extends AddressRequest with HasParent[P]

class TransactionAddressRequest(parent: TransactionRequest, override val tagName: String)
  extends AddressRequestWithParent[TransactionRequest](parent)

class ApplicantDetailsAddressRequest(parent: ApplicantDetailsRequest)
  extends AddressRequestWithParent[ApplicantDetailsRequest](parent)

/**
 * CreditCardAddressRequests always have a parent which is some subtype P of CreditCardRequest (which may or
 * may not have a parent).
 */
class CreditCardAddressRequest[P <: CreditCardRequest](val parent: P) extends AddressRequestWithParent[P](parent) {
  private var optionsRequest: CreditCardAddressOptionsRequest[CreditCardAddressRequest[P]] = null

  protected override def tagName = "billingAddress"

  def options = {
    optionsRequest = new CreditCardAddressOptionsRequest[CreditCardAddressRequest[P]](this)
    optionsRequest
  }

  protected override def buildRequest(root: String): RequestBuilder = {
    super.buildRequest(root).addElement("options", optionsRequest)
  }
}

object AddressRequest {

  def transactionShipping(parent: TransactionRequest) = new TransactionAddressRequest(parent, "shipping")

  def transactionBilling(parent: TransactionRequest) = new TransactionAddressRequest(parent, "billing")

  def applicantDetails(parent: ApplicantDetailsRequest) = new ApplicantDetailsAddressRequest(parent)

  def creditCard[P <: CreditCardRequest](parent: P) = new CreditCardAddressRequest[P](parent)
}