package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway

/**
 * Provides a fluent interface to build up requests around {@link CreditCard CreditCards}.
 */
sealed class CreditCardRequest extends Request {

  private var billingAddressRequest: Option[CreditCardAddressRequest[this.type]] = None
  private var billingAddressId: Option[String] = None
  private var deviceData: Option[String] = None
  private var cardholderName: Option[String] = None
  private var customerId: Option[String] = None
  private var cvv: Option[String] = None
  private var deviceSessionId: Option[String] = None
  private var expirationDate: Option[String] = None
  private var expirationMonth: Option[String] = None
  private var expirationYear: Option[String] = None
  private var number: Option[String] = None
  private var optionsRequest: Option[CreditCardOptionsRequest[this.type]] = None
  private var token: Option[String] = None
  private var paymentMethodToken: Option[String] = None
  private var venmoSdkPaymentMethodCode: Option[String] = None

  def billingAddress: CreditCardAddressRequest[this.type] = {
    val subRequest = AddressRequest.creditCard[this.type](this)
    billingAddressRequest = Some(subRequest)
    subRequest
  }

  def billingAddressId(billingAddressId: String): this.type = {
    this.billingAddressId = Option(billingAddressId)
    this
  }

  def deviceData(deviceData: String): this.type = {
    this.deviceData = Option(deviceData)
    this
  }

  def cardholderName(cardholderName: String): this.type = {
    this.cardholderName = Option(cardholderName)
    this
  }

  def customerId(customerId: String): this.type = {
    this.customerId = Option(customerId)
    this
  }

  def cvv(cvv: String): this.type = {
    this.cvv = Option(cvv)
    this
  }

  def deviceSessionId(deviceSessionId: String): this.type = {
    this.deviceSessionId = Option(deviceSessionId)
    this
  }

  def expirationDate(expirationDate: String): this.type = {
    this.expirationDate = Option(expirationDate)
    this
  }

  def expirationMonth(expirationMonth: String): this.type = {
    this.expirationMonth = Option(expirationMonth)
    this
  }

  def expirationYear(expirationYear: String): this.type = {
    this.expirationYear = Option(expirationYear)
    this
  }

  def getCustomerId: Option[String] = {
    customerId
  }

  def getKind: String = {
    if (paymentMethodToken.isDefined) {
      TransparentRedirectGateway.UPDATE_PAYMENT_METHOD
    } else {
      TransparentRedirectGateway.CREATE_PAYMENT_METHOD
    }
  }

  def getToken: Option[String] = {
    token
  }

  def number(number: String): this.type = {
    this.number = Option(number)
    this
  }

  def venmoSdkPaymentMethodCode(venmoSdkPaymentMethodCode: String): this.type = {
    this.venmoSdkPaymentMethodCode = Option(venmoSdkPaymentMethodCode)
    this
  }

  def options: CreditCardOptionsRequest[this.type] = {
    val subRequest = new CreditCardOptionsRequest[this.type](this)
    this.optionsRequest = Some(subRequest)
    subRequest
  }

  def paymentMethodToken(paymentMethodToken: String): this.type = {
    this.paymentMethodToken = Option(paymentMethodToken)
    this
  }

  def token(token: String): this.type = {
    this.token = Option(token)
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
