package com.braintreegateway

class SubscriptionOptionsRequest(val done:SubscriptionRequest) extends BaseRequest with HasParent[SubscriptionRequest] {

  // TODO use options or straight up defaults
  private var doNotInheritAddOnsOrDiscounts: Option[Boolean] = None
  private var prorateCharges: Option[Boolean] = None
  private var replaceAllAddOnsAndDiscounts: Option[Boolean] = None
  private var revertSubscriptionOnProrationFailure: Option[Boolean] = None
  private var startImmediately: Option[Boolean] = None

  def doNotInheritAddOnsOrDiscounts(doNotInheritAddOnsOrDiscounts: Boolean): SubscriptionOptionsRequest = {
    this.doNotInheritAddOnsOrDiscounts = Some(doNotInheritAddOnsOrDiscounts)
    this
  }

  def prorateCharges(prorateCharges: Boolean): SubscriptionOptionsRequest = {
    this.prorateCharges = Some(prorateCharges)
    this
  }

  def replaceAllAddOnsAndDiscounts(replaceAllAddonsAndDiscounts: Boolean): SubscriptionOptionsRequest = {
    this.replaceAllAddOnsAndDiscounts = Some(replaceAllAddonsAndDiscounts)
    this
  }

  def revertSubscriptionOnProrationFailure(revertSubscriptionOnProrationFailure: Boolean): SubscriptionOptionsRequest = {
    this.revertSubscriptionOnProrationFailure = Some(revertSubscriptionOnProrationFailure)
    this
  }

  def startImmediately(startImmediately: Boolean): SubscriptionOptionsRequest = {
    this.startImmediately = Some(startImmediately)
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