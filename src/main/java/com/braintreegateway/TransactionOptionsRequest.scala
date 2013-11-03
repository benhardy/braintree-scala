package com.braintreegateway

class TransactionOptionsRequest(val done: TransactionRequest) extends BaseRequest with HasParent[TransactionRequest] {

  def addBillingAddressToPaymentMethod(addBillingAddressToPaymentMethod: Boolean): TransactionOptionsRequest = {
    this.addBillingAddressToPaymentMethod = addBillingAddressToPaymentMethod
    this
  }

  def holdInEscrow(holdInEscrow: Boolean): TransactionOptionsRequest = {
    this.holdInEscrow = holdInEscrow
    this
  }

  def storeInVault(storeInVault: Boolean): TransactionOptionsRequest = {
    this.storeInVault = storeInVault
    this
  }

  def storeInVaultOnSuccess(storeInVaultOnSuccess: Boolean): TransactionOptionsRequest = {
    this.storeInVaultOnSuccess = storeInVaultOnSuccess
    this
  }

  def storeShippingAddressInVault(storeShippingAddressInVault: Boolean): TransactionOptionsRequest = {
    this.storeShippingAddressInVault = storeShippingAddressInVault
    this
  }

  def submitForSettlement(submitForSettlement: Boolean): TransactionOptionsRequest = {
    this.submitForSettlement = submitForSettlement
    this
  }

  def venmoSdkSession(venmoSdkSession: String): TransactionOptionsRequest = {
    this.venmoSdkSession = venmoSdkSession
    this
  }

  override def toXmlString: String = {
    buildRequest("options").toXmlString
  }

  override def toQueryString: String = {
    toQueryString("options")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("holdInEscrow", holdInEscrow).
      addElement("storeInVault", storeInVault).
      addElement("storeInVaultOnSuccess", storeInVaultOnSuccess).
      addElement("addBillingAddressToPaymentMethod", addBillingAddressToPaymentMethod).
      addElement("storeShippingAddressInVault", storeShippingAddressInVault).
      addElement("submitForSettlement", submitForSettlement).
      addElement("venmoSdkSession", venmoSdkSession)
  }

  private var addBillingAddressToPaymentMethod: java.lang.Boolean = null
  private var holdInEscrow: java.lang.Boolean = null
  private var storeInVault: java.lang.Boolean = null
  private var storeInVaultOnSuccess: java.lang.Boolean = null
  private var storeShippingAddressInVault: java.lang.Boolean = null
  private var submitForSettlement: java.lang.Boolean = null
  private var venmoSdkSession: String = null
}