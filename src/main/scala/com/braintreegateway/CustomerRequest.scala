package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway
import java.util.HashMap
import java.util.Map

/**
 * Provides a fluent interface to build up requests around {@link Customer Customers}.
 */

class CustomerRequest extends Request {
  private val customFields = new HashMap[String, String]

  private var deviceData: String = null
  private var company: String = null
  private var customerId: String = null
  private var deviceSessionId: String = null
  private var email: String = null
  private var fax: String = null
  private var firstName: String = null
  private var id: String = null
  private var lastName: String = null
  private var phone: String = null
  private var website: String = null
  private var creditCardRequest: CreditCardRequest.ForCustomer = null

  def deviceData(deviceData: String): this.type = {
    this.deviceData = deviceData
    this
  }

  def company(company: String): this.type = {
    this.company = company
    this
  }

  def creditCard = {
    this.creditCardRequest = CreditCardRequest.forCustomer(this)
    this.creditCardRequest
  }

  def customerId(customerId: String): this.type = {
    this.customerId = customerId
    this
  }

  def customField(apiName: String, value: String): this.type = {
    customFields.put(apiName, value)
    this
  }

  def deviceSessionId(deviceSessionId: String): this.type = {
    this.deviceSessionId = deviceSessionId
    this
  }

  def email(email: String): this.type = {
    this.email = email
    this
  }

  def fax(fax: String): this.type = {
    this.fax = fax
    this
  }

  def firstName(firstName: String): this.type = {
    this.firstName = firstName
    this
  }

  def getKind: String = {
    if (this.customerId == null) {
      TransparentRedirectGateway.CREATE_CUSTOMER
    }
    else {
      TransparentRedirectGateway.UPDATE_CUSTOMER
    }
  }

  def lastName(lastName: String): this.type = {
    this.lastName = lastName
    this
  }

  def id(id: String): this.type = {
    this.id = id
    this
  }

  def getId: String = {
    id
  }

  def phone(phone: String): this.type = {
    this.phone = phone
    this
  }

  def website(website: String): this.type = {
    this.website = website
    this
  }

  def toXmlString: String = {
    buildRequest("customer").toXmlString
  }

  def toQueryString: String = {
    toQueryString("customer")
  }

  def toQueryString(root: String): String = {
    buildRequest(root).addTopLevelElement("customerId", customerId).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
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
      .addElementIf(customFields.size > 0, "customFields", customFields)
  }
}

object CustomerRequest {

  class ForTransaction(val done: TransactionRequest) extends CustomerRequest with HasParent[TransactionRequest]

  def forTransaction(parent: TransactionRequest) = new ForTransaction(parent)
}