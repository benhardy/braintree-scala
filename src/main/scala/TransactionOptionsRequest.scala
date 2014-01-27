package net.bhardy.braintree.scala

class TransactionOptionsRequest(val done: TransactionRequest) extends BaseRequest with HasParent[TransactionRequest] {

  private var addBillingAddressToPaymentMethod: Option[Boolean] = None
  private var holdInEscrow: Option[Boolean] = None
  private var storeInVault: Option[Boolean] = None
  private var storeInVaultOnSuccess: Option[Boolean] = None
  private var storeShippingAddressInVault: Option[Boolean] = None
  private var submitForSettlement: Option[Boolean] = None
  private var venmoSdkSession: Option[String] = None

  def addBillingAddressToPaymentMethod(addBillingAddressToPaymentMethod: Boolean): TransactionOptionsRequest = {
    this.addBillingAddressToPaymentMethod = Some(addBillingAddressToPaymentMethod)
    this
  }

  def holdInEscrow(holdInEscrow: Boolean): TransactionOptionsRequest = {
    this.holdInEscrow = Some(holdInEscrow)
    this
  }

  def storeInVault(storeInVault: Boolean): TransactionOptionsRequest = {
    this.storeInVault = Some(storeInVault)
    this
  }

  def storeInVaultOnSuccess(storeInVaultOnSuccess: Boolean): TransactionOptionsRequest = {
    this.storeInVaultOnSuccess = Some(storeInVaultOnSuccess)
    this
  }

  def storeShippingAddressInVault(storeShippingAddressInVault: Boolean): TransactionOptionsRequest = {
    this.storeShippingAddressInVault = Some(storeShippingAddressInVault)
    this
  }

  def submitForSettlement(submitForSettlement: Boolean): TransactionOptionsRequest = {
    this.submitForSettlement = Some(submitForSettlement)
    this
  }

  def venmoSdkSession(venmoSdkSession: String): TransactionOptionsRequest = {
    this.venmoSdkSession = Option(venmoSdkSession)
    this
  }

  override val xmlName = "options"

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
}