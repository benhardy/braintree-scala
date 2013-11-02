package com.braintreegateway

class CreditCardAddressOptionsRequest(parent: CreditCardAddressRequest) extends BaseRequest {

  private var updateExisting: Option[Boolean] = None

  def updateExisting(updateExisting: Boolean): HasParent[CreditCardAddressRequest] = {
    this.updateExisting = Some(updateExisting)
    new HasParent[CreditCardAddressRequest] {
      def done = parent
    }
  }

  override def toQueryString: String = {
    toQueryString("options")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  override def toXmlString: String = {
    buildRequest("options").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    val start = new RequestBuilder(root)
    updateExisting.map { update =>
      start.addElement("updateExisting", update)
    } getOrElse {
      start
    }
  }

}