package com.braintreegateway

class ApplicantDetailsRequest(val done: MerchantAccountRequest) extends BaseRequest with HasParent[MerchantAccountRequest] {

  private var companyName: Option[String] = None
  private var firstName: Option[String] = None
  private var lastName: Option[String] = None
  private var email: Option[String] = None
  private var phone: Option[String] = None
  private var _address: Option[ApplicantDetailsAddressRequest] = None
  private var dateOfBirth: Option[String] = None
  private var ssn: Option[String] = None
  private var taxId: Option[String] = None
  private var routingNumber: Option[String] = None
  private var accountNumber: Option[String] = None
  
  def companyName(companyName: String): ApplicantDetailsRequest = {
    this.companyName = Option(companyName)
    this
  }

  def firstName(firstName: String): ApplicantDetailsRequest = {
    this.firstName = Option(firstName)
    this
  }

  def lastName(lastName: String): ApplicantDetailsRequest = {
    this.lastName = Option(lastName)
    this
  }

  def email(email: String): ApplicantDetailsRequest = {
    this.email = Option(email)
    this
  }

  def phone(phone: String): ApplicantDetailsRequest = {
    this.phone = Option(phone)
    this
  }

  def address: ApplicantDetailsAddressRequest = {
    val subRequest = AddressRequest.applicantDetails(this)
    this._address = Some(subRequest)
    subRequest
  }

  def dateOfBirth(dob: String): ApplicantDetailsRequest = {
    this.dateOfBirth = Option(dob)
    this
  }

  def ssn(ssn: String): ApplicantDetailsRequest = {
    this.ssn = Option(ssn)
    this
  }

  def taxId(taxId: String): ApplicantDetailsRequest = {
    this.taxId = Option(taxId)
    this
  }

  def routingNumber(routingNumber: String): ApplicantDetailsRequest = {
    this.routingNumber = Option(routingNumber)
    this
  }

  def accountNumber(accountNumber: String): ApplicantDetailsRequest = {
    this.accountNumber = Option(accountNumber)
    this
  }

  override def toQueryString: String = {
    toQueryString("applicant_details")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  override def toXmlString: String = {
    buildRequest("applicant_details").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("companyName", companyName).
      addElement("firstName", firstName).
      addElement("lastName", lastName).
      addElement("email", email).
      addElement("addressRequest", _address).
      addElement("dateOfBirth", dateOfBirth).
      addElement("ssn", ssn).
      addElement("taxId", taxId).
      addElement("routingNumber", routingNumber).
      addElement("accountNumber", accountNumber)
  }
}