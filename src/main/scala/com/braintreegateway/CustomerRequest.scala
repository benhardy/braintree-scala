package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway

/**
 * Provides a fluent interface to build up requests around {@link Customer Customers}.
 */

class CustomerRequest extends BaseRequest {
  private val customFields = Map.newBuilder[String, String]

  private var deviceData: Option[String] = None
  private var company: Option[String] = None
  private var customerId: Option[String] = None
  private var deviceSessionId: Option[String] = None
  private var email: Option[String] = None
  private var fax: Option[String] = None
  private var firstName: Option[String] = None
  private var id: Option[String] = None
  private var lastName: Option[String] = None
  private var phone: Option[String] = None
  private var website: Option[String] = None
  private var creditCardRequest: Option[CreditCardRequest.ForCustomer] = None

  def deviceData(deviceData: String): this.type = {
    this.deviceData = Option(deviceData)
    this
  }

  def company(company: String): this.type = {
    this.company = Option(company)
    this
  }

  def creditCard = {
    val subRequest = CreditCardRequest.forCustomer(this)
    this.creditCardRequest = Some(subRequest)
    subRequest
  }

  def customerId(customerId: String): this.type = {
    this.customerId = Option(customerId)
    this
  }

  def customField(apiName: String, value: String): this.type = {
    customFields += (apiName -> value)
    this
  }

  def deviceSessionId(deviceSessionId: String): this.type = {
    this.deviceSessionId = Option(deviceSessionId)
    this
  }

  def email(email: String): this.type = {
    this.email = Option(email)
    this
  }

  def fax(fax: String): this.type = {
    this.fax = Option(fax)
    this
  }

  def firstName(firstName: String): this.type = {
    this.firstName = Option(firstName)
    this
  }

  override def getKind: String = {
    if (customerId.isDefined) {
      TransparentRedirectGateway.UPDATE_CUSTOMER
    } else {
      TransparentRedirectGateway.CREATE_CUSTOMER
    }
  }

  def lastName(lastName: String): this.type = {
    this.lastName = Option(lastName)
    this
  }

  def id(id: String): this.type = {
    this.id = Option(id)
    this
  }

  def getId: Option[String] = {
    id
  }

  def phone(phone: String): this.type = {
    this.phone = Option(phone)
    this
  }

  def website(website: String): this.type = {
    this.website = Option(website)
    this
  }

  override val xmlName = "customer"

  override def toQueryString: String = {
    toQueryString("customer")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).addTopLevelElement("customerId", customerId).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    val custom = customFields.result
    new RequestBuilder(root)
      .addElement("deviceData", deviceData)
      .addElement("company", company)
      .addElement("email", email)
      .addElement("fax", fax)
      .addElement("firstName", firstName)
      .addElement("id", id)
      .addElement("lastName", lastName)
      .addElement("phone", phone)
      .addElement("website", website)
      .addElement("creditCard", creditCardRequest)
      .addElementIf(!custom.isEmpty, "customFields", custom)
  }
}

object CustomerRequest {

  class ForTransaction(val done: TransactionRequest) extends CustomerRequest with HasParent[TransactionRequest]

  def forTransaction(parent: TransactionRequest) = new ForTransaction(parent)
}