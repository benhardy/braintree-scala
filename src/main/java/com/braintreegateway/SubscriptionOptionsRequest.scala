package com.braintreegateway

class SubscriptionOptionsRequest(val done:SubscriptionRequest) extends BaseRequest with HasParent[SubscriptionRequest] {

  // TODO use options or straight up defaults
  private var doNotInheritAddOnsOrDiscounts: java.lang.Boolean = null
  private var prorateCharges: java.lang.Boolean = null
  private var replaceAllAddOnsAndDiscounts: java.lang.Boolean = null
  private var revertSubscriptionOnProrationFailure: java.lang.Boolean = null
  private var startImmediately: java.lang.Boolean = null

  def doNotInheritAddOnsOrDiscounts(doNotInheritAddOnsOrDiscounts: Boolean): SubscriptionOptionsRequest = {
    this.doNotInheritAddOnsOrDiscounts = doNotInheritAddOnsOrDiscounts
    this
  }

  def prorateCharges(prorateCharges: Boolean): SubscriptionOptionsRequest = {
    this.prorateCharges = prorateCharges
    this
  }

  def replaceAllAddOnsAndDiscounts(replaceAllAddonsAndDiscounts: Boolean): SubscriptionOptionsRequest = {
    this.replaceAllAddOnsAndDiscounts = replaceAllAddonsAndDiscounts
    this
  }

  def revertSubscriptionOnProrationFailure(revertSubscriptionOnProrationFailure: Boolean): SubscriptionOptionsRequest = {
    this.revertSubscriptionOnProrationFailure = revertSubscriptionOnProrationFailure
    this
  }

  def startImmediately(startImmediately: Boolean): SubscriptionOptionsRequest = {
    this.startImmediately = startImmediately
    this
  }

  override def toXmlString: String = {
    buildRequest("options").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("doNotInheritAddOnsOrDiscounts", doNotInheritAddOnsOrDiscounts).
      addElement("prorateCharges", prorateCharges).
      addElement("replaceAllAddOnsAndDiscounts", replaceAllAddOnsAndDiscounts).
      addElement("revertSubscriptionOnProrationFailure", revertSubscriptionOnProrationFailure).
      addElement("startImmediately", startImmediately)
  }
}