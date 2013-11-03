package com.braintreegateway

class TransactionOptionsCloneRequest(val done: TransactionCloneRequest) extends BaseRequest {

  def submitForSettlement(submitForSettlement: Boolean) = {
    _submitForSettlement = submitForSettlement
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
    new RequestBuilder(root).
      addElement("submitForSettlement", _submitForSettlement)
  }

  private var _submitForSettlement: java.lang.Boolean = null
}