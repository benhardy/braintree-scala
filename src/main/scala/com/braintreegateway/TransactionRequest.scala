package com.braintreegateway

import com.braintreegateway.gw.TransparentRedirectGateway
import scala.math.BigDecimal

/**
 * Provides a fluent interface to build up requests around {@link Transaction Transactions}.
 */
class TransactionRequest extends BaseRequest {
  private val customFields = Map.newBuilder[String, String]

  def amount(amount: BigDecimal): TransactionRequest = {
    this.amount = Option(amount)
    this
  }

  def billingAddress: TransactionAddressRequest = {
    val subRequest = AddressRequest.transactionBilling(this)
    billingAddressRequest = Some(subRequest)
    subRequest
  }

  def deviceData(deviceData: String): TransactionRequest = {
    this.deviceData = Option(deviceData)
    this
  }

  def channel(channel: String): TransactionRequest = {
    this.channel = Option(channel)
    this
  }

  def creditCard: TransactionCreditCardRequest = {
    val subRequest = new TransactionCreditCardRequest(this)
    creditCardRequest = Some(subRequest)
    subRequest
  }

  def serviceFeeAmount(fee: BigDecimal): TransactionRequest = {
    serviceFeeAmount = Option(fee)
    this
  }

  def customer: CustomerRequest.ForTransaction = {
    val subRequest = CustomerRequest.forTransaction(this)
    customerRequest = Some(subRequest)
    subRequest
  }

  def customerId(customerId: String): TransactionRequest = {
    this.customerId = Option(customerId)
    this
  }

  def customField(apiName: String, value: String): TransactionRequest = {
    customFields += (apiName -> value)
    this
  }

  def deviceSessionId(deviceSessionId: String): TransactionRequest = {
    this.deviceSessionId = Option(deviceSessionId)
    this
  }

  def descriptor: DescriptorRequest[TransactionRequest] = {
    val subRequest = DescriptorRequest(this)
    descriptorRequest = Some(subRequest)
    subRequest
  }

  override def getKind: String = {
    TransparentRedirectGateway.CREATE_TRANSACTION
  }

  def merchantAccountId(merchantAccountId: String): TransactionRequest = {
    this.merchantAccountId = Option(merchantAccountId)
    this
  }

  def options: TransactionOptionsRequest = {
    val subRequest = new TransactionOptionsRequest(this)
    transactionOptionsRequest = Some(subRequest)
    subRequest
  }

  def orderId(orderId: String): TransactionRequest = {
    this.orderId = Option(orderId)
    this
  }

  def paymentMethodToken(paymentMethodToken: String): TransactionRequest = {
    this.paymentMethodToken = Option(paymentMethodToken)
    this
  }

  def purchaseOrderNumber(purchaseOrderNumber: String): TransactionRequest = {
    this.purchaseOrderNumber = Option(purchaseOrderNumber)
    this
  }

  def recurring(recurring: Boolean): TransactionRequest = {
    this.recurring = Some(recurring)
    this
  }

  def shippingAddress: TransactionAddressRequest = {
    val subRequest = AddressRequest.transactionShipping(this)
    shippingAddressRequest = Some(subRequest)
    subRequest
  }

  def shippingAddressId(shippingAddressId: String): TransactionRequest = {
    this.shippingAddressId = Option(shippingAddressId)
    this
  }

  def taxAmount(taxAmount: BigDecimal): TransactionRequest = {
    this.taxAmount = Option(taxAmount)
    this
  }

  def taxExempt(taxExempt: Boolean): TransactionRequest = {
    this.taxExempt = Some(taxExempt)
    this
  }

  def venmoSdkPaymentMethodCode(venmoSdkPaymentMethodCode: String): TransactionRequest = {
    this.venmoSdkPaymentMethodCode = Option(venmoSdkPaymentMethodCode)
    this
  }

  override def toQueryString: String = {
    toQueryString("transaction")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  def transactionType(transactionType: Transactions.Type): TransactionRequest = {
    this.transactionType = Option(transactionType)
    this
  }

  override val xmlName = "transaction"

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
      .addElement("type", transactionType.map {_.toString.toLowerCase})
  }

  private var amount: Option[BigDecimal] = None
  private var billingAddressRequest: Option[TransactionAddressRequest] = None
  private var deviceData: Option[String] = None
  private var creditCardRequest: Option[TransactionCreditCardRequest] = None
  private var channel: Option[String] = None
  private var customerId: Option[String] = None
  private var deviceSessionId: Option[String] = None
  private var customerRequest: Option[CustomerRequest.ForTransaction] = None
  private var merchantAccountId: Option[String] = None
  private var orderId: Option[String] = None
  private var paymentMethodToken: Option[String] = None
  private var purchaseOrderNumber: Option[String] = None
  private var recurring: Option[Boolean] = None
  private var shippingAddressId: Option[String] = None
  private var descriptorRequest: Option[DescriptorRequest[TransactionRequest]] = None
  private var shippingAddressRequest: Option[TransactionAddressRequest] = None
  private var transactionOptionsRequest: Option[TransactionOptionsRequest] = None
  private var taxAmount: Option[BigDecimal] = None
  private var taxExempt: Option[Boolean] = None
  private var transactionType: Option[Transactions.Type] = None
  private var venmoSdkPaymentMethodCode: Option[String] = None
  private var serviceFeeAmount: Option[BigDecimal] = None
}