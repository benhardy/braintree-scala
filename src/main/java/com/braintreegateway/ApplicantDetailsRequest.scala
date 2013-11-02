package com.braintreegateway

class ApplicantDetailsRequest(val done: MerchantAccountRequest) extends BaseRequest with HasParent[MerchantAccountRequest] {

  private var companyName: String = null
  private var firstName: String = null
  private var lastName: String = null
  private var email: String = null
  private var phone: String = null
  private var _address: ApplicantDetailsAddressRequest = null
  private var dateOfBirth: String = null
  private var ssn: String = null
  private var taxId: String = null
  private var routingNumber: String = null
  private var accountNumber: String = null
  
  def companyName(companyName: String): ApplicantDetailsRequest = {
    this.companyName = companyName
    this
  }

  def firstName(firstName: String): ApplicantDetailsRequest = {
    this.firstName = firstName
    this
  }

  def lastName(lastName: String): ApplicantDetailsRequest = {
    this.lastName = lastName
    this
  }

  def email(email: String): ApplicantDetailsRequest = {
    this.email = email
    this
  }

  def phone(phone: String): ApplicantDetailsRequest = {
    this.phone = phone
    this
  }

  def address: ApplicantDetailsAddressRequest = {
    this._address = AddressRequest.applicantDetails(this)
    _address
  }

  def dateOfBirth(dob: String): ApplicantDetailsRequest = {
    this.dateOfBirth = dob
    this
  }

  def ssn(ssn: String): ApplicantDetailsRequest = {
    this.ssn = ssn
    this
  }

  def taxId(taxId: String): ApplicantDetailsRequest = {
    this.taxId = taxId
    this
  }

  def routingNumber(routingNumber: String): ApplicantDetailsRequest = {
    this.routingNumber = routingNumber
    this
  }

  def accountNumber(accountNumber: String): ApplicantDetailsRequest = {
    this.accountNumber = accountNumber
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