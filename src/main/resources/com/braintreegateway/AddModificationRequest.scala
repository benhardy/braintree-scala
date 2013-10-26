package com.braintreegateway

class AddModificationRequest(parent: ModificationsRequest) extends ModificationRequest(parent) {

  private var inheritedFromId: String = null

  def inheritedFromId(inheritedFromId: String): AddModificationRequest = {
    this.inheritedFromId = inheritedFromId
    this
  }

  protected override def buildRequest(root: String): RequestBuilder = {
    super.buildRequest(root).addElement("inheritedFromId", inheritedFromId)
  }
}