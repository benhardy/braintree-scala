package com.braintreegateway

/**
 * Provides a fluent interface to build up requests around {@link Address Addresses}.
 */
class AddressRequest extends BaseRequest {

  private var countryCodeAlpha2: Option[String] = None
  private var countryCodeAlpha3: Option[String] = None
  private var countryCodeNumeric: Option[String] = None
  private var countryName: Option[String] = None
  private var extendedAddress: Option[String] = None
  private var firstName: Option[String] = None
  private var lastName: Option[String] = None
  private var locality: Option[String] = None
  private var postalCode: Option[String] = None
  private var region: Option[String] = None
  private var streetAddress: Option[String] = None
  private var company: Option[String] = None

  protected def tagName = "address"

  def company(company: String): this.type = {
    this.company = Option(company)
    this
  }

  def countryCodeAlpha2(countryCodeAlpha2: String): this.type = {
    this.countryCodeAlpha2 = Option(countryCodeAlpha2)
    this
  }

  def countryCodeAlpha3(countryCodeAlpha3: String): this.type = {
    this.countryCodeAlpha3 = Option(countryCodeAlpha3)
    this
  }

  def countryCodeNumeric(countryCodeNumeric: String): this.type = {
    this.countryCodeNumeric = Option(countryCodeNumeric)
    this
  }

  def countryName(countryName: String): this.type = {
    this.countryName = Option(countryName)
    this
  }

  def extendedAddress(extendedAddress: String): this.type = {
    this.extendedAddress = Option(extendedAddress)
    this
  }

  def firstName(firstName: String): this.type = {
    this.firstName = Option(firstName)
    this
  }

  def lastName(lastName: String): this.type = {
    this.lastName = Option(lastName)
    this
  }

  def locality(locality: String): this.type = {
    this.locality = Option(locality)
    this
  }

  def postalCode(postalCode: String): this.type = {
    this.postalCode = Option(postalCode)
    this
  }

  def region(region: String): this.type = {
    this.region = Option(region)
    this
  }

  def streetAddress(streetAddress: String): this.type = {
    this.streetAddress = Option(streetAddress)
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
  private var optionsRequest: Option[CreditCardAddressOptionsRequest[CreditCardAddressRequest[P]]] = None

  protected override def tagName = "billingAddress"

  def options = {
    val subRequest = new CreditCardAddressOptionsRequest[CreditCardAddressRequest[P]](this)
    optionsRequest = Some(subRequest)
    subRequest
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