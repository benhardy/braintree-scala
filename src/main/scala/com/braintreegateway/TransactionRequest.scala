package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway
import scala.math.BigDecimal

/**
 * Provides a fluent interface to build up requests around {@link Transaction Transactions}.
 */
class TransactionRequest extends Request {
  private val customFields = Map.newBuilder[String, String]

  def amount(amount: BigDecimal): TransactionRequest = {
    this.amount = amount
    this
  }

  def billingAddress: TransactionAddressRequest = {
    billingAddressRequest = AddressRequest.transactionBilling(this)
    billingAddressRequest
  }

  def deviceData(deviceData: String): TransactionRequest = {
    this.deviceData = deviceData
    this
  }

  def channel(channel: String): TransactionRequest = {
    this.channel = channel
    this
  }

  def creditCard: TransactionCreditCardRequest = {
    creditCardRequest = new TransactionCreditCardRequest(this)
    creditCardRequest
  }

  def serviceFeeAmount(fee: BigDecimal): TransactionRequest = {
    serviceFeeAmount = fee
    this
  }

  def customer: CustomerRequest.ForTransaction = {
    customerRequest = CustomerRequest.forTransaction(this)
    customerRequest
  }

  def customerId(customerId: String): TransactionRequest = {
    this.customerId = customerId
    this
  }

  def customField(apiName: String, value: String): TransactionRequest = {
    customFields += (apiName -> value)
    this
  }

  def deviceSessionId(deviceSessionId: String): TransactionRequest = {
    this.deviceSessionId = deviceSessionId
    this
  }

  def descriptor: DescriptorRequest[TransactionRequest] = {
    descriptorRequest = DescriptorRequest(this)
    descriptorRequest
  }

  def getKind: String = {
    TransparentRedirectGateway.CREATE_TRANSACTION
  }

  def merchantAccountId(merchantAccountId: String): TransactionRequest = {
    this.merchantAccountId = merchantAccountId
    this
  }

  def options: TransactionOptionsRequest = {
    transactionOptionsRequest = new TransactionOptionsRequest(this)
    transactionOptionsRequest
  }

  def orderId(orderId: String): TransactionRequest = {
    this.orderId = orderId
    this
  }

  def paymentMethodToken(paymentMethodToken: String): TransactionRequest = {
    this.paymentMethodToken = paymentMethodToken
    this
  }

  def purchaseOrderNumber(purchaseOrderNumber: String): TransactionRequest = {
    this.purchaseOrderNumber = purchaseOrderNumber
    this
  }

  def recurring(recurring: Boolean): TransactionRequest = {
    this.recurring = recurring
    this
  }

  def shippingAddress: TransactionAddressRequest = {
    shippingAddressRequest = AddressRequest.transactionShipping(this)
    shippingAddressRequest
  }

  def shippingAddressId(shippingAddressId: String): TransactionRequest = {
    this.shippingAddressId = shippingAddressId
    this
  }

  def taxAmount(taxAmount: BigDecimal): TransactionRequest = {
    this.taxAmount = taxAmount
    this
  }

  def taxExempt(taxExempt: Boolean): TransactionRequest = {
    this.taxExempt = taxExempt
    this
  }

  def venmoSdkPaymentMethodCode(venmoSdkPaymentMethodCode: String): TransactionRequest = {
    this.venmoSdkPaymentMethodCode = venmoSdkPaymentMethodCode
    this
  }

  def toQueryString: String = {
    toQueryString("transaction")
  }

  def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  def toXmlString: String = {
    buildRequest("transaction").toXmlString
  }

  def `type`(`type`: Transactions.Type): TransactionRequest = {
    this.`type` = `type`
    this
  }

  protected def buildRequest(root: String): RequestBuilder = {
    val custom = customFields.result()
    new RequestBuilder(root)
      .addElement("amount", amount)
      .addElement("deviceData", deviceData)
      .addElement("channel", channel)
      .addElement("customerId", customerId)
      .addElement("merchantAccountId", merchantAccountId)
      .addElement("orderId", orderId)
      .addElement("paymentMethodToken", paymentMethodToken)
      .addElement("purchaseOrderNumber", purchaseOrderNumber)
      .addElement("taxAmount", taxAmount)
      .addElement("taxExempt", taxExempt)
      .addElement("shippingAddressId", shippingAddressId)
      .addElement("creditCard", creditCardRequest)
      .addElement("customer", customerRequest)
      .addElement("descriptor", descriptorRequest)
      .addElement("billing", billingAddressRequest)
      .addElement("shipping", shippingAddressRequest)
      .addElement("options", transactionOptionsRequest)
      .addElement("recurring", recurring)
      .addElement("deviceSessionId", deviceSessionId)
      .addElement("venmoSdkPaymentMethodCode", venmoSdkPaymentMethodCode)
      .addElement("serviceFeeAmount", serviceFeeAmount)
      .addElementIf(!custom.isEmpty, "customFields", custom)
      .addLowerCaseElementIfPresent("type", `type`)
  }

  private var amount: BigDecimal = null
  private var billingAddressRequest: TransactionAddressRequest = null
  private var deviceData: String = null
  private var creditCardRequest: TransactionCreditCardRequest = null
  private var channel: String = null
  private var customerId: String = null
  private var deviceSessionId: String = null
  private var customerRequest: CustomerRequest.ForTransaction = null
  private var merchantAccountId: String = null
  private var orderId: String = null
  private var paymentMethodToken: String = null
  private var purchaseOrderNumber: String = null
  private var recurring: java.lang.Boolean = null
  private var shippingAddressId: String = null
  private var descriptorRequest: DescriptorRequest[TransactionRequest] = null
  private var shippingAddressRequest: TransactionAddressRequest = null
  private var transactionOptionsRequest: TransactionOptionsRequest = null
  private var taxAmount: BigDecimal = null
  private var taxExempt: java.lang.Boolean = null
  private var `type`: Transactions.Type = null
  private var venmoSdkPaymentMethodCode: String = null
  private var serviceFeeAmount: BigDecimal = null
}