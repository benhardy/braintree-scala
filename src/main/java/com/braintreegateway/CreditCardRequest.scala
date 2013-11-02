package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway

/**
 * Provides a fluent interface to build up requests around {@link CreditCard CreditCards}.
 */
sealed class CreditCardRequest extends Request {

  private var billingAddressRequest: CreditCardAddressRequest[this.type] = null
  private var billingAddressId: String = null
  private var deviceData: String = null
  private var cardholderName: String = null
  private var customerId: String = null
  private var cvv: String = null
  private var deviceSessionId: String = null
  private var expirationDate: String = null
  private var expirationMonth: String = null
  private var expirationYear: String = null
  private var number: String = null
  private var optionsRequest: CreditCardOptionsRequest[this.type] = null
  private var token: String = null
  private var paymentMethodToken: String = null
  private var venmoSdkPaymentMethodCode: String = null

  def billingAddress: CreditCardAddressRequest[this.type] = {
    billingAddressRequest = AddressRequest.creditCard[this.type](this)
    billingAddressRequest
  }

  def billingAddressId(billingAddressId: String): this.type = {
    this.billingAddressId = billingAddressId
    this
  }

  def deviceData(deviceData: String): this.type = {
    this.deviceData = deviceData
    this
  }

  def cardholderName(cardholderName: String): this.type = {
    this.cardholderName = cardholderName
    this
  }

  def customerId(customerId: String): this.type = {
    this.customerId = customerId
    this
  }

  def cvv(cvv: String): this.type = {
    this.cvv = cvv
    this
  }

  def deviceSessionId(deviceSessionId: String): this.type = {
    this.deviceSessionId = deviceSessionId
    this
  }

  def expirationDate(expirationDate: String): this.type = {
    this.expirationDate = expirationDate
    this
  }

  def expirationMonth(expirationMonth: String): this.type = {
    this.expirationMonth = expirationMonth
    this
  }

  def expirationYear(expirationYear: String): this.type = {
    this.expirationYear = expirationYear
    this
  }

  def getCustomerId: String = {
    customerId
  }

  def getKind: String = {
    if (this.paymentMethodToken == null) {
      TransparentRedirectGateway.CREATE_PAYMENT_METHOD
    }
    else {
      TransparentRedirectGateway.UPDATE_PAYMENT_METHOD
    }
  }

  def getToken: String = {
    token
  }

  def number(number: String): this.type = {
    this.number = number
    this
  }

  def venmoSdkPaymentMethodCode(venmoSdkPaymentMethodCode: String): this.type = {
    this.venmoSdkPaymentMethodCode = venmoSdkPaymentMethodCode
    this
  }

  def options: CreditCardOptionsRequest[this.type] = {
    this.optionsRequest = new CreditCardOptionsRequest[this.type](this)
    optionsRequest
  }

  def paymentMethodToken(paymentMethodToken: String): this.type = {
    this.paymentMethodToken = paymentMethodToken
    this
  }

  def token(token: String): this.type = {
    this.token = token
    this
  }

  def toXmlString: String = {
    buildRequest("creditCard").toXmlString
  }

  def toQueryString: String = {
    toQueryString("creditCard")
  }

  def toQueryString(root: String): String = {
    buildRequest(root).addTopLevelElement("paymentMethodToken", paymentMethodToken).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root)
      .addElement("billingAddress", billingAddressRequest)
      .addElement("billingAddressId", billingAddressId)
      .addElement("deviceData", deviceData)
      .addElement("options", optionsRequest)
      .addElement("customerId", customerId)
      .addElement("cardholderName", cardholderName)
      .addElement("cvv", cvv)
      .addElement("number", number)
      .addElement("deviceSessionId", deviceSessionId)
      .addElement("expirationDate", expirationDate)
      .addElement("expirationMonth", expirationMonth)
      .addElement("expirationYear", expirationYear)
      .addElement("token", token)
      .addElement("venmoSdkPaymentMethodCode", venmoSdkPaymentMethodCode)
  }
}

/**
 * Credit card requests can be part of a customer request. Provide a done method to get back to that
 * in that case.
 */
object CreditCardRequest {
  class ForCustomer(val done:CustomerRequest) extends CreditCardRequest with HasParent[CustomerRequest]

  def forCustomer(parent:CustomerRequest) = new ForCustomer(parent)
}
