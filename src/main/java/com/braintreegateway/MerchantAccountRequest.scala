package com.braintreegateway

/**
 * Provides a fluent interface to build up requests around {@link MerchantAccount MerchantAccounts}.
 */
class MerchantAccountRequest extends BaseRequest {

  private var _applicantDetails: ApplicantDetailsRequest = null
  private var tosAccepted: Boolean = false
  private var masterMerchantAccountId: String = null
  private var id: String = null

  def id(id: String): MerchantAccountRequest = {
    this.id = id
    this
  }

  def applicantDetails: ApplicantDetailsRequest = {
    _applicantDetails = new ApplicantDetailsRequest(this)
    _applicantDetails
  }

  def masterMerchantAccountId(masterMerchantAccountId: String): MerchantAccountRequest = {
    this.masterMerchantAccountId = masterMerchantAccountId
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

  override def toXmlString: String = {
    val req = buildRequest("merchant_account")
    val res = req.toXmlString
    res
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root)
      .addElement("applicantDetails", _applicantDetails)
      .addElement("tosAccepted", tosAccepted)
      .addElement("masterMerchantAccountId", masterMerchantAccountId)
      .addElement("id", id)
  }
}
