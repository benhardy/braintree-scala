package net.bhardy.braintree.scala

/**
 * Provides a fluent interface to build up requests around {@link MerchantAccount MerchantAccounts}.
 */
class MerchantAccountRequest extends BaseRequest {

  private var _applicantDetails: Option[ApplicantDetailsRequest] = None
  private var tosAccepted: Boolean = false
  private var masterMerchantAccountId: Option[String] = None
  private var id: Option[String] = None

  def id(id: String): MerchantAccountRequest = {
    this.id = Option(id)
    this
  }

  def applicantDetails: ApplicantDetailsRequest = {
    val subRequest = new ApplicantDetailsRequest(this)
    this._applicantDetails = Some(subRequest)
    subRequest
  }

  def masterMerchantAccountId(masterMerchantAccountId: String): MerchantAccountRequest = {
    this.masterMerchantAccountId = Option(masterMerchantAccountId)
    this
  }

  def tosAccepted(accepted: Boolean): MerchantAccountRequest = {
    this.tosAccepted = accepted
    this
  }

  override def toQueryString: String = {
    toQueryString("merchant_account")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  override val xmlName = "merchant_account"

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root)
      .addElement("applicantDetails", _applicantDetails)
      .addElement("tosAccepted", tosAccepted)
      .addElement("masterMerchantAccountId", masterMerchantAccountId)
      .addElement("id", id)
  }
}
