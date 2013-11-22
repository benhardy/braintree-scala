package com.braintreegateway

class CreditCardOptionsRequest[P <: CreditCardRequest](val done: P) extends BaseRequest with HasParent[P] {

  private var verificationMerchantAccountId: Option[String] = None
  private var failOnDuplicatePaymentMethod: Option[Boolean] = None
  private var verifyCard: Option[Boolean] = None
  private var makeDefault: Option[Boolean] = None
  private var updateExistingToken: Option[String] = None
  private var venmoSdkSession: Option[String] = None

  def verificationMerchantAccountId(verificationMerchantAccountId: String): this.type = {
    this.verificationMerchantAccountId = Option(verificationMerchantAccountId)
    this
  }

  def failOnDuplicatePaymentMethod(failOnDuplicatePaymentMethod: Boolean): this.type = {
    this.failOnDuplicatePaymentMethod = Option(failOnDuplicatePaymentMethod)
    this
  }

  def verifyCard(verifyCard: Boolean): this.type = {
    this.verifyCard = Option(verifyCard)
    this
  }

  def makeDefault(makeDefault: Boolean): this.type = {
    this.makeDefault = Option(makeDefault)
    this
  }

  def updateExistingToken(token: String): this.type = {
    this.updateExistingToken = Option(token)
    this
  }

  def venmoSdkSession(venmoSdkSession: String): this.type = {
    this.venmoSdkSession = Option(venmoSdkSession)
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
    new RequestBuilder(root)
      .addElement("failOnDuplicatePaymentMethod", failOnDuplicatePaymentMethod)
      .addElement("verifyCard", verifyCard)
      .addElement("verificationMerchantAccountId", verificationMerchantAccountId)
      .addElement("makeDefault", makeDefault)
      .addElement("updateExistingToken", updateExistingToken)
      .addElement("venmoSdkSession", venmoSdkSession)
  }
}