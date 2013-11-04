package com.braintreegateway

class CreditCardOptionsRequest[P <: CreditCardRequest](val done: P) extends BaseRequest with HasParent[P] {

  private var verificationMerchantAccountId: String = null
  private var failOnDuplicatePaymentMethod: java.lang.Boolean = null
  private var verifyCard: java.lang.Boolean = null
  private var makeDefault: java.lang.Boolean = null
  private var updateExistingToken: String = null
  private var venmoSdkSession: String = null

  def verificationMerchantAccountId(verificationMerchantAccountId: String): this.type = {
    this.verificationMerchantAccountId = verificationMerchantAccountId
    this
  }

  def failOnDuplicatePaymentMethod(failOnDuplicatePaymentMethod: Boolean): this.type = {
    this.failOnDuplicatePaymentMethod = failOnDuplicatePaymentMethod
    this
  }

  def verifyCard(verifyCard: Boolean): this.type = {
    this.verifyCard = verifyCard
    this
  }

  def makeDefault(makeDefault: Boolean): this.type = {
    this.makeDefault = makeDefault
    this
  }

  def updateExistingToken(token: String): this.type = {
    this.updateExistingToken = token
    this
  }

  def venmoSdkSession(venmoSdkSession: String): this.type = {
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
    new RequestBuilder(root)
      .addElement("failOnDuplicatePaymentMethod", failOnDuplicatePaymentMethod)
      .addElement("verifyCard", verifyCard)
      .addElement("verificationMerchantAccountId", verificationMerchantAccountId)
      .addElementIf(makeDefault != null && makeDefault.booleanValue, "makeDefault", makeDefault)
      .addElement("updateExistingToken", updateExistingToken)
      .addElement("venmoSdkSession", venmoSdkSession)
  }
}