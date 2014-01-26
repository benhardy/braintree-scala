package net.bhardy.braintree.scala

class TransactionOptionsCloneRequest(val done: TransactionCloneRequest) extends BaseRequest {

  private var _submitForSettlement: Option[Boolean] = None

  def submitForSettlement(submitForSettlement: Boolean) = {
    _submitForSettlement = Some(submitForSettlement)
    this
  }

  override val xmlName = "options"

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
}