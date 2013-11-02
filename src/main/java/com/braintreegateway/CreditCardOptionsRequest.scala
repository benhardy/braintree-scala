package com.braintreegateway

class CreditCardOptionsRequest(val done: CreditCardRequest) extends BaseRequest with HasParent[CreditCardRequest] {

  private var verificationMerchantAccountId: String = null
  private var failOnDuplicatePaymentMethod: java.lang.Boolean = null
  private var verifyCard: java.lang.Boolean = null
  private var makeDefault: java.lang.Boolean = null
  private var updateExistingToken: String = null
  private var venmoSdkSession: String = null

  def verificationMerchantAccountId(verificationMerchantAccountId: String): CreditCardOptionsRequest = {
    this.verificationMerchantAccountId = verificationMerchantAccountId
    this
  }

  def failOnDuplicatePaymentMethod(failOnDuplicatePaymentMethod: Boolean): CreditCardOptionsRequest = {
    this.failOnDuplicatePaymentMethod = failOnDuplicatePaymentMethod
    this
  }

  def verifyCard(verifyCard: Boolean): CreditCardOptionsRequest = {
    this.verifyCard = verifyCard
    this
  }

  def makeDefault(makeDefault: Boolean): CreditCardOptionsRequest = {
    this.makeDefault = makeDefault
    this
  }

  def updateExistingToken(token: String): CreditCardOptionsRequest = {
    this.updateExistingToken = token
    this
  }

  def venmoSdkSession(venmoSdkSession: String): CreditCardOptionsRequest = {
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